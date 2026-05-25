package auction.logic.model;


import auction.logic.enums.AuctionEvent;
import auction.logic.enums.AuthenticationException;
import auction.logic.enums.AuctionStatus;
import auction.logic.manager.AuctionStateManagement;
import auction.logic.manager.BidHistory;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.DoubleProperty;


public class Auction extends Entity implements Serializable {
	private static final long serialVersionUID = 1L;
	private AuctionStatus status;
	private LocalDateTime startTime;
	private LocalDateTime finishTime;
	private Item product;
	private double currentPrice;
	private double minimumStep;
	private Bidder currentWinner;
	private Clients seller;
//	private DoubleProperty currentPriceProperty;


	private BidHistory bidHistory = new BidHistory();
	private AuctionObservers auctionObservers;
	private final AuctionStateManagement auctionStateManagement = new AuctionStateManagement();

	// Constructor with duration in minutes
	public Auction(Item product, double currentPrice, double miniumStep,long durationMinutes){
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
//		this.currentPriceProperty = new SimpleDoubleProperty(currentPrice);
		this.minimumStep=miniumStep;
		this.status= AuctionStatus.PENDING;
		this.startTime= LocalDateTime.now();
		this.finishTime=LocalDateTime.now().plusMinutes(durationMinutes);
	}
	// Hàm kiểm tra, set người chiến thắng hiện tại
	public boolean setCurrentWinner(Bidder bidder, double price){

		synchronized (this){ // NGon vay, this ở đây chỉ cái auction đó
			// không Auction.class vì sẽ blocks ALL auction
			if (bidder == null || this.status != AuctionStatus.RUNNING){
				return false;
			}
			if (this.currentPrice + minimumStep <= price) {
				//update new winner
				this.currentWinner = bidder;
				this.currentPrice = price;
				this.addBidTransaction((Clients) bidder, price);

				// add observer
				auctionObservers.registerObserver((Clients) bidder);

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
	public Clients getCurrentWinner(){return (Clients)currentWinner;}
	public Clients getSeller(){return seller;}
	public AuctionStatus getStatus(){
		return status;
	}
	public synchronized void setStatus(AuctionStatus status){
		this.status = status;
	}
	public Item getItem(){return product; }
	public LocalDateTime getFinishTime(){
		return this.finishTime;
	}

	public LocalDateTime getStartTime(){
		return this.startTime;
	}

	public void setFinishTime(LocalDateTime finishTime){
		this.finishTime = finishTime;
	}
//	public DoubleProperty currentPriceProperty(){
//		return this.currentPriceProperty;
//	}

	public void notifyObservers(AuctionEvent event, String message){
		auctionObservers.sendNotification(this,event, message );
	}
	// Thêm lịch sử đã giao dịch
	public void addBidTransaction(Clients bidder, double price){
		this.bidHistory.addingTransaction(bidder, price);
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

	public void setCurrentWinnerSnapshot(Bidder bidder) {
		this.currentWinner = bidder;
	}

	public void setSellerSnapshot(Clients seller) {
		this.seller = seller;
	}
}