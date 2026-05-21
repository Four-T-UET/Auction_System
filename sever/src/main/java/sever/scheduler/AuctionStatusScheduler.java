package sever.scheduler;

import auction.logic.ResponseDTO.BroadcastMessage;
import auction.logic.enums.AuctionStatus;
import auction.logic.model.Auction;
import sever.dao.AuctionDAO;
import sever.manager.ServerClientManager;
import sever.service.AuctionService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionStatusScheduler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AuctionDAO auctionDAO = AuctionDAO.getInstance();

    public void start() {
        scheduler.scheduleAtFixedRate(this::checkAndUpdateExpiredAuctions, 0, 1, TimeUnit.SECONDS);
        System.out.println("[AuctionStatusScheduler] Started, checking every 30 seconds");
    }

    private void checkAndUpdateExpiredAuctions() {
        List<ExpiredAuction> expiredAuctions = getExpiredRunningAuctions();
        if (expiredAuctions.isEmpty()) return;
        
        AuctionService auctionService = new AuctionService();
        for (ExpiredAuction expired : expiredAuctions) {
            try {
                // Update status in database
                auctionDAO.updateStatus(expired.auctionId, AuctionStatus.FINISHED.name());
                System.out.println("[AuctionStatusScheduler] Updated auction status to FINISHED: " + expired.auctionId);

                // Fetch full auction data from database (preserves all fields)
                Auction fullAuction = auctionService.getAuctionById(expired.auctionId);
                if (fullAuction != null) {
                    // Broadcast full auction object to all clients
                    BroadcastMessage msg = new BroadcastMessage(BroadcastMessage.EventType.AUCTION_FINISHED, fullAuction);
                    ServerClientManager.getInstance().broadcastToAll(msg);
                    System.out.println("[AuctionStatusScheduler] Broadcasted full auction " + expired.auctionId + " with status FINISHED");
                } else {
                    System.err.println("[AuctionStatusScheduler] Could not fetch full auction for " + expired.auctionId);
                }
            } catch (SQLException e) {
                System.err.println("[AuctionStatusScheduler] Failed to update auction " + expired.auctionId + ": " + e.getMessage());
            }
        }
    }

    private List<ExpiredAuction> getExpiredRunningAuctions() {
        List<ExpiredAuction> list = new ArrayList<>();
        String sql = "SELECT id FROM auctions WHERE status = 'RUNNING' AND finish_time <= ?";
        try (Connection conn = sever.config.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, LocalDateTime.now());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ExpiredAuction ea = new ExpiredAuction();
                    ea.auctionId = rs.getString("id");
                    list.add(ea);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AuctionStatusScheduler] Error querying expired auctions: " + e.getMessage());
        }
        return list;
    }

    private static class ExpiredAuction {
        String auctionId;
    }
}
