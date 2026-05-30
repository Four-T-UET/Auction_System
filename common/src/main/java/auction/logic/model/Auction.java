package auction.logic.model;


import auction.logic.enums.AuthenticationException;
import auction.logic.enums.AuctionStatus;
import auction.logic.manager.AuctionStateManagement;
import auction.logic.manager.BidHistory;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Auction extends Entity implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Auction.class);

	private static final long serialVersionUID = 1L;
	private AuctionStatus status;
	private LocalDateTime startTime;
	private LocalDateTime finishTime;
	private Item product;
	private double currentPrice;
	private double minimumStep;
	private Bidder currentWinner;
	private Clients seller;

	private BidHistory bidHistory = new BidHistory();
	private final AuctionStateManagement auctionStateManagement = new AuctionStateManagement();

	// Hàm khởi tạo với thời gian kéo dài tính bằng phút
	public Auction(Item product, double currentPrice, double miniumStep,long durationMinutes){
		try{
			if(currentPrice < 0){
				throw new AuthenticationException("Giá tiến không hợp lệ. Vui lòng nhập lại");
			}
		}catch (AuthenticationException e){
			LOGGER.warn(e.getMessage(), e);
		}


		super();
		this.product=product;
		this.currentPrice=currentPrice;
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
				// Hoàn tiền cho người thắng cũ TRƯỚC KHI set winner mới
				if (this.currentWinner != null) {
					this.currentWinner.releaseBalance(this);
				}
				
				// Đặt winner mới
				this.currentWinner = bidder;
				this.currentPrice = price;
				this.addBidTransaction((Clients) bidder, price);
				
				return true;
			} else {
				return false;
			}
		}
	}


	// Phương thức lấy - gán giá trị
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


	// Thêm lịch sử đã giao dịch
	public void addBidTransaction(Clients bidder, double price){
		this.bidHistory.addingTransaction(bidder, price);
	}

	// --------------------------------------------------------
	// Logic chuyển trạng thái của AUCTION --------------------
	public synchronized void startAuction(){
		auctionStateManagement.startAuction(this);
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
	public BidHistory getBidHistory(){
		return bidHistory;
	}
}