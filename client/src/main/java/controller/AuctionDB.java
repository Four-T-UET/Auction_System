package controller;


import auction.logic.model.Auction;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AuctionDB {
    private static final List<Auction> AUCTIONS = new CopyOnWriteArrayList<>();

    private AuctionDB() {
    }

    public static boolean insertAuction(Auction auction) {
        if (auction == null) {
            return false;
        }
        AUCTIONS.add(auction);
        return true;
    }

    public static List<Auction> getAuctions() {
        return List.copyOf(AUCTIONS);
    }
}

