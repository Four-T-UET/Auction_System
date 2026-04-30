package model;

import enums.AuctionEvent;

public interface Bidder {
    void placeBid(Auction a, double price);
    void update(Auction a, AuctionEvent b, String message);
    Wallet getWallet();
    void releaseBalance(Auction auction);
    void deductLockbalance(Auction auction);

//    void setAutoBid(model.Auction a, double maxBid, double increment);
}

