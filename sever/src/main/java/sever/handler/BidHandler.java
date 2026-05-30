package sever.handler;

import auction.logic.RequestDTO.BidDTO;
import auction.logic.ResponseDTO.BroadcastMessage;
import auction.logic.ResponseDTO.WalletResponseDTO;
import auction.logic.enums.AuctionStatus;
import auction.logic.model.Auction;
import auction.logic.model.Clients;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sever.dao.AuctionDAO;
import sever.dao.WalletDAO;
import sever.manager.AuctionRuntimeManager;
import sever.manager.ClientRuntimeManager;
import sever.manager.ServerClientManager;
import sever.scheduler.AuctionStatusScheduler;

public class BidHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(BidHandler.class);
    private final AuctionDAO auctionDAO = AuctionDAO.getInstance();
    private final WalletDAO walletDAO = WalletDAO.getInstance();

    public void handle(BidDTO bidDTO, ObjectOutputStream out) {
        try {
            if (bidDTO == null) {
                out.writeObject("FAILED: Invalid request");
                out.flush();
                return;
            }
            String auctionId = bidDTO.getAuctionId();
            String bidderId = bidDTO.getBidderId();
            double price = bidDTO.getPrice();

            Auction auction = AuctionRuntimeManager.getInstance().getOrLoad(auctionId);
            /// //////////////////////////////////////////////////////////////

            if (auction == null) {
              out.writeObject("FAILED: Auction not found");
              out.flush();
              return;
            }

            
            /// ////////////////////////////////////////////////////////////

            if (auction.getStatus() != AuctionStatus.RUNNING) {
                out.writeObject("FAILED: Auction not running");
                out.flush();
                return;
            }

            Clients seller = auction.getSeller();
            if (seller != null && bidderId.equals(seller.getId())) {
                out.writeObject("FAILED: Seller cannot bid on own auction");
                out.flush();
                return;
            }

            if (seller == null) {
                String sellerId = auctionDAO.findSellerIdByAuctionId(auctionId);
                if (sellerId != null && sellerId.equals(bidderId)) {
                    out.writeObject("FAILED: Seller cannot bid on own auction");
                    out.flush();
                    return;
                }
            }

            Clients bidder = ClientRuntimeManager.getInstance().getOrLoad(bidderId);
            if (bidder == null) {
                out.writeObject("FAILED: Bidder not found");
                out.flush();
                return;
            }

            Clients previousWinner = auction.getCurrentWinner();
            double previousPrice = auction.getCurrentPrice();

            synchronized (auction) {
                bidder.placeBid(auction, price);
            }

            boolean bidAccepted = auction.getCurrentPrice() != previousPrice && auction.getCurrentWinner() == bidder;
            if (!bidAccepted) {
                out.writeObject("FAILED: Bid rejected");
                out.flush();
                return;
            }
            /// /////////////////////////////////////////

            long remainingSeconds = Duration.between(LocalDateTime.now(), auction.getFinishTime()).getSeconds();
            if (remainingSeconds <= 10) {
                LocalDateTime oldFinishTime = auction.getFinishTime();
                LocalDateTime newFinishTime = oldFinishTime.plusSeconds(30);
                auction.setFinishTime(newFinishTime);
                auctionDAO.updateFinishTime(auction.getId(), newFinishTime);
                AuctionStatusScheduler.getInstance().rescheduleAuctionEnd(auction.getId(), newFinishTime);
                LOGGER.info("[ANTI_SNIPING] auctionId=" + auction.getId()
                    + " bidderId=" + bidder.getId()
                    + " remainingSeconds=" + remainingSeconds
                    + " oldFinishTime=" + oldFinishTime
                    + " newFinishTime=" + newFinishTime);
            }

            // cập nhật database time
            String currentWinnerId = auction.getCurrentWinner() != null ? auction.getCurrentWinner().getId() : null;
            auctionDAO.updatePriceAndWinner(auction.getId(), auction.getCurrentPrice(), currentWinnerId);
            walletDAO.updateWallet(bidder.getId(), bidder.getWallet().getBalance(), bidder.getWallet().getLockBalance());

            // cập nhật ví cho bidder
            WalletResponseDTO bidderWalletUpdate = new WalletResponseDTO(
                bidder.getWallet().getBalance(),
                bidder.getWallet().getLockBalance()
            );
            BroadcastMessage walletMsgForBidder = new BroadcastMessage(
                BroadcastMessage.EventType.WALLET_UPDATED,
                bidderWalletUpdate
            );
            ServerClientManager.getInstance().sendToUser(bidder.getId(), walletMsgForBidder);
            // Xử lý những thằng previous winner ( nếu có tồn tại và khác thằng bidder hiện tại )
            if (previousWinner != null && previousWinner != bidder) {
                walletDAO.updateWallet(previousWinner.getId(), previousWinner.getWallet().getBalance(), previousWinner.getWallet().getLockBalance());

                WalletResponseDTO prevWinnerWalletUpdate = new WalletResponseDTO(
                    previousWinner.getWallet().getBalance(),
                    previousWinner.getWallet().getLockBalance()
                );
                BroadcastMessage walletMsgForPrevWinner = new BroadcastMessage(
                    BroadcastMessage.EventType.WALLET_UPDATED,
                    prevWinnerWalletUpdate
                );
                ServerClientManager.getInstance().sendToUser(previousWinner.getId(), walletMsgForPrevWinner);
            }

            // cập nhật runtime cache
            AuctionRuntimeManager.getInstance().addOrUpdate(auction);
            out.writeObject(auction);
            out.flush();
            // Thông báo, broadcast auction cập nhật tới tất cả các thằng clients
            BroadcastMessage broadcastMsg = new BroadcastMessage(BroadcastMessage.EventType.AUCTION_UPDATED, auction);
            ServerClientManager.getInstance().broadcastToAll(broadcastMsg);
        } catch (IllegalArgumentException | IllegalStateException e) {
            try {
                out.writeObject("FAILED: " + e.getMessage());
                out.flush();
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            try {
                out.writeObject("FAILED: Server error");
                out.flush();
            } catch (Exception ignored) {
            }
        }
    }
}
