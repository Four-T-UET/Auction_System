package clientController;

import auction.logic.enums.AuctionStatus;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import auction.logic.model.Auction;
import service.ClientSocket;
import service.TimeSyncService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlProductCard{
    private static final Logger LOGGER = LoggerFactory.getLogger(ControlProductCard.class);

    @FXML
    private ImageView itemImageView;
    @FXML
    private Label itemName;
    @FXML
    private Label itemType;
    @FXML
    private Label itemTimeLeft;
    @FXML
    private Label itemCurrentBid;
    @FXML
    private Button bidBut;

    private Auction currentAuction;

    public Auction getCurrentAuction() {
        return currentAuction;
    }


    public void setBidButVisible(boolean visible) {
        if(bidBut != null){
            bidBut.setVisible(visible);
            bidBut.setManaged(visible);
        }
    }
    public void setData(Auction auction, boolean isHidden){
        if(currentAuction != null){
            itemCurrentBid.textProperty().unbind();
        }
        this.currentAuction = auction;
        if (auction == null || auction.getItem() == null) {
            itemName.setText("");
            itemType.setText("");
            itemCurrentBid.setText("0.00");
            itemTimeLeft.setText("N/A");
            return;
        }

        itemName.setText(auction.getItem().getName());
        itemType.setText(auction.getItem().getCategory() != null ? auction.getItem().getCategory().toString() : "Unknown");
        loadImage(auction);
        // Vô hiệu hóa nút bid nếu phiên đấu giá đã kết thúc
        if (bidBut != null) {
            bidBut.setDisable(isHidden);
        }
        refreshDisplay();
    }

    private void loadImage(Auction auction) {
        try {
            Image image = resolveImage(auction);
            if (image != null) {
                itemImageView.setImage(image);
            }
        } catch (Exception e) {
            LOGGER.error("Không loa được ảnh", e);
        }
    }

    // Đổi EventHandler<ActionEvent> thành Consumer<Auction>
    public static ControlProductCard renderCard(Auction myAuction,
                                                boolean isHidden,
                                                PaneContainer container,
                                                java.util.function.Consumer<Auction> onBidClick) {
        try {
            FXMLLoader loader = new FXMLLoader(ControlProductCard.class.getResource("/clientResource/productCard.fxml"));
            VBox cardBox = loader.load();
            ControlProductCard controller = loader.getController();

            if (controller.bidBut != null) {
                // Sửa lại đoạn này: Lấy currentAuction hiện tại của controller để truyền ra ngoài
                controller.bidBut.setOnAction(e -> {
                    if (onBidClick != null) {
                        onBidClick.accept(controller.getCurrentAuction());
                    }
                });
                if (isHidden) controller.bidBut.setDisable(true);
            }

            controller.setData(myAuction, isHidden);

            if (container != null) {
                container.addNode(cardBox);
            }

            return controller;
        } catch (IOException e) {
            LOGGER.error("Error khi load FXML", e);
            return null;
        }
    }

private Image resolveImage(Auction auction) {
    try {
        //  Thử load từ bytes trước
        if (auction.getItem().getImageBytes() != null && auction.getItem().getImageBytes().length > 0) {
            return new Image(new ByteArrayInputStream(auction.getItem().getImageBytes()));
        }
    } catch (Exception e) {
        LOGGER.warn("Error loading image from bytes: " + e.getMessage(), e);
    }

    //  Thử load default image từ classpath
    try {
        java.io.InputStream stream = getClass().getResourceAsStream("/image/loginImage.jpg");
        if (stream != null) {
            return new Image(stream);
        }
    } catch (Exception e) {
        LOGGER.warn("Error loading default image from resources: " + e.getMessage(), e);
    }

    //  Nếu fail hết, trả về null
    LOGGER.warn("Warning: No image available for auction");
    return null;
}

    public void refreshDisplay() {
        if (currentAuction == null) {
            itemCurrentBid.setText("0.00");
            itemTimeLeft.setText("N/A");
            return;
        }

        itemCurrentBid.setText(String.format("%.2f", currentAuction.getCurrentPrice()));
        itemTimeLeft.setText(formatRemaining(currentAuction));
        // Tự động kiểm tra hết giờ để ẩn nút
        boolean ended = isAuctionEnded(currentAuction);
        setBidButVisible(!ended);
    }
    private boolean isAuctionEnded(Auction auction) {
        if (auction == null || auction.getFinishTime() == null) return false;
        /// ///////////////////////////////////////////////////////
        if (auction.getStatus() == AuctionStatus.CANCELLED){
            return true;
        }
        /// ///////////////////////////////////////////////////////
        ClientSocket clientSocket = ClientSocket.getInstance();
        TimeSyncService timeSyncService = TimeSyncService.getInstance();
        long finishMillis = timeSyncService.toServerEpochMillis(auction.getFinishTime());
        long remainingMillis = finishMillis - timeSyncService.getServerTimeMillis();
        return remainingMillis <= 0;  // TRUE = hết giờ → nút biến mất
        // ĐOẠN DEBUG: In ra console để xem số nào đang bị sai
    }

    private String formatRemaining(Auction auction){
        if(auction == null || auction.getFinishTime() == null){
            return "N/A";
        }


        /// /////////////////////////////////////////////
        if (auction.getStatus() == AuctionStatus.CANCELLED) {
            return "Auction Canceled";
        }
        /// ///////////////////////////////////////////////


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

    public void dispose(){
        if(currentAuction != null){
            itemCurrentBid.textProperty().unbind();
        }
        currentAuction = null;
    }
    // Giao diện hỗ trợ Dashboard nhét thẻ vào FlowPane
    @FunctionalInterface
    public interface PaneContainer {
        void addNode(VBox cardVisual);
    }
}
