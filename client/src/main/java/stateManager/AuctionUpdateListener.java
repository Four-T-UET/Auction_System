package stateManager;

import auction.logic.model.Auction;
import java.util.List;

public interface AuctionUpdateListener {
    void onAuctionAdded(Auction auction);
    void onAuctionUpdated(Auction auction);
    void onAuctionsReplaced(List<Auction> auctions);
    void onAuctionRemoved(String auctionId);
}

