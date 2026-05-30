package auction.logic.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
public class WalletTest {

    private Wallet wallet;
    private Auction dummyAuction;   // Auction giả để dùng cho việc lock tiền

    @BeforeEach
    void setUp() {
        wallet = new Wallet();
        // Tạo một Auction giả để test
        // Chúng ta không cần item thật ở đây
        dummyAuction = new Auction(
                new Electronics("Test Item", "For testing", null),
                1000,// start price
                50,     // min step
                60      // duration
        );
    }

    //  TEST DEPOSIT

    @Test
    @DisplayName("deposit() - Nạp tiền thành công")
    void deposit_shouldIncreaseBalance() {
        // Arrange + Act
        wallet.deposit(5000);

        // Assert
        assertThat(wallet.getBalance()).isEqualTo(5000);
        assertThat(wallet.getLockBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("deposit() - Nạp tiền âm phải ném ra ngoại lệ")
    void deposit_negativeAmount_shouldThrowException() {
        assertThatThrownBy(() -> wallet.deposit(-100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }

    @Test
    @DisplayName("deposit() - Nạp số tiền bằng 0 phải ném ngoại lệ")
    void deposit_zeroAmount_shouldThrowException() {
        assertThatThrownBy(() -> wallet.deposit(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    //  TEST LOCK WALLET

    @Test
    @DisplayName("lockWallet() - Khóa tiền thành công khi có đủ số dư")
    void lockWallet_shouldLockMoneyCorrectly() {
        wallet.deposit(10000);

        wallet.lockWallet(dummyAuction, 3000);

        assertThat(wallet.getBalance()).isEqualTo(7000);           // Số dư giảm
        assertThat(wallet.getLockBalance()).isEqualTo(3000);       // Số tiền bị khóa
        assertThat(wallet.getLockedAmount(dummyAuction.getId())).isEqualTo(3000);
    }

    @Test
    @DisplayName("lockWallet() - Lock nhiều lần cùng 1 auction sẽ cập nhật đúng")
    void lockWallet_multipleTimes_shouldUpdateCorrectly() {
        wallet.deposit(10000);

        wallet.lockWallet(dummyAuction, 2000);
        wallet.lockWallet(dummyAuction, 5000);   // Tăng lên

        assertThat(wallet.getBalance()).isEqualTo(5000);
        assertThat(wallet.getLockBalance()).isEqualTo(5000);
        assertThat(wallet.getLockedAmount(dummyAuction.getId())).isEqualTo(5000);
    }

    //  TEST RELEASE BALANCE

    @Test
    @DisplayName("releaseBalance() - Trả lại tiền khi bị outbid")
    void releaseBalance_shouldReturnLockedMoney() {
        wallet.deposit(10000);
        wallet.lockWallet(dummyAuction, 4000);

        wallet.releaseBalance(dummyAuction);

        assertThat(wallet.getBalance()).isEqualTo(10000);     // Đã được trả lại
        assertThat(wallet.getLockBalance()).isEqualTo(0);
        assertThat(wallet.getLockedAmount(dummyAuction.getId())).isEqualTo(0);
    }

    @Test
    @DisplayName("releaseBalance() - Gọi nhiều lần không bị lỗi")
    void releaseBalance_calledMultipleTimes_shouldNotFail() {
        wallet.deposit(5000);
        wallet.lockWallet(dummyAuction, 2000);

        wallet.releaseBalance(dummyAuction);
        wallet.releaseBalance(dummyAuction); // Gọi lần 2

        assertThat(wallet.getBalance()).isEqualTo(5000);
    }

    //  TEST DEDUCT LOCK BALANCE

    @Test
    @DisplayName("deductLockBalance() - Trừ tiền khi thắng đấu giá")
    void deductLockBalance_shouldDeductLockedMoney() {
        wallet.deposit(10000);
        wallet.lockWallet(dummyAuction, 6000);

        wallet.deductLockBalance(dummyAuction);

        assertThat(wallet.getBalance()).isEqualTo(4000);      // Số dư còn lại
        assertThat(wallet.getLockBalance()).isEqualTo(0);     // Đã trừ hết
    }

    //  TEST GET LOCKED AMOUNT

    @Test
    @DisplayName("getLockedAmount() - Trả về 0 khi auction chưa lock")
    void getLockedAmount_nonExistentAuction_shouldReturnZero() {
        double amount = wallet.getLockedAmount("auction-not-exist");

        assertThat(amount).isEqualTo(0);
    }
}