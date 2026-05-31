package auction.logic.RequestDTO;

import java.io.Serializable;

public class BidDTO implements Serializable {
    private final String auctionId;
    private final String bidderId;
    private final double price;

    public BidDTO(String auctionId, String bidderId, double price) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.price = price;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public double getPrice() {
        return price;
    }
}

