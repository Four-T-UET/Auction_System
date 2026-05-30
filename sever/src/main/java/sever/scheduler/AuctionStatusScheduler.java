package sever.scheduler;

import auction.logic.ResponseDTO.BroadcastMessage;
import auction.logic.ResponseDTO.WalletResponseDTO;
import auction.logic.enums.AuctionStatus;
import auction.logic.model.Auction;
import auction.logic.model.Clients;
import sever.dao.AuctionDAO;
import sever.dao.WalletDAO;
import sever.manager.AuctionRuntimeManager;
import sever.manager.ClientRuntimeManager;
import sever.manager.ServerClientManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionStatusScheduler {
    // Thread Pool quản lý danh sách các "báo thức" hẹn giờ kết thúc đấu giá
    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionStatusScheduler.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final ConcurrentHashMap<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    private final AuctionDAO auctionDAO = AuctionDAO.getInstance();
    private final WalletDAO walletDAO = WalletDAO.getInstance();

    private static final AuctionStatusScheduler instance = new AuctionStatusScheduler();
    public static AuctionStatusScheduler getInstance() { return instance; }

    private AuctionStatusScheduler() {}

    /**
     * Gọi 1 lần duy nhất lúc khởi động Server để nạp lại báo thức cho các phiên đang chạy dở
     */
    public void start() {
        LOGGER.info("Auction Status Scheduler : Initialize scheduler");
        scheduleAllRunningAuctionsFromDB();
    }

    /**
     * Đăng ký hẹn giờ kết thúc cho 1 phiên đấu giá (Cả phiên cũ lẫn phiên mới tạo)
     */
    public void scheduleAuctionEnd(String auctionId, LocalDateTime finishTime) {
        LocalDateTime now = LocalDateTime.now();
        long delayInSeconds = Duration.between(now, finishTime).getSeconds();


        ScheduledFuture<?> previousTask = scheduledTasks.remove(auctionId);
        if (previousTask != null) {
            previousTask.cancel(false);
        }

        ScheduledFuture<?> future = scheduler.schedule(() -> processAuctionEnd(auctionId), delayInSeconds, TimeUnit.SECONDS);
        scheduledTasks.put(auctionId, future);
        LOGGER.info("AuctionStatusScheduler : Scheduled auction " + auctionId + " to end in " + delayInSeconds + " seconds.");
    }

    public void rescheduleAuctionEnd(String auctionId, LocalDateTime newFinishTime) {
        scheduleAuctionEnd(auctionId, newFinishTime);
        LOGGER.info("[ANTI_SNIPING] Rescheduled auction " + auctionId + " to " + newFinishTime);
    }

    private void processAuctionEnd(String auctionId) {
        Auction fullAuction = AuctionRuntimeManager.getInstance().getOrLoad(auctionId);
        if (fullAuction == null || fullAuction.getStatus() != AuctionStatus.RUNNING) {
            scheduledTasks.remove(auctionId);
            return;
        }

        // Chuyển trạng thái trên RAM sang FINISHED để khóa phòng, chặn API đặt cược chen ngang
        fullAuction.setStatus(AuctionStatus.FINISHED);

        Clients winnerToNotify = null;
        Clients sellerToNotify = null;

        try {
            // 1. Cập nhật trạng thái FINISHED dưới DB
            auctionDAO.updateStatus(fullAuction.getId(), AuctionStatus.FINISHED.name());

            Clients winner = fullAuction.getCurrentWinner();
            Clients seller = fullAuction.getSeller();

            if (winner != null && seller != null && !winner.getId().equals(seller.getId())) {

                // Tránh lỗi Deadlock bằng cách xếp thứ tự ổ khóa theo ID cố định
                Clients firstLock = winner.getId().compareTo(seller.getId()) < 0 ? winner : seller;
                Clients secondLock = firstLock == winner ? seller : winner;

                synchronized (firstLock.getWallet()) {
                    synchronized (secondLock.getWallet()) {
                        double amount = winner.getWallet().getLockedAmount(fullAuction.getId());

                        if (amount > 0) {
                            // Cập nhật số dư trên RAM
                            winner.getWallet().deductLockBalance(fullAuction);
                            seller.getWallet().deposit(amount);
                            ClientRuntimeManager.getInstance().addOrUpdate(winner);
                            ClientRuntimeManager.getInstance().addOrUpdate(seller);

                            // Đồng bộ xuống CSDL
                            walletDAO.updateWallet(winner.getId(), winner.getWallet().getBalance(), winner.getWallet().getLockBalance());
                            walletDAO.updateWallet(seller.getId(), seller.getWallet().getBalance(), seller.getWallet().getLockBalance());

                            // Chuyển trạng thái sang PAID ở cả DB và RAM
                            auctionDAO.updateStatus(fullAuction.getId(), AuctionStatus.PAID.name());
                            fullAuction.setStatus(AuctionStatus.PAID);

                            winnerToNotify = winner;
                            sellerToNotify = seller;
                        }
                    }
                }
            }

            // 2. Bắn Socket thông báo (Nằm ngoài khối synchronized giúp mượt mạng)
            if (winnerToNotify != null && sellerToNotify != null) {
                WalletResponseDTO winnerWalletUpdate = new WalletResponseDTO(winnerToNotify.getWallet().getBalance(), winnerToNotify.getWallet().getLockBalance());
                ServerClientManager.getInstance().sendToUser(winnerToNotify.getId(), new BroadcastMessage(BroadcastMessage.EventType.WALLET_UPDATED, winnerWalletUpdate));

                WalletResponseDTO sellerWalletUpdate = new WalletResponseDTO(sellerToNotify.getWallet().getBalance(), sellerToNotify.getWallet().getLockBalance());
                ServerClientManager.getInstance().sendToUser(sellerToNotify.getId(), new BroadcastMessage(BroadcastMessage.EventType.WALLET_UPDATED, sellerWalletUpdate));
            }

            ServerClientManager.getInstance().broadcastToAll(new BroadcastMessage(BroadcastMessage.EventType.AUCTION_FINISHED, fullAuction));
            LOGGER.info("[AuctionStatusScheduler] Successfully processed expired auction: " + fullAuction.getId());

        } catch (SQLException e) {
            LOGGER.error("[AuctionStatusScheduler] DB Error processing auction " + fullAuction.getId(), e);
            // Nếu lỗi DB, trả trạng thái RAM về RUNNING để luồng sau có cơ hội xử lý lại
            fullAuction.setStatus(AuctionStatus.RUNNING);
        }
        scheduledTasks.remove(auctionId);
    }

    private void scheduleAllRunningAuctionsFromDB() {
        String sql = "SELECT id, finish_time FROM auctions WHERE status = 'RUNNING'";
        try (Connection conn = sever.config.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                String id = rs.getString("id");
                LocalDateTime finishTime = rs.getObject("finish_time", LocalDateTime.class);
                scheduleAuctionEnd(id, finishTime);
                count++;
            }
            LOGGER.info("[AuctionStatusScheduler] Khôi phục và lên lịch " + count + " phiên đấu giá đang chạy từ Database.");
        } catch (SQLException e) {
            LOGGER.error("[AuctionStatusScheduler] Error reloading running auctions on startup", e);
        }
    }

    public void stop() {
        scheduler.shutdown();
    }
}