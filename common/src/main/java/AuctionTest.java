//
//
//import auction.logic.enums.AuctionStatus;
//import auction.logic.model.Art;
//import auction.logic.model.Auction;
//import auction.logic.model.Clients;
//import auction.logic.model.Item;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import org.testng.annotations.Test;
//
//public class AuctionTest {
//
//    @Test
//    void validBid_shouldUpdateWinnerAndPrice() {
//        Clients seller = new Clients("s1", "p1");
//        Clients bidder = new Clients("b1", "p2");
//        bidder.deposit(5000);
//
//        Item item = new Art("iphone", "apple");
//        Auction auction = seller.addAuction(item, 1000, 50);
//        auction.startAuction();
//
//        boolean ok = auction.setCurrentWinner(bidder, 1050);
//
//        assertTrue(ok);
//        assertEquals(1050, auction.getCurrentPrice());
//        assertEquals(bidder, auction.getCurrentWinner());
//    }
//
//    @Test
//    void invalidBid_shouldNotUpdateWinnerAndPrice() {
//        Clients seller = new Clients("s1", "p1");
//        Clients bidder = new Clients("b1", "p2");
//
//        Item item = new Art("iphone", "apple");
//        Auction auction = seller.addAuction(item, 1000, 50);
//        auction.startAuction();
//        auction.setCurrentWinner(bidder, 1050);
//
//        boolean ok = auction.setCurrentWinner(bidder, 1070); // < 1050 + 50
//
//        assertFalse(ok);
//        assertEquals(1050, auction.getCurrentPrice());
//    }
//
//    @Test
//    void finishAuction_shouldChangeStatusToFinished() {
//        Clients seller = new Clients("s1", "p1");
//        Item item = new Art("iphone", "apple");
//        Auction auction = seller.addAuction(item, 1000, 50);
//
//        auction.startAuction();
//        auction.finishAuction();
//
//        assertEquals(AuctionStatus.FINISHED, auction.getStatus());
//    }
//
//    @Test
//    void mixed_placeBidThenDirectSetCurrentWinner_shouldUpdateWinnerAndRefundOldBidder() {
//        Clients seller = new Clients("seller", "p");
//        Clients bidderA = new Clients("A", "p");
//        Clients bidderB = new Clients("B", "p");
//        bidderA.deposit(3000);
//        bidderB.deposit(3000);
//
//        Item item = new Art("Painting", "Artist");
//        Auction auction = seller.addAuction(item, 1000, 50);
//        auction.startAuction();
//
//        bidderA.placeBid(auction, 1050);
//        boolean ok = auction.setCurrentWinner(bidderB, 1150);
//
//        assertTrue(ok);
//        assertEquals(1150, auction.getCurrentPrice());
//        assertEquals(bidderB, auction.getCurrentWinner());
//
//        // bidderA placed via placeBid so funds are locked then released when outbid.
//        assertEquals(0, bidderA.getWallet().getLockBalance());
//        assertEquals(3000, bidderA.getWallet().getBalance());
//    }
//
//    @Test
//    void mixed_directBidThenPlaceBid_shouldKeepLatestValidBidAndLockWinnerFunds() {
//        Clients seller = new Clients("seller", "p");
//        Clients bidderA = new Clients("A", "p");
//        Clients bidderB = new Clients("B", "p");
//        bidderB.deposit(5000);
//
//        Item item = new Art("Camera", "Canon");
//        Auction auction = seller.addAuction(item, 1000, 50);
//        auction.startAuction();
//
//        assertTrue(auction.setCurrentWinner(bidderA, 1050));
//        bidderB.placeBid(auction, 1200);
//
//        assertEquals(1200, auction.getCurrentPrice());
//        assertEquals(bidderB, auction.getCurrentWinner());
//        assertEquals(1200, bidderB.getWallet().getLockBalance());
//        assertEquals(3800, bidderB.getWallet().getBalance());
//    }
//
//    @Test
//    void concurrentPlaceBid_highestBidShouldWin_withoutLostUpdate() throws Exception {
//        Clients seller = new Clients("seller", "p");
//        Clients lowBidder = new Clients("low", "p");
//        Clients highBidder = new Clients("high", "p");
//        lowBidder.deposit(10000);
//        highBidder.deposit(10000);
//
//        Item item = new Art("Laptop", "Brand");
//        Auction auction = seller.addAuction(item, 1000, 50);
//        auction.startAuction();
//
//        ExecutorService pool = Executors.newFixedThreadPool(2);
//        CountDownLatch startGate = new CountDownLatch(1);
//        CountDownLatch doneGate = new CountDownLatch(2);
//
//        pool.submit(() -> {
//            try {
//                startGate.await();
//                lowBidder.placeBid(auction, 1100);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            } finally {
//                doneGate.countDown();
//            }
//        });
//
//        pool.submit(() -> {
//            try {
//                startGate.await();
//                highBidder.placeBid(auction, 1200);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            } finally {
//                doneGate.countDown();
//            }
//        });
//
//        startGate.countDown();
//        assertTrue(doneGate.await(3, TimeUnit.SECONDS));
//        pool.shutdown();
//        assertTrue(pool.awaitTermination(3, TimeUnit.SECONDS));
//
//        assertEquals(1200, auction.getCurrentPrice());
//        assertEquals(highBidder, auction.getCurrentWinner());
//        assertEquals(1200, highBidder.getWallet().getLockBalance());
//
//        // loser should not keep locked money after being outbid.
//        assertEquals(0, lowBidder.getWallet().getLockBalance());
//        assertEquals(10000, lowBidder.getWallet().getBalance());
//    }
//
//    @Test
//    void finish_thenDirectSetCurrentWinner_shouldRejectNewBid() {
//        Clients seller = new Clients("seller", "p");
//        Clients bidder = new Clients("B", "p");
//        bidder.deposit(5000);
//
//        Item item = new Art("Watch", "Rolex");
//        Auction auction = seller.addAuction(item, 1000, 50);
//        auction.startAuction();
//        bidder.placeBid(auction, 1050);
//        auction.finishAuction();
//
//        boolean ok = auction.setCurrentWinner(bidder, 1200);
//
//        // With the current implementation this still returns true; this assertion documents desired behavior.
//        assertFalse(ok);
//        assertEquals(1050, auction.getCurrentPrice());
//        assertEquals(AuctionStatus.FINISHED, auction.getStatus());
//    }
//    @Test
//    void intense_stressTest_hundredBidders() throws Exception {
//        int numBidders = 100;
//        Clients seller = new Clients("seller", "p");
//        Item item = new Art("Super Rare Item", "Artist");
//        Auction auction = seller.addAuction(item, 1000, 10); // Bước giá 10
//        auction.startAuction();
//
//        ExecutorService pool = Executors.newFixedThreadPool(20);
//        CountDownLatch latch = new CountDownLatch(numBidders);
//
//        // Tạo 100 người dùng với số dư dồi dào
//        for (int i = 0; i < numBidders; i++) {
//            final int bidAmount = 1100 + (i * 10); // Mỗi người bid cao hơn người trước 10đ
//            Clients bidder = new Clients("bidder" + i, "p");
//            bidder.deposit(10000);
//
//            pool.submit(() -> {
//                try {
//                    bidder.placeBid(auction, bidAmount);
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        assertTrue(latch.await(10, TimeUnit.SECONDS));
//        pool.shutdown();
//
//        // KIỂM TRA TÍNH TOÀN VẸN:
//        // 1. Giá cuối cùng phải là mức giá cao nhất được đặt (1100 + 99*10 = 2090)
//        assertEquals(2090, auction.getCurrentPrice(), "Giá cuối cùng bị sai do Race Condition!");
//
//        // 2. TỔNG TIỀN HỆ THỐNG: Tổng số tiền bị lock trong tất cả ví phải đúng bằng giá cuối cùng
//        // Đây là lỗi hay gặp: Người cũ bị outbid nhưng tiền vẫn bị lock (hoặc lock của người mới không ghi nhận)
//        // Bạn cần loop qua danh sách bidders để check hoặc kiểm tra người thắng cuối cùng.
//        assertEquals(2090, auction.getCurrentWinner().getWallet().getLockBalance());
//    }
//    @Test
//    void raceCondition_bidVersusFinish() throws Exception {
//        Clients seller = new Clients("seller", "p");
//        Clients bidder = new Clients("sniper", "p");
//        bidder.deposit(5000);
//
//        Item item = new Art("Ending Soon", "Artist");
//        Auction auction = seller.addAuction(item, 1000, 50);
//        auction.startAuction();
//
//        ExecutorService pool = Executors.newFixedThreadPool(2);
//
//        // Luồng 1: Cố gắng đặt giá liên tục
//        pool.submit(() -> {
//            for(int i=0; i<100; i++) {
//                bidder.placeBid(auction, 1100 + i*50);
//            }
//        });
//
//        // Luồng 2: Kết thúc phiên đấu giá bất thình lình
//        pool.submit(() -> {
//            try { Thread.sleep(5); } catch (InterruptedException e) {}
//            auction.finishAuction();
//        });
//
//        pool.shutdown();
//        pool.awaitTermination(5, TimeUnit.SECONDS);
//
//        // KIỂM TRA: Nếu đấu giá đã FINISHED, số dư bị khóa (lockBalance) của người thắng
//        // phải khớp tuyệt đối với giá cuối cùng trong Auction.
//        // Tránh lỗi: Auction finish rồi nhưng bidder vẫn kịp lách qua khe cửa hẹp để lock thêm tiền.
//        if (auction.getStatus() == AuctionStatus.FINISHED) {
//            assertEquals(auction.getCurrentPrice(), auction.getCurrentWinner().getWallet().getLockBalance());
//        }
//    }
//
//    @Test
//    void cancelAuction_withoutAnyBid_shouldNotThrowAndSetCancelled() {
//        Clients seller = new Clients("seller", "p");
//        Item item = new Art("NoBidItem", "Artist");
//        Auction auction = seller.addAuction(item, 1000, 50);
//
//        // This test is expected to fail with current code because currentWinner is null.
//        assertDoesNotThrow(auction::cancelAuction); // ép chương trình chạy cancelAuction
//        assertEquals(AuctionStatus.CANCELLED, auction.getStatus());
//    }
//
//    @Test
//    void failing_paymentShouldNotDeductWinnerBalanceTwice() {
//        Clients seller = new Clients("seller", "p");
//        Clients bidder = new Clients("winner", "p");
//        bidder.deposit(5000);
//
//        Item item = new Art("Rare", "Artist");
//        Auction auction = seller.addAuction(item, 1000, 50);
//        auction.startAuction();
//        bidder.placeBid(auction, 1200);
//        auction.finishAuction();
//
//        double balanceBeforePay = bidder.getWallet().getBalance();
//        auction.payingAuction();
//
//        // Expected domain behavior: bid money was already reserved during bidding,
//        // so paying should commit lock only, not reduce balance again.
//        assertEquals(balanceBeforePay, bidder.getWallet().getBalance());
//    }
//
//    @Test
//    void failing_paymentShouldClearLockedMoneyAfterPaid() {
//        Clients seller = new Clients("seller", "p");
//        Clients bidder = new Clients("winner", "p");
//        bidder.deposit(5000);
//
//        Item item = new Art("Phone", "Brand");
//        Auction auction = seller.addAuction(item, 1000, 50);
//        auction.startAuction();
//        bidder.placeBid(auction, 1300);
//        auction.finishAuction();
//        auction.payingAuction();
//
//        // Expected domain behavior: after PAID, there should be no locked balance.
//        assertEquals(0, bidder.getWallet().getLockBalance());
//    }
//}
//
