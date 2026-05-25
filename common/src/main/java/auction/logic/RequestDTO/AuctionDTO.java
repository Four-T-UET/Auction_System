package auction.logic.RequestDTO;

import java.io.Serializable;

public class AuctionDTO implements Serializable {
  private String itemId;
  private double startValue;
  private double minStep;
  private int duration;
  private String sellerId;

  public AuctionDTO (String itemId, double startValue, double minStep, int duration, String sellerId){
    this.itemId = itemId;
    this.startValue = startValue;
    this.minStep = minStep;
    this.duration = duration;
    this.sellerId = sellerId;
  }

  public String getId(){
    return this.itemId;
  }

  public double getStartValue(){
    return this.startValue;
  }

  public double getMinStep(){
    return this.minStep;
  }

  public int getDuration(){
    return this.duration;
  }

  public String getSellerId() {
    return this.sellerId;
  }
}
