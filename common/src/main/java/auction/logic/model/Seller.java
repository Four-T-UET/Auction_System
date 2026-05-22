package auction.logic.model;

public interface Seller {
    void removeItem(Item temp);

    Auction addAuction(Item temp, double firstprice, double miniumStep, int duration);
}
