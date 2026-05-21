package auction.logic.manager;

import auction.logic.enums.AuctionEvent;
import auction.logic.enums.AuctionStatus;
import auction.logic.model.Auction;
import java.io.Serializable;

public class AuctionStateManagement implements Serializable {
    public synchronized void startAuction(Auction auction){
        if(auction.getStatus() == AuctionStatus.PENDING){
            auction.setStatus(AuctionStatus.RUNNING);
            System.out.println("Bắt đầu phiên đấu giá");
        }
    }
    public synchronized void finishAuction(Auction auction){
        if(auction.getStatus() == AuctionStatus.RUNNING){
            auction.setStatus(AuctionStatus.FINISHED);
            payingAuction(auction);
            System.out.println("Hoàn thành phiên đấu giá");
        }
    }
    public synchronized void payingAuction(Auction auction){
        if ( auction.getStatus() != AuctionStatus.FINISHED){
            System.out.println("Phiên đấu giá không thể thanh toán");
            return;
        }
        if (auction.getCurrentWinner() == null || auction.getCurrentWinner().getWallet() == null){
            System.out.println("Không có người thắng để thanh toán");
            return;
        }

        auction.getCurrentWinner().deductLockbalance(auction);
         auction.setStatus(AuctionStatus.PAID);
        System.out.println("Phiên đấu giá đã được thanh toán");
    }

    public synchronized void cancelAuction(Auction auction){
        if(auction.getStatus() != AuctionStatus.PAID && auction.getStatus() != AuctionStatus.CANCELLED){
            auction.setStatus( AuctionStatus.CANCELLED);
            auction.notifyObservers(AuctionEvent.AUCTION_CANCELLED,"Phiên đấu giá đã bị huỷ");
            try{
                auction.getCurrentWinner().releaseBalance(auction);

            } catch (NullPointerException e) {
                System.out.println("");
            }
            System.out.println("Phiên đâu giá đã bị huỷ");
        }else{
            System.out.println("Không thể huỷ phiên đấu giá");
        }
    }

}

