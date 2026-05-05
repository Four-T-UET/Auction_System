package auction.logic.manager;

import auction.logic.enums.AuctionStatus;
import auction.logic.model.Auction;
import auction.logic.model.User;

public class Admin extends User {

    public Admin(String username, String password){
        super(username, password);
    }
    public void approveAuction(Auction auction){
        if(auction != null){
            if(auction.getStatus() == AuctionStatus.PENDING){
                auction.startAuction();
                System.out.println("Phiên đấu giá đã được phê duyệt thành công");
            }else{
                System.out.println("Đã được phê duyệt");
            }
        }else{
            System.out.println("Phiên đấu giá không tồn tại");
        }
    }
   public void cancelAuction(Auction auction) {
        if(auction != null){
            auction.cancelAuction();
            System.out.println("Phiên đấu giá đã được huỷ");
        }else{
            System.out.println("Phiên đấu giá không tồn tại");
        }
    }

}
