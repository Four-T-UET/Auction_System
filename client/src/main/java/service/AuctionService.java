package service;

import auction.logic.RequestDTO.AuctionDTO;
import auction.logic.RequestDTO.PullDTO;
import auction.logic.RequestDTO.BidDTO;
import stateManager.AuctionManager;
import auction.logic.model.Auction;
import auction.logic.model.Item;
import java.util.List;

public class AuctionService {
  public static Object addAuction(Item item, double startPrice, double minStep, int duration, String sellerId){
    try{
      ClientSocket clientSocket = ClientSocket.getInstance();
      AuctionDTO auctionDTO = new AuctionDTO(item.getId(), startPrice, minStep, duration,sellerId);
      clientSocket.send(auctionDTO);
      Object response = clientSocket.receive();

      return response;
    } catch (RuntimeException e) {
      return null;
    }
  }
  public static Object placeBid(String auctionId, String bidderId, double price) {
    try{
      ClientSocket clientSocket = ClientSocket.getInstance();
      BidDTO bidDTO = new BidDTO(auctionId, bidderId, price);
      clientSocket.send(bidDTO);
      Object response = clientSocket.receive();
      // nếu server trả về updated Auction, thì auctionManager sẽ update
      if (response instanceof Auction) {
        AuctionManager.getInstance().addOrUpdate((Auction) response);
      }
      return response;
    } catch (RuntimeException e) {
      e.printStackTrace();
      return null;
    }
  }
 public static Object pullAuction() {
    try{
      ClientSocket clientSocket = ClientSocket.getInstance();
      PullDTO pullDTO = new PullDTO();
      clientSocket.send(pullDTO);
      Object response = clientSocket.receive();
      if (response instanceof List){
        AuctionManager.getInstance().replaceAll((List) response);
      }
      return response;
    } catch (RuntimeException e) {
      e.printStackTrace();
      return null;

    }

 }
}
