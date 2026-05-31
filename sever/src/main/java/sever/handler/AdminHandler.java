package sever.handler;

import auction.logic.RequestDTO.CancelAuctionDTO;
import auction.logic.ResponseDTO.BroadcastMessage;
import auction.logic.model.Auction;
import auction.logic.model.Clients;
import sever.dao.WalletDAO;
import sever.manager.AuctionRuntimeManager;
import sever.manager.ClientRuntimeManager;
import sever.manager.ServerClientManager;
import sever.service.AuctionService;

import java.io.ObjectOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AdminHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminHandler.class);
    private final AuctionService auctionService = new AuctionService();

    public void handleCancel(CancelAuctionDTO cancelAuctionDTO, ObjectOutputStream out) {
        try {
            if (cancelAuctionDTO == null
                    || cancelAuctionDTO.getAuctionId() == null
                    || cancelAuctionDTO.getAuctionId().isBlank()) {
                out.writeObject("ERROR:  Auction ID is required");
                out.flush();
                return;
            }

            Auction auction = auctionService.cancelActiveAuction(cancelAuctionDTO.getAuctionId());
            if (auction == null) {
                out.writeObject("ERROR: You cann't cancel again");
                out.flush();
                return;
            }
            // Nạp vào trong AuctionRunTime - câp nhật vào trong cache
            AuctionRuntimeManager.getInstance().addOrUpdate(auction);

            // Hoàn tiền trong DB nếu có winner
            if (auction.getCurrentWinner() instanceof Clients winner && winner.getWallet() != null) {
                double balance = winner.getWallet().getBalance();
                double locked = winner.getWallet().getLockBalance();

                WalletDAO.getInstance().updateWallet(winner.getId(), balance, locked);
                ClientRuntimeManager.getInstance().addOrUpdate(winner);
/// /////////////////////////
                // Gửi thông báo cập nhật ví tới winner (giống như AuctionStatusScheduler / BidHandler)
                try {
                    auction.logic.ResponseDTO.WalletResponseDTO walletUpdate = new auction.logic.ResponseDTO.WalletResponseDTO(balance, locked);
                    ServerClientManager.getInstance().sendToUser(winner.getId(), new BroadcastMessage(BroadcastMessage.EventType.WALLET_UPDATED, walletUpdate));
                } catch (Exception ex) {
                    LOGGER.warn("[Admin Handler] :Không thể thông báo ví đã cập nhật", ex);
                }
            }
/// ////////////////////////////
            BroadcastMessage msg = new BroadcastMessage(
                    BroadcastMessage.EventType.AUCTION_CANCELLED,
                    auction
            );
            msg.setAuctionId(auction.getId());
            ServerClientManager.getInstance().broadcastToAll(msg);

            out.writeObject("SUCCESS: Auction has been cancelled");
            out.flush();

        } catch (Exception e) {
            LOGGER.error("Admin cancel auction failed", e);
        }
    }
}