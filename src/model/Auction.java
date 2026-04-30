package model;


import enums.AuctionEvent;
import enums.AuctionStatus;
import enums.AuthenticationException;
import manager.AuctionManager;

import java.time.LocalDateTime;
import java.util.*;

public class Auction extends Entity {
    private AuctionStatus status;
    private Item product;
    private double currentPrice;
    private double minimumStep;
    private Bidder currentWinner;
	private AuctionObservers auctionObservers;
    private HashMap<String,BidTransaction> BidHistory = new HashMap<>();
	// Constructor
    public Auction(Item product, double currentPrice, double miniumStep){
		try{
			if(currentPrice < 0){
				throw new AuthenticationException("Giá tiến không hợp lệ. Vui lòng nhập lại");
			}
		}catch (AuthenticationException e){
			System.out.println(e.getMessage());
		}


        super();
		this.auctionObservers=new AuctionObservers();
        this.product=product;
        this.currentPrice=currentPrice;
        this.minimumStep=miniumStep;
        this.status= AuctionStatus.RUNNING;
		AuctionManager.getInstance().add(this);
//useless
    }
	public void notifyObservers(AuctionEvent event, String message){
		auctionObservers.sendNotification(this,event, message );
	}
	// Hàm kiểm tra, set người chiến thắng hiện tại
    public boolean setCurrentWinner(Bidder bidder, double price){

		synchronized (this){ // NGon vay, this ở đây chỉ cái auction đó
							// không Auction.class vì sẽ blocks ALL auction
			if (bidder == null || this.status != AuctionStatus.RUNNING){
				return false;
			}
			if (this.currentPrice + minimumStep <= price){
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

	// Thêm lịch sử đã giao dịch
    public void addBidTransaction(Bidder bidder, double price){
        BidTransaction temp = new BidTransaction(bidder,price);
        BidHistory.put(temp.getId(),temp);
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

	// --------------------------------------------------------
	// Logic chuyển trạng thái của AUCTION --------------------
	public synchronized void startAuction(){
		if(this.status == AuctionStatus.PENDING){
			this.status = AuctionStatus.RUNNING;
			System.out.println("Bắt đầu phiên đấu giá");
		}
	}
	public synchronized void finishAuction(){
		if(this.status == AuctionStatus.RUNNING){
			this.status = AuctionStatus.FINISHED;
			this.payingAuction();
			System.out.println("Hoàn thành phiên đấu giá");
		}
	}
	public synchronized void payingAuction(){
		if (this.status != AuctionStatus.FINISHED){
			System.out.println("Phiên đấu giá không thể thanh toán");
			return;
		}
		if (this.currentWinner == null || this.currentWinner.getWallet() == null){
			System.out.println("Không có người thắng để thanh toán");
			return;
		}

		this.currentWinner.deductLockbalance(this);
		this.status = AuctionStatus.PAID;
		System.out.println("Phiên đấu giá đã được thanh toán");
	}

	public synchronized void cancelAuction(){
		if(this.status != AuctionStatus.PAID && this.status != AuctionStatus.CANCELLED){
			this.status = AuctionStatus.CANCELLED;
			notifyObservers(AuctionEvent.AUCTION_CANCELLED,"Phiên đấu giá đã bị huỷ");
			try{
				this.currentWinner.releaseBalance(this);

			} catch (NullPointerException e) {
				System.out.println("");
			}
			System.out.println("Phiên đâu giá đã bị huỷ");
		}else{
			System.out.println("Không thể huỷ phiên đấu giá");
		}
	}
}
