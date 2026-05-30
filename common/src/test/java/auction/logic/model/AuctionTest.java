package auction.logic.model;

import auction.logic.enums.AuctionStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class AuctionTest {

    private Auction auction;
    private Clients seller;
    private Clients bidder1;
    private Clients bidder2;
    private Item item;

    @BeforeEach
    void setUp() {
        // Tạo người bán
        seller = new Clients("seller01", "123456", "USER");

        // Tạo 2 người mua
        bidder1 = new Clients("bidder01", "123456", "USER");
        bidder2 = new Clients("bidder02", "123456", "USER");

        // Nạp tiền cho các bidder để có thể đặt giá
        bidder1.deposit(50000);
        bidder2.deposit(50000);

        // Tạo item mẫu
        item = new Electronics("Laptop Gaming", "Laptop mạnh", null);

        // Tạo phiên đấu giá: giá khởi điểm 10000, bước giá 500, thời gian 60 phút
        auction = new Auction(item, 10000, 500, 60);

        // Bắt buộc phải start auction thì mới cho phép đặt giá
        auction.startAuction();
    }

    //  TEST BID THÀNH CÔNG

    @Test
    @DisplayName("setCurrentWinner() - Đặt giá thành công khi đủ điều kiện")
    void setCurrentWinner_shouldSucceed_whenBidIsValid() {
        boolean result = auction.setCurrentWinner(bidder1, 12000);

        assertThat(result).isTrue();
        assertThat(auction.getCurrentPrice()).isEqualTo(12000);
        assertThat(auction.getCurrentWinner()).isEqualTo(bidder1);
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.RUNNING);
    }

    @Test
    @DisplayName("setCurrentWinner() - Bid đúng mức tối thiểu (current + minStep)")
    void setCurrentWinner_shouldSucceed_whenBidExactlyMinStep() {
        // Giá khởi điểm 10000 + bước giá 500 = 10500
        boolean result = auction.setCurrentWinner(bidder1, 10500);

        assertThat(result).isTrue();
        assertThat(auction.getCurrentPrice()).isEqualTo(10500);
    }

    //  TEST BID KO THÀNH CÔNG

    @Test
    @DisplayName("setCurrentWinner() - Bid thất bại khi giá thấp hơn bước giá tối thiểu")
    void setCurrentWinner_shouldFail_whenBidBelowMinimumStep() {
        // 10000 + 500 = 10500. Đặt 10400 là sai
        boolean result = auction.setCurrentWinner(bidder1, 10400);

        assertThat(result).isFalse();
        assertThat(auction.getCurrentPrice()).isEqualTo(10000); // Giá không đổi
        assertThat(auction.getCurrentWinner()).isNull();
    }

    @Test
    @DisplayName("setCurrentWinner() - Bid thất bại khi auction chưa bắt đầu (PENDING)")
    void setCurrentWinner_shouldFail_whenAuctionIsNotRunning() {
        // Tạo auction mới nhưng ko start
        Auction pendingAuction = new Auction(item, 10000, 500, 60);
        boolean result = pendingAuction.setCurrentWinner(bidder1, 12000);
        assertThat(result).isFalse();
    }

    //  TEST OUTBID

    @Test
    @DisplayName("setCurrentWinner() - Khi có người bid cao hơn, người cũ phải được release tiền")
    void setCurrentWinner_whenOutbid_shouldReleasePreviousWinnerMoney() {
        // Dùng placeBid để trigger logic khóa tiền thật
        bidder1.placeBid(auction, 25000);                    // bidder1 bid 25000
        double bidder1BalanceAfterFirstBid = bidder1.getWallet().getBalance();

        bidder2.placeBid(auction, 30000);                    // bidder2 vượt giá

        // Kiểm tra bidder1 được trả lại tiền
        assertThat(bidder1.getWallet().getBalance())
                .isEqualTo(bidder1BalanceAfterFirstBid + 25000);

        assertThat(auction.getCurrentWinner()).isEqualTo(bidder2);
        assertThat(auction.getCurrentPrice()).isEqualTo(30000);
    }

}