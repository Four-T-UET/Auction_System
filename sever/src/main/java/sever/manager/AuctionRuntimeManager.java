package sever.manager;

import auction.logic.model.Auction;
import java.util.concurrent.ConcurrentHashMap;
import sever.service.AuctionService;

public class AuctionRuntimeManager {
    private static AuctionRuntimeManager instance = new AuctionRuntimeManager();
    private final ConcurrentHashMap<String, Auction> auctionCache = new ConcurrentHashMap<>();
    private final AuctionService auctionService = new AuctionService();

    private AuctionRuntimeManager() {}

    public static AuctionRuntimeManager getInstance() {
        return instance;
    }

    public Auction getOrLoad(String auctionId) {
        if (auctionId == null || auctionId.isBlank()) {
            return null;
        }
        return auctionCache.computeIfAbsent(auctionId, id -> auctionService.getAuctionById(id));
    }
    /// ///////////////////////////////
    public Auction getFromCache(String auctionId) {
        if (auctionId == null || auctionId.isBlank()) {
            return null;
        }
        return auctionCache.get(auctionId);
    }
    /// ////////////////////////////

    public void addOrUpdate(Auction auction) {
        if (auction == null || auction.getId() == null) {
            return;
        }
        auctionCache.put(auction.getId(), auction);
    }
}

