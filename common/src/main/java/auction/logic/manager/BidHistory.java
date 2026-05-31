package auction.logic.manager;

import auction.logic.model.Auction;
import auction.logic.model.BidTransaction;
import auction.logic.model.Bidder;
import auction.logic.model.Clients;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class BidHistory implements Serializable {
    private HashMap<String, BidTransaction> bidHistory = new HashMap<>();

    public void addingTransaction(Clients bidder, double price){
        BidTransaction temp = new BidTransaction(bidder, price);
        bidHistory.put(temp.getId(),temp);
    }


    public List<BidTransaction> getTransactions(){
        List<BidTransaction> list = new ArrayList<>(bidHistory.values());
        list.sort(Comparator.comparing(BidTransaction::getTime));
        return list;
    }
}
