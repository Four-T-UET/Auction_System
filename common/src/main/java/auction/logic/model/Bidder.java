package auction.logic.model;

public interface Bidder {
    void placeBid(Auction a, double price);
    Wallet getWallet();
    void releaseBalance(Auction auction);
}

