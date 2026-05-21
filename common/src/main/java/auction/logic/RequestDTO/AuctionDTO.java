package auction.logic.RequestDTO;

import java.io.Serializable;

public class AuctionDTO implements Serializable {
  private String itemId;
  private double startValue;
  private double minStep;
  private int duration;

  public AuctionDTO (String itemId, double startValue, double minStep, int duration){
    this.itemId = itemId;
    this.startValue = startValue;
    this.minStep = minStep;
    this.duration = duration;
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




}
