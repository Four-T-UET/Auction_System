package model;


import enums.AuctionEvent;
import enums.AuctionStatus;
import enums.AuthenticationException;
import manager.AuctionStateManagement;
import manager.BidHistory;

import java.time.LocalDateTime;
import java.util.*;

public class Auction extends Entity {
    private AuctionStatus status;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private Item product;
    private double currentPrice;
    private double minimumStep;
    private Bidder currentWinner;

	private BidHistory bidHistory;
	private AuctionObservers auctionObservers;
    private final AuctionStateManagement auctionStateManagement;

	// Constructor
    public Auction(Item product, double currentPrice, double miniumStep){
		super();
		try{
			if(currentPrice < 0){
				throw new AuthenticationException("Giá tiến không hợp lệ. Vui lòng nhập lại");
			}
		}catch (AuthenticationException e){
			System.out.println(e.getMessage());
		}

		this.auctionObservers=new AuctionObservers();
		this.auctionStateManagement = new AuctionStateManagement();
        this.product=product;
        this.currentPrice=currentPrice;
        this.minimumStep=miniumStep;
        this.status= AuctionStatus.PENDING;
        this.startTime=LocalDateTime.now();
        this.finishTime=startTime.plusDays(1);//useless
    }
	// Hàm kiểm tra, set người chiến thắng hiện tại
    public boolean setCurrentWinner(Clients bidder, double price){

		synchronized (this){ // NGon vay, this ở đây chỉ cái auction đó
							// không Auction.class vì sẽ blocks ALL auction
			if (bidder == null || this.status != AuctionStatus.RUNNING){
				return false;
			}
			if (this.currentPrice + minimumStep <= price) {
				//update new winner
				this.currentWinner = bidder;
				this.currentPrice = price;
				this.addBidTransaction(bidder, price);

				// add observer
				auctionObservers.registerObserver(bidder);

				notifyObservers(AuctionEvent.PRICE_UPDATED, "Gia da duoc cap nhat: " + price);
				return true;
			} else {
				return false;
			}
		}
    }


	// Getter - Setter
	public double getMiniumStep(){
		return this.minimumStep;
	}
	public double getCurrentPrice(){
		return this.currentPrice;
	}
	public Bidder getCurrentWinner(){return this.currentWinner;}
	public AuctionStatus getStatus(){
		return status;
	}
	public synchronized void setStatus(AuctionStatus status){
		this.status = status;
	}


	public void notifyObservers(AuctionEvent event, String message){
		auctionObservers.sendNotification(this,event, message );
	}
	// Thêm lịch sử đã giao dịch
	public void addBidTransaction(Clients bidder, double price){
		bidHistory.addingTransaction(bidder, price);
	}
	public void printBidTransaction(){
		bidHistory.printBidTransaction();
	}
	// --------------------------------------------------------
	// Logic chuyển trạng thái của AUCTION --------------------
	public synchronized void startAuction(){
		auctionStateManagement.startAuction(this);
	}
	public synchronized void finishAuction(){
		auctionStateManagement.finishAuction(this);
	}
	public synchronized void payingAuction(){
		auctionStateManagement.payingAuction(this);
	}
	public synchronized void cancelAuction(){
		auctionStateManagement.cancelAuction(this);
	}
}