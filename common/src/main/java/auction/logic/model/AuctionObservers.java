package auction.logic.model;
import auction.logic.enums.AuctionEvent;

import java.io.Serializable;
import java.util.HashSet;
public class AuctionObservers implements Serializable {
    private HashSet<Bidder> observers = new HashSet<>();
    public synchronized void registerObserver(Bidder bidder) {
        observers.add(bidder);
    }
    public void sendNotification(Auction auction, AuctionEvent event, String message){
        for (Bidder observer: observers){
            observer.update(auction,event,message);
        }
    }
}
