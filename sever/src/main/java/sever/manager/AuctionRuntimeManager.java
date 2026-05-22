package sever.manager;

import auction.logic.model.Auction;
import java.util.concurrent.ConcurrentHashMap;
import sever.service.AuctionService;

public class AuctionRuntimeManager {
    private static AuctionRuntimeManager instance;
    private final ConcurrentHashMap<String, Auction> auctionCache = new ConcurrentHashMap<>();
    private final AuctionService auctionService = new AuctionService();

    private AuctionRuntimeManager() {}

    public static AuctionRuntimeManager getInstance() {
        if (instance == null) {
            instance = new AuctionRuntimeManager();
        }
        return instance;
    }

    public Auction getOrLoad(String auctionId) {
        if (auctionId == null || auctionId.isBlank()) {
            return null;
        }
        return auctionCache.computeIfAbsent(auctionId, id -> auctionService.getAuctionById(id));
    }

    public void addOrUpdate(Auction auction) {
        if (auction == null || auction.getId() == null) {
            return;
        }
        auctionCache.put(auction.getId(), auction);
    }
}

