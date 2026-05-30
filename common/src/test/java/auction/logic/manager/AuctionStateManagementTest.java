package auction.logic.manager;
import auction.logic.enums.AuctionStatus;
import auction.logic.model.Auction;
import auction.logic.model.Clients;
import auction.logic.model.Electronics;
import auction.logic.model.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
public class AuctionStateManagementTest {
    private AuctionStateManagement stateManager;
    private Auction auction;
    private Clients seller;
    private Clients winner;

    @BeforeEach
    void setUp() {
        stateManager = new AuctionStateManagement();

        seller = new Clients("seller01", "123", "USER");
        winner = new Clients("winner01", "123", "USER");
        winner.deposit(50000);

        Item item = new Electronics("Test Item", "For state test", null);
        auction = new Auction(item, 10000, 500, 60);

        // Gán seller cho auction
        auction.setSellerSnapshot(seller);
    }

    //  TEST START AUCTION
    @Test
    @DisplayName("startAuction() - Chuyển từ PENDING sang RUNNING")
    void startAuction_shouldChangeStatusToRunning() {
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.PENDING);

        stateManager.startAuction(auction);

        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.RUNNING);
    }
    @Test
    @DisplayName("startAuction() - Không chuyển trạng thái nếu không phải PENDING")
    void startAuction_shouldNotChange_ifNotPending() {
        auction.startAuction(); // Đã thành RUNNING
        stateManager.startAuction(auction);

        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.RUNNING);
    }
    //  TEST CANCEL AUCTION
    @Test
    @DisplayName("cancelAuction() - Hủy auction và trả tiền cho winner (nếu có)")
    void cancelAuction_shouldReleaseMoneyToWinner() {
        auction.startAuction();
        // Winner đặt giá và bị lock tiền
        winner.placeBid(auction, 15000);  // lock 15000
        double balanceBeforeCancel = winner.getWallet().getBalance();
        stateManager.cancelAuction(auction);
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.CANCELLED);
        // Tiền phải được trả lại
        assertThat(winner.getWallet().getBalance()).isEqualTo(balanceBeforeCancel + 15000);
    }
    @Test
    @DisplayName("cancelAuction() - Không hủy được nếu đã PAID hoặc CANCELLED")
    void cancelAuction_shouldNotCancel_ifAlreadyPaidOrCancelled() {
        auction.startAuction();
        auction.setStatus(AuctionStatus.PAID);
        stateManager.cancelAuction(auction);
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.PAID); // Giữ nguyên
    }
}