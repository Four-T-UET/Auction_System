package auction.logic.model;

import java.time.LocalDateTime;
public class BidTransaction extends Entity {
    private final Clients bidder;
    private final double amount;
    private LocalDateTime time;
    public BidTransaction(Clients bidder, double amount){
        super();
        this.bidder=bidder;
        this.amount=amount;
        this.time=LocalDateTime.now();
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTime() {
        return time;
    }



}
	