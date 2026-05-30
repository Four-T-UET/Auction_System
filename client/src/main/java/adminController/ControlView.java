package adminController;

import Utils.AlertShow;
import auction.logic.enums.AuctionStatus;
import stateManager.AuctionManager;
import stateManager.AuctionUpdateListener;
import auction.logic.model.Auction;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import service.TimeSyncService;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlView implements Initializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ControlView.class);
    private Auction auction;
    private AuctionUpdateListener updateListener;
    private Timeline countdownTimeLine;
    @FXML private Label timeLeft;
    @FXML private Label type;
    @FXML private Label name;
    @FXML private Label ownerLabel;
    @FXML private Label currentWinnerLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label minimumStepLabel;
    @FXML private Label statusLabel;
    @FXML private Label descriptionLabel;
    @FXML private ImageView itemImageView;


    /**
     * Cập nhật thông tin sản phẩm trên bên trái
     */
    private void updateProductInfo(Auction auction) {
        if (auction == null || auction.getItem() == null) return;

        // Cập nhật tên sản phẩm
        name.setText(auction.getItem().getName());

        // Cập nhật Owner
        ownerLabel.setText(auction.getSeller().getUsername());

        // Cập nhật miniumStep
        minimumStepLabel.setText(String.valueOf(auction.getMiniumStep()));

        // ĐÃ SỬA: Kiểm tra xem đã có người ra giá (Winner) hay chưa
        if (auction.getCurrentWinner() != null) {
            currentWinnerLabel.setText(auction.getCurrentWinner().getUsername());
        } else {
            currentWinnerLabel.setText("No winner yet"); // Hiển thị nếu chưa có ai bid
        }

        // Cập nhật mô tả
        descriptionLabel.setText(auction.getItem().getDescription() != null ?
                auction.getItem().getDescription() : "No description available");

        // Cập nhật category
        type.setText(auction.getItem().getCategory() != null ?
                auction.getItem().getCategory().toString() : "N/A");

        // Cập nhật giá hiện tại
        currentPriceLabel.setText(String.format("$%.2f", auction.getCurrentPrice()));

        // Cập nhật thời gian còn lại
        timeLeft.setText(formatTimeLeft(auction));

        // Cập nhật Status
        /// /////////////////////////////////////////////
        if (auction.getStatus() == AuctionStatus.CANCELLED) {
            statusLabel.setText("CANCELLED");
        } else if (isAuctionEnded(auction) && auction.getCurrentWinner() == null) {
            statusLabel.setText("UN_SOLD");
        } else {
            statusLabel.setText(auction.getStatus().toString());
        }
        /// /////////////////////////////////////////////////

        // Cập nhật hình ảnh
        if (auction.getItem().getImageBytes() != null && auction.getItem().getImageBytes().length > 0) {
            try {
                Image image = new Image(new java.io.ByteArrayInputStream(auction.getItem().getImageBytes()));
                itemImageView.setImage(image);
            } catch (Exception e) {
                LOGGER.warn("Error loading product image: " + e.getMessage(), e);
            }
        } else {
            Image image = new Image("image/loginImage.jpg");
            itemImageView.setImage(image);
        }
    }

    /**
     * Format thời gian còn lại thành chuỗi dễ đọc
     */
    private String formatTimeLeft(Auction auction){
        if(auction == null || auction.getFinishTime() == null){
            return "N/A";
        }
        if (auction.getStatus() == AuctionStatus.CANCELLED){
            return "Auction cancelled";
        }
        TimeSyncService timeSyncService = TimeSyncService.getInstance();
        long finishMillis = timeSyncService.toServerEpochMillis(auction.getFinishTime());
        long remainingMillis = finishMillis - timeSyncService.getServerTimeMillis();

        if(remainingMillis <= 0){
            return "Auction ended";
        }

        Duration dur = Duration.ofMillis(remainingMillis);
        long totalSeconds = Math.max(0, dur.getSeconds());
        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds % (24 * 3600)) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return days > 0
                ? String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds)
                : String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    /**     * Áp dụng style: đỏ nếu đã kết thúc, xanh nếu đang hoạt động     */
    private void applyTimeLeftStyle(Auction auction) {
        if (timeLeft == null) return;
        if (auction == null) {
            timeLeft.setText("N/A");
            timeLeft.setTextFill(javafx.scene.paint.Color.GRAY);
            return;
        }
        timeLeft.setText(formatTimeLeft(auction));
        boolean ended = isAuctionEnded(auction);
        timeLeft.setTextFill(ended ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.LIMEGREEN);
    }

    private boolean isAuctionEnded(Auction auction) {
        if (auction == null || auction.getFinishTime() == null) return false;
        /// ///////////////
        if (auction.getStatus() == AuctionStatus.CANCELLED) {
            return true;
        }
        /// ////////////////////////////////////////////////////
        TimeSyncService timeSyncService = TimeSyncService.getInstance();
        long finishMillis = timeSyncService.toServerEpochMillis(auction.getFinishTime());
        long remainingMillis = finishMillis - timeSyncService.getServerTimeMillis();
        return remainingMillis <= 0;
    }

    /**
     * Được gọi khi FXML được load
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        registerAuctionListener();
        // Bắt đầu đếm ngược mỗi giây
        startRealtimeCountdown();
        setupAutoCleanup();
    }

    /**
     * Bắt đầu cập nhật đếm ngược mỗi giây
     */
    private void startRealtimeCountdown() {
        if (countdownTimeLine != null) countdownTimeLine.stop();
        countdownTimeLine = new Timeline(
                new KeyFrame(javafx.util.Duration.ZERO, e -> refreshTimeDisplay()),
                new KeyFrame(javafx.util.Duration.seconds(1), e -> refreshTimeDisplay())
        );
        countdownTimeLine.setCycleCount(Timeline.INDEFINITE);
        countdownTimeLine.play();
    }

    /**
     * Làm mới hiển thị thời gian mỗi giây kèm màu sắc
     */
    private void refreshTimeDisplay() {
        if (auction != null) {
            applyTimeLeftStyle(auction);
        }
    }

    /**
     * Đăng ký listener với AuctionManager để nhận realtime updates
     */
    private void registerAuctionListener() {
        // Tạo listener
        updateListener = new AuctionUpdateListener() {
            @Override
            public void onAuctionAdded(Auction a) {
                // không xử lý trong bidding screen (chỉ quan tâm auction hiện tại bị update)
            }

            @Override
            public void onAuctionUpdated(Auction a) {
                // Nếu update là cho auction hiện tại, refresh UI
                if (auction != null && a.getId().equals(auction.getId())) {
                    Platform.runLater(() -> Display(a));
                }
            }

            @Override
            public void onAuctionsReplaced(List<Auction> auctions) {
                // Cập nhật lại auction hiện tại nếu có trong danh sách mới
                if (auction != null) {
                    auctions.stream()
                            .filter(a -> a.getId().equals(auction.getId()))
                            .findFirst()
                            .ifPresent(updated -> Platform.runLater(() -> Display(updated)));
                }
            }

            @Override
            public void onAuctionRemoved(String auctionId) {
                if (auction != null && auction.getId().equals(auctionId)) {
                    Platform.runLater(() -> {
                        AlertShow.showAlert(Alert.AlertType.WARNING, "Thông báo",
                                "Phiên đấu giá đã bị hủy!");
                        // TODO: có thể gọi returnToMain() hoặc đóng màn hình bidding
                    });
                }
            }
        };

        // Đăng ký listener vào AuctionManager
        AuctionManager.getInstance().registerListener(updateListener);
    }

    /**
     * Cập nhật UI khi nhận realtime update từ server
     */
    public void Display(Auction updatedAuction) {
        if (updatedAuction == null) return;

        // 1. Unregister listener cũ (nếu có) để tránh duplicate
        unregisterAuctionListener();

        // 2. Cập nhật reference
        this.auction = updatedAuction;

        // 3. Cập nhật thông tin sản phẩm
        updateProductInfo(updatedAuction);

        // 4. Đăng ký listener mới cho auction này
        registerAuctionListener();
    }


    /**
     * Hủy đăng ký listener từ AuctionManager một cách an toàn
     */
    private void unregisterAuctionListener() {
        if (updateListener != null) {
            AuctionManager.getInstance().unregisterListener(updateListener);
            updateListener = null;
        }
    }

    /**
     * Cơ chế tự động gọi hàm cleanup khi view bị tháo khỏi Center Pane
     */
    private void setupAutoCleanup() {
        if (name != null) { // Chọn đại một nhãn Label đã được @FXML inject
            name.sceneProperty().addListener((observable, oldScene, newScene) -> {
                if (newScene == null) {
                    cleanup();
                }
            });
        }
    }

    /**
     * Giải phóng hoàn toàn Timeline và Listener
     */
    public void cleanup() {
        // 1. Dừng Timeline đếm ngược
        if (countdownTimeLine != null) {
            countdownTimeLine.stop();
            countdownTimeLine = null;
        }

        // 2. Hủy đăng ký listener từ AuctionManager
        unregisterAuctionListener();

        // 3. Clear auction reference
        auction = null;
    }



}
