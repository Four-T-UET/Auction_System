package auction.logic.model;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Wallet extends Entity implements Serializable {
    private volatile double balance;
    private double totalLockBalance;
    private ReentrantLock lock=new ReentrantLock();
    private ConcurrentHashMap<String,Double> lockList=new ConcurrentHashMap<>();
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
        balance += amount;
    }
    public synchronized void lockWallet(Auction auction,double amount){
        this.totalLockBalance=this.totalLockBalance-lockList.getOrDefault(auction.getId(),0.0)+amount;
        lockList.put(auction.getId(), amount);
        this.balance = this.balance - totalLockBalance;

    }

    public void releaseBalance(Auction auction){
        lock.lock();
        try {
            this.balance += lockList.get(auction.getId());
            this.totalLockBalance -= lockList.get(auction.getId());
            lockList.remove(auction.getId());

        }finally {
            lock.unlock();
        }

    }

    public void deductLockBalance(Auction auction){
        lock.lock();
        try {
            this.balance -= lockList.get(auction.getId());
            this.totalLockBalance -= lockList.get(auction.getId());
            lockList.remove(auction.getId());
        }finally{
            lock.unlock();
        }
    }
    public void withdraw(double money) {
        this.balance -= money;
    }

}
