package sever.handler;

import auction.logic.RequestDTO.BidDTO;
import auction.logic.ResponseDTO.BroadcastMessage;
import auction.logic.ResponseDTO.WalletResponseDTO;
import auction.logic.enums.AuctionStatus;
import auction.logic.model.Auction;
import auction.logic.model.Clients;
import java.io.ObjectOutputStream;
import sever.dao.AuctionDAO;
import sever.dao.WalletDAO;
import sever.manager.AuctionRuntimeManager;
import sever.manager.ClientRuntimeManager;
import sever.manager.ServerClientManager;

public class BidHandler {
    private final AuctionDAO auctionDAO = AuctionDAO.getInstance();
    private final WalletDAO walletDAO = new WalletDAO();

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
            if (auction == null) {
                out.writeObject("FAILED: Auction not found");
                out.flush();
                return;
            }
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

            // Update database
            String currentWinnerId = auction.getCurrentWinner() != null ? auction.getCurrentWinner().getId() : null;
            auctionDAO.updatePriceAndWinner(auction.getId(), auction.getCurrentPrice(), currentWinnerId);
            walletDAO.updateWalletSnapshot(bidder.getId(), bidder.getWallet().getBalance(), bidder.getWallet().getLockBalance());

            // Send wallet update to bidder
            WalletResponseDTO bidderWalletUpdate = new WalletResponseDTO(
                bidder.getWallet().getBalance(),
                bidder.getWallet().getLockBalance()
            );
            BroadcastMessage walletMsgForBidder = new BroadcastMessage(
                BroadcastMessage.EventType.WALLET_UPDATED,
                bidderWalletUpdate
            );
            ServerClientManager.getInstance().sendToUser(bidder.getId(), walletMsgForBidder);
            
            // Handle previous winner (if exists and different from bidder)
            if (previousWinner != null && previousWinner != bidder) {
                walletDAO.updateWalletSnapshot(previousWinner.getId(), previousWinner.getWallet().getBalance(), previousWinner.getWallet().getLockBalance());

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

            // Update runtime cache
            AuctionRuntimeManager.getInstance().addOrUpdate(auction);
            out.writeObject(auction);
            out.flush();

            // Broadcast auction update to all clients
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
