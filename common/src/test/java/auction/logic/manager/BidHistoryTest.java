package auction.logic.manager;

import auction.logic.model.BidTransaction;
import auction.logic.model.Clients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

public class BidHistoryTest {

    private BidHistory bidHistory;
    private Clients bidder1;
    private Clients bidder2;

    @BeforeEach
    void setUp() {
        bidHistory = new BidHistory();
        bidder1 = new Clients("bidder01", "123", "USER");
        bidder2 = new Clients("bidder02", "123", "USER");
    }

    @Test
    @DisplayName("addingTransaction - Thêm giao dịch thành công")
    void addingTransaction_shouldAddTransaction() {
        bidHistory.addingTransaction(bidder1, 15000);

        List<BidTransaction> transactions = bidHistory.getTransactions();

        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getAmount()).isEqualTo(15000);
    }

    @Test
    @DisplayName("getTransactions - Trả về danh sách theo thứ tự thời gian tăng dần")
    void getTransactions_shouldReturnTransactionsSortedByTime() throws InterruptedException {
        bidHistory.addingTransaction(bidder1, 10000);
        Thread.sleep(5); // đảm bảo thời gian khác nhau
        bidHistory.addingTransaction(bidder2, 12000);
        Thread.sleep(5);
        bidHistory.addingTransaction(bidder1, 15000);

        List<BidTransaction> transactions = bidHistory.getTransactions();

        assertThat(transactions).hasSize(3);
        // Kiểm tra thứ tự thời gian
        assertThat(transactions.get(0).getAmount()).isEqualTo(10000);
        assertThat(transactions.get(1).getAmount()).isEqualTo(12000);
        assertThat(transactions.get(2).getAmount()).isEqualTo(15000);
    }

    @Test
    @DisplayName("getTransactions - Trả về danh sách rỗng khi chưa có giao dịch nào")
    void getTransactions_shouldReturnEmptyList_whenNoTransactions() {
        List<BidTransaction> transactions = bidHistory.getTransactions();

        assertThat(transactions).isEmpty();
    }

    @Test
    @DisplayName("addingTransaction - Có thể thêm nhiều giao dịch từ nhiều người")
    void addingTransaction_shouldHandleMultipleBidders() {
        bidHistory.addingTransaction(bidder1, 10000);
        bidHistory.addingTransaction(bidder2, 11000);
        bidHistory.addingTransaction(bidder1, 13000);

        List<BidTransaction> transactions = bidHistory.getTransactions();

        assertThat(transactions).hasSize(3);
    }
}