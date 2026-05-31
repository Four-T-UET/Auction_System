package auction.logic.manager;

import auction.logic.enums.AuctionStatus;
import auction.logic.model.Auction;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionStateManagement implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionStateManagement.class);
    public synchronized void startAuction(Auction auction){
        if(auction.getStatus() == AuctionStatus.PENDING){
            auction.setStatus(AuctionStatus.RUNNING);
            LOGGER.info("Bắt đầu phiên đấu giá");
        }
    }

    public synchronized void cancelAuction(Auction auction){
        if(auction.getStatus() != AuctionStatus.PAID && auction.getStatus() != AuctionStatus.CANCELLED){
            auction.setStatus( AuctionStatus.CANCELLED);
            try{
                auction.getCurrentWinner().releaseBalance(auction);
            } catch (NullPointerException e) {
                LOGGER.debug("No current winner to release balance.", e);
            }
            LOGGER.info("Phiên đấu giá đã bị huỷ");
        }else{
            LOGGER.warn("Không thể huỷ phiên đấu giá");
        }
    }

}
