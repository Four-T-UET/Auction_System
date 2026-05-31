package auction.logic.manager;

import auction.logic.enums.AuctionStatus;
import auction.logic.model.Auction;
import auction.logic.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Admin extends User {
    private static final Logger LOGGER = LoggerFactory.getLogger(Admin.class);

    public Admin(String username, String password){
        super(username, password,"ADMIN");
    }
    public void approveAuction(Auction auction){
        if(auction != null){
            if(auction.getStatus() == AuctionStatus.PENDING){
                auction.startAuction();
                LOGGER.info("Phiên đấu giá đã được phê duyệt thành công");
            }else{
                LOGGER.warn("Đã được phê duyệt");
            }
        }else{
            LOGGER.warn("Phiên đấu giá không tồn tại");
        }
    }
   public void cancelAuction(Auction auction) {
        if(auction != null){
            auction.cancelAuction();
            LOGGER.info("Phiên đấu giá đã được huỷ");
        }else{
            LOGGER.warn("Phiên đấu giá không tồn tại");
        }
    }

}
