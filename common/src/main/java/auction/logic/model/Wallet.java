package auction.logic.model;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Wallet extends Entity implements Serializable {
    private volatile double balance;
    private double totalLockBalance;
    private ReentrantLock lock = new ReentrantLock();
    private ConcurrentHashMap<String,Double> lockList = new ConcurrentHashMap<>();
    public Wallet(){
        super();
        this.balance = 0;this.totalLockBalance = 0;
    }
    //Getter/Setter
    public void setBalance(double balance){this.balance = balance;}
    public void settotalLockBalance(double lockBalance){this.totalLockBalance = lockBalance;}
    public double getBalance(){return this.balance;}
    public double getLockBalance(){return this.totalLockBalance;}

    //Methods
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

    public void releaseBalance(Auction auction){
        lock.lock();
        try {
            Double locked = lockList.remove(auction.getId());
            if (locked == null) {
                return;
            }
            this.balance += locked;
            this.totalLockBalance -= locked;

        }finally {
            lock.unlock();
        }

    }

    public void deductLockBalance(Auction auction){
        lock.lock();
        try {
            Double locked = lockList.remove(auction.getId());
            if (locked == null) {
                return;
            }
            this.balance -= locked;
            this.totalLockBalance -= locked;
        }finally{
            lock.unlock();
        }
    }

    public void deductLockedAmount(String auctionId, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        lock.lock();
        try {
            Double locked = lockList.get(auctionId);
            if (locked != null) {
                double remaining = locked - amount;
                if (remaining <= 0) {
                    lockList.remove(auctionId);
                } else {
                    lockList.put(auctionId, remaining);
                }
            }
            this.totalLockBalance -= amount;
        } finally {
            lock.unlock();
        }
    }
    public void withdraw(double money) {
        if (money <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.balance < money) {
            throw new IllegalStateException("Insufficient balance");
        }
        this.balance -= money;
    }

}
