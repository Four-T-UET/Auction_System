package auction.logic.model;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Wallet extends Entity implements Serializable {
    private volatile double balance;
    private volatile double totalLockBalance;
    private ConcurrentHashMap<String,Double> lockList = new ConcurrentHashMap<>();
    public Wallet(){
        super();
        this.balance = 0;this.totalLockBalance = 0;
    }
    // Phương thức lấy/gán giá trị
    public void setBalance(double balance){this.balance = balance;}
    public void settotalLockBalance(double lockBalance){this.totalLockBalance = lockBalance;}
    public double getBalance(){return this.balance;}
    public double getLockBalance(){return this.totalLockBalance;}

    // Các phương thức
    public synchronized void deposit(double amount){
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        balance += amount;
    }
    public synchronized void lockWallet(Auction auction,double amount){
        double currentLocked = lockList.getOrDefault(auction.getId(), 0.0);
        double delta = amount - currentLocked;
        if (delta == 0) {
            return;
        }
        this.totalLockBalance += delta;
        this.balance -= delta;
        lockList.put(auction.getId(), amount);

    }

    public synchronized double getLockedAmount(String auctionId) {
        return lockList.getOrDefault(auctionId, 0.0);
    }

    public synchronized void releaseBalance(Auction auction){


            Double locked = lockList.remove(auction.getId());
            if (locked == null) {
                return;
            }
            this.balance += locked;
            this.totalLockBalance -= locked;



    }

    public synchronized void deductLockBalance(Auction auction){

            Double locked = lockList.remove(auction.getId());
            if (locked == null) {
                return;
            }

            this.totalLockBalance -= locked;

    }




}
