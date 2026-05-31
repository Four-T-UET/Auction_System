package service;

import auction.logic.ResponseDTO.BroadcastMessage;
import auction.logic.ResponseDTO.WalletResponseDTO;
import stateManager.AuctionManager;
import stateManager.WalletManager;
import auction.logic.model.Clients;
import stateManager.UserSession;

public class BroadcastDispatcher {

  public static void process(BroadcastMessage msg) {
    if (msg == null) return;

    // Cập nhật thời gian trước
    TimeSyncService.getInstance().updateClock(msg.getServerNowMillis(), msg.getServerZoneId());

    //Đi điều phối các logic nghiệp vụ khác
    switch (msg.getEventType()) {
      case AUCTION_CREATED:
      case AUCTION_UPDATED:
      case AUCTION_FINISHED:
        if (msg.getAuction() != null) {
          AuctionManager.getInstance().addOrUpdate(msg.getAuction());
        }
        break;
    /// ////////////////////////////////////////////////
      case AUCTION_CANCELLED:
        if (msg.getAuction() != null){
          AuctionManager.getInstance().addOrUpdate(msg.getAuction());
        }
        break;
     ///////////////////////////////////  //////////////
      case AUCTION_REMOVED:
        if (msg.getAuctionId() != null) {
          AuctionManager.getInstance().removeById(msg.getAuctionId());
        }
        break;
      case WALLET_UPDATED:
        if (msg.getData() instanceof WalletResponseDTO walletUpdate) {
          WalletManager.getInstance().updateWallet(walletUpdate.getBalance(), walletUpdate.getLocked());
          Clients currentUser = UserSession.getCurrentUser();
          if (currentUser != null) {
            currentUser.getWallet().setBalance(walletUpdate.getBalance());
            currentUser.getWallet().settotalLockBalance(walletUpdate.getLocked());
          }
        }
        break;
    }
  }
}