package auction.logic.model;


import auction.logic.enums.AuctionClosedException;
import auction.logic.enums.AuctionStatus;
import auction.logic.enums.InvalidBidException;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Clients extends User implements Bidder, Seller{
    private static final Logger LOGGER = LoggerFactory.getLogger(Clients.class);
    private HashMap<String, Item> inventory = new HashMap<>();
    private Wallet wallet = new Wallet();

    public Clients(String username, String password, String userRole){
        super(username, password, userRole);
    }
    @Override
    public synchronized boolean login(String name, String pass) {
        return super.login(name, pass);
    }


    @Override
    public void removeItem(Item temp) {
        inventory.remove(temp.getId());
    }

    @Override
    public Auction addAuction(Item temp, double firstprice, double miniumStep,int durationDays) {
        Auction auction =  new Auction(temp, firstprice, miniumStep, durationDays);
        auction.setSellerSnapshot(this);
        return auction;
    }


    @Override
    public void placeBid(Auction auction, double price) {
        try {
            if (auction.getStatus() != AuctionStatus.RUNNING) {
                throw new AuctionClosedException("Phiên đấu giá đang chờ ");
            }
            if (inventory.containsValue(auction.getItem()) ){
                LOGGER.warn("Ban khong the dau gia san pham cua chinh minh");
                return;
            }
        }catch(AuctionClosedException e){
            LOGGER.warn(e.getMessage(), e);
            return;
        }

        // Kiểm tra số tiền
        try {
            if (wallet.getBalance() + wallet.getLockedAmount(auction.getId()) < price ) {
                throw new InvalidBidException("Khong du tien trong tai khoan");
            }
        }catch(InvalidBidException e){
            LOGGER.warn(e.getMessage(), e);
            return;
        }

        boolean isBidValid = auction.setCurrentWinner(this, price);
        // Khóa tiền
        if (isBidValid) {
            // Khóa số dư
            wallet.lockWallet(auction,price);
        } else {

        }
    }
    // Phương thức liên quan đến Wallet
    public Wallet getWallet(){
        return this.wallet;
    }
    public void releaseBalance(Auction auction) {
        wallet.releaseBalance(auction);
    }
    public void deposit(double amount){
        wallet.deposit(amount);
    }
    public String getUsername(){
        return super.getUsername();
    }
}