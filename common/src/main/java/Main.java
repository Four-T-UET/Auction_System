import auction.logic.factory.ArtFactory;
import auction.logic.factory.ItemFactory;
import auction.logic.manager.Admin;
import auction.logic.manager.AuctionManager;
import auction.logic.model.Auction;
import auction.logic.model.Clients;
import auction.logic.model.Item;

public class Main {
    public static void main(String[] args) {
        // tạo Admin
        Admin admin = new Admin("Bui Duy Thang", "Fourty");


        // 1. Tạo 2 người dùng
        Clients user1 = new Clients("Alice", "pass123");
        Clients user2 = new Clients("Bob", "pass456");
        AuctionManager auctionManager = AuctionManager.getInstance(); // tạo Auction Manager
        user1.deposit(100000);



        // Factory Method tạo món đồ để đấu giá
//        ItemFactory item1 = new ArtFactory();
//        Item phone =  item1.createItem("Iphone 11 promax", "Apple");
//
//        // 3. User1 mở một phiên đấu giá bán iPhone (Giá khởi điểm 1000, bước giá 50)
//        Auction auction1 = user1.addAuction(phone, 1000, 50);
//        System.out.println(auction1.getStatus());
//        admin.approveAuction(auction1); // admin duyệt auction
//        auctionManager.add(auction1); // thêm auction vào trong auction manager.
//        System.out.println(auction1.getStatus());


//
//
//        System.out.println("--- BẮT ĐẦU ĐẤU GIÁ ---");
//        // 4. User2 vào đặt giá 1050 (Hợp lệ vì 1000 + 50 <= 1050)
//        user2.placeBid(auction1, 1050);
//
//        // 5. User1 tự đặt giá 1080 (Thất bại vì bước giá yêu cầu là 50 -> Cần ít nhất 1100)
//        user1.placeBid(auction1, 1080);
//
//        // 6. User1 đặt lại 1150 (Hợp lệ)
//        user1.placeBid(auction1, 1150);
//
//        System.out.println("Chạy thành công mà không cần sửa kiến trúc!");
    }
}