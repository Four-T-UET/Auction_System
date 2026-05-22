package sever.handler;

import auction.logic.RequestDTO.AuctionDTO;
import auction.logic.model.Auction;
import auction.logic.ResponseDTO.BroadcastMessage;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import sever.dao.AuctionDAO;
import sever.service.AuctionService;
import sever.manager.ServerClientManager;
import sever.manager.AuctionRuntimeManager;

public class AuctionHandler {
  public void handle(AuctionDTO auctionDTO, ObjectOutputStream out) {
    try {

      String itemid = auctionDTO.getId();
      double startPrice = auctionDTO.getStartValue();
      double minPrice = auctionDTO.getMinStep();
      int duration = auctionDTO.getDuration();


      AuctionService auctionService = new AuctionService();
      Auction auction = auctionService.createAuction(itemid, startPrice, minPrice, duration);  // tìm USER từ database
      Object response;
      if (auction != null) { // gọi hàm login trong User ( logic )
        response = auction; // gán phản hồi là User đó ( là một Object ) vì đã implements Serializable
        System.out.println("[AuctionHandler] ✓ Tạo phiên đấu giá thành công: " + auction.getId());

        AuctionRuntimeManager.getInstance().addOrUpdate(auction);
        //  BROADCAST AUCTION MỚI tới TẤT CẢ OTHER CLIENTS
        BroadcastMessage broadcastMsg = new BroadcastMessage(
            BroadcastMessage.EventType.AUCTION_CREATED,
            auction
        );
        ServerClientManager.getInstance().broadcastToAll(broadcastMsg);
        System.out.println("[AuctionHandler] ✓ Broadcast new auction to all clients");
      } else {
        response = "FAILED";
        System.out.println("[AuctionHandler] ✗ Tạo phiên đấu giá thất bại");
      }
      out.writeObject(response);
      out.flush();

    }catch (Exception e){
      e.printStackTrace();
    }

  }
  // gọi auctionDao để pull data từ dtb về
  public void pull(ObjectOutputStream out) {
    try {
      AuctionService auctionService = new AuctionService();
      List<Auction> list = auctionService.getAllAuctions();
      out.writeObject(list);
      out.flush();
    }catch (Exception e){
      e.printStackTrace();
    }


  }

}
