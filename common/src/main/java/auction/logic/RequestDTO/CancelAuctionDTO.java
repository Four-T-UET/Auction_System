package auction.logic.RequestDTO;

import java.io.Serializable;

public class CancelAuctionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String auctionId;
    private String auctionName;
    private String currentWinnerId;

    public CancelAuctionDTO(String auctionName, String auctionId, String currentWinnerId) {
        this.auctionName = auctionName;
        this.auctionId = auctionId;
        this.currentWinnerId = currentWinnerId;
    }

    public String getCurrentWinnerId() {
        return currentWinnerId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getAuctionName() {
        return auctionName;
    }
}
