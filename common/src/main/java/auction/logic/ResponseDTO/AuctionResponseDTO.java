package auction.logic.ResponseDTO;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AuctionResponseDTO implements Serializable {
    public String auctionId;
    public String itemId;
    public double startPrice;
    public double minStep;
    public long duration;
    public LocalDateTime finishTime;
    public String status;
    public String currentWinnerId;
    public String sellerId;
    public String itemName;
    public String itemDescription;
    public String itemCategoryStr;
    public byte[] itemImageByte;

    public AuctionResponseDTO(String auctionId, String itemId, double startPrice, double minStep, long duration,
                              LocalDateTime finishTime, String status, String currentWinnerId, String sellerId,
                              String itemName, String itemDescription, String itemCategoryStr, byte[] itemImageByte) {
        this.auctionId = auctionId;
        this.itemId = itemId;
        this.startPrice = startPrice;
        this.minStep = minStep;
        this.duration = duration;
        this.finishTime = finishTime;
        this.status = status;
        this.currentWinnerId = currentWinnerId;
        this.sellerId = sellerId;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemCategoryStr = itemCategoryStr;
        this.itemImageByte = itemImageByte;
    }
}
