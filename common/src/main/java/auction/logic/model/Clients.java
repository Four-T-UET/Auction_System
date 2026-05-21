package auction.logic.model;


import auction.logic.enums.AuctionClosedException;
import auction.logic.enums.AuctionEvent;
import auction.logic.enums.AuctionStatus;
import auction.logic.enums.InvalidBidException;
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

    @Override
    public void removeItem(Item temp) {
        inventory.remove(temp.getId());
    }

    @Override
    public Auction addAuction(Item temp, double firstprice, double miniumStep,int durationDays) {
        return new Auction(temp, firstprice, miniumStep, durationDays);
    }


    @Override
    public void placeBid(Auction auction, double price) {
        try {
            if (auction.getStatus() != AuctionStatus.RUNNING) {
                throw new AuctionClosedException("Phiên đấu giá đang chờ ");
            }
            if (inventory.containsValue(auction.getItem()) ){
                System.out.println("Ban khong the dau gia san pham cua chinh minh");
                return;
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
    // observers pattern
    public void update(Auction auction, AuctionEvent eventType, String message){
        //print notification
        // logic wallet
        if (eventType== AuctionEvent.PRICE_UPDATED){
            //check lockbalance
            boolean isMoneyLocked = wallet.getLockBalance() > 0;
            //check winner
            boolean amIWinner = (auction.getCurrentWinner() == this);

            if (isMoneyLocked && !amIWinner){
                wallet.releaseBalance(auction);
            }
        }
    }

    // method với Wallet
    public Wallet getWallet(){
        return this.wallet;
    }
    public void deposit(double amount){
        wallet.deposit(amount);
    }
    public void releaseBalance(Auction auction) {
        wallet.releaseBalance(auction);
    }
    public void deductLockbalance(Auction auction) {
        wallet.deductLockBalance(auction);
    }


    public String getUsername(){
        return super.getUsername();
    }
}