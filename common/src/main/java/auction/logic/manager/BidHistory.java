package auction.logic.manager;

import auction.logic.model.Auction;
import auction.logic.model.BidTransaction;
import auction.logic.model.Bidder;
import auction.logic.model.Clients;

import java.util.HashMap;

public class BidHistory {
    private HashMap<String, BidTransaction> bidHistory = new HashMap<>();

    public void addingTransaction(Clients bidder, double price){
        BidTransaction temp = new BidTransaction(bidder, price);
        bidHistory.put(temp.getId(),temp);
    }
    public void printBidTransaction(){
        for(String key : bidHistory.keySet()){
            System.out.println(key +  " " + bidHistory.get(key).getBidder().getUsername());
        }
    }

}
