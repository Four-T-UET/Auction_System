package model;

import enums.AuctionClosedException;
import enums.AuctionEvent;
import enums.AuctionStatus;
import enums.InvalidBidException;
import java.util.HashMap;

public class Clients extends User implements Bidder, Seller{
    private HashMap<String, Item> inventory = new HashMap<>();
    private Wallet wallet = new Wallet();

    public Clients(String username, String password){
        super(username, password);
    }
    @Override
    public synchronized boolean login(String name, String pass) {
        return super.login(name, pass);
    }
    public Clients register(String id, String name, String pass){
        return new Clients(name, pass);
    }

    // method nạp tiền
    public void deposit(double amount){
        wallet.deposit(amount);
    }

    @Override
    public void addItem(Item temp) {inventory.put(temp.getId(), temp);
    }
    @Override
    public void removeItem(Item temp) {
        inventory.remove(temp.getId());
    }

    @Override
    public Auction addAuction(Item temp, double firstprice, double miniumStep) {
        return new Auction(temp, firstprice, miniumStep);
    }

    @Override
    public void placeBid(Auction auction, double price) {
        try {
            if (auction.getStatus() != AuctionStatus.RUNNING) {
                throw new AuctionClosedException("Phiên đấu giá đang chờ ");
            }
        }catch(AuctionClosedException e){
            System.out.println(e.getMessage());
            return;
        }

        //check money
        try {
            if (wallet.getBalance() + wallet.getLockBalance() < price) {
                throw new InvalidBidException("Khong du tien trong tai khoan");
            }
        }catch(InvalidBidException e){
            System.out.println(e.getMessage());
            return;
        }

        boolean isBidValid = auction.setCurrentWinner(this, price);
        //Lockmoney
        if (isBidValid) {
            //lockbalance
            wallet.lockWallet(auction,price);
        } else {

        }
    }

    public void update(Auction auction, AuctionEvent eventType, String message){
        //print notification
        // logic wallet
        if (eventType== AuctionEvent.PRICE_UPDATED){
            //check lockbalance
            boolean isMoneyLocked = this.wallet.getLockBalance() > 0;
            //check winner
            boolean amIWinner = (auction.getCurrentWinner() == this);

            if (isMoneyLocked && !amIWinner){
                this.wallet.releaseBalance(auction);
            }
        }


    }


    public Wallet getWallet(){
        return this.wallet;
    }
    @Override
    public void releaseBalance(Auction auction) {
        wallet.releaseBalance(auction);
    }

    @Override
    public void deductLockbalance(Auction auction) {
        wallet.deductLockBalance(auction);
    }
    //    @Override
//    public void setAutoBid(model.Auction , double maxBid, double increment) {
//
//    }
}
