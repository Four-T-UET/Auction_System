package adminController;

import auction.logic.enums.AuctionStatus;
import auction.logic.model.Auction;
import java.io.InputStream;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import service.ClientSocket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import service.TimeSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlAdminProductCard {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControlAdminProductCard.class);

    @FXML private Label name;
    @FXML private Label type;
    @FXML private Label price;
    @FXML private Label time;
    @FXML private ImageView itemImageView;
    @FXML private Button viewBtn;
    @FXML private Button cancelBut;

    private Auction currentAuction;

    public Auction getCurrentAuction() {
        return currentAuction;
    }

    public void setData(Auction auction) {
        if (currentAuction != null) {
            price.textProperty().unbind();
        }
        this.currentAuction = auction;
        if (auction == null || auction.getItem() == null) {
            name.setText("");
            type.setText("");
            price.setText("0.00");
            time.setText("N/A");
            return;
        }
        name.setText(auction.getItem().getName());
        type.setText(auction.getItem().getCategory() != null ? auction.getItem().getCategory().toString() : "Unknown");
        loadImage(auction);

        refreshDisplay();
    }

    private void loadImage(Auction auction) {
        try {
            Image image = resolveImage(auction);
            if (image != null) {
                itemImageView.setImage(image);
            }
        } catch (Exception e) {
            LOGGER.error("Error loading Product Card FXML", e);
        }
    }

    public static ControlAdminProductCard renderCard(Auction myAuction, PaneContainer container) {
        try {
            FXMLLoader loader = new FXMLLoader(ControlAdminProductCard.class.getResource("/adminResource/productCardForAdmin.fxml"));
            VBox cardBox = loader.load();

            ControlAdminProductCard controller = loader.getController();
            controller.setData(myAuction);

            // Đẩy đồ họa vào FlowPane
            container.addNode(cardBox);

            return controller; // Trả controller về cho Dashboard xử lý tiếp
        } catch (IOException e) {
            System.err.println("Error loading Product Card FXML");
            e.printStackTrace();
            return null;
        }
    }

    // Để Dashboard có thể cài sự kiện bằng EventHandler thông thường
    public void setOnCancelAction(EventHandler<ActionEvent> handler) {
        if (this.cancelBut != null) {
            this.cancelBut.setOnAction(handler);
        }
    }

    public void setOnViewAction(EventHandler<ActionEvent> handler) {
        if (this.viewBtn != null) {
            this.viewBtn.setOnAction(handler);
        }
    }

    private Image resolveImage(Auction auction) {
        try {
            if (auction.getItem().getImageBytes() != null && auction.getItem().getImageBytes().length > 0) {
                return new Image(new ByteArrayInputStream(auction.getItem().getImageBytes()));
            }
        } catch (Exception e) {
            LOGGER.warn("Error loading image from bytes: " + e.getMessage(), e);
        }

        try {
            InputStream stream = getClass().getResourceAsStream("/image/loginImage.jpg");
            if (stream != null) {
                return new Image(stream);
            }
        } catch (Exception e) {
            LOGGER.warn("Error loading default image from resources: " + e.getMessage(), e);
        }
        LOGGER.warn("Warning: No image available for auction");
        return null;
    }

    // ĐÃ CHUYỂN THÀNH PUBLIC ĐỂ DASHBOARD GỌI
    public void refreshDisplay() {
        if (currentAuction == null) {
            price.setText("0.00");
            time.setText("N/A");
            return;
        }

        price.setText(String.format("%.2f", currentAuction.getCurrentPrice()));
        time.setText(formatRemaining(currentAuction));
        /// //////////////////////////////////////////////////////
        boolean isStopped = isAuctionEnded(currentAuction);

        if (cancelBut != null) {
            // Nếu auction đã ended hoặc cancelled, vô hiệu hóa nút Hủy (xám nút, không cho bấm nữa)
            cancelBut.setDisable(isStopped);
        }
        /// /////////////////////////////////////////////
    }

    private boolean isAuctionEnded(Auction auction) {
        if (auction == null || auction.getFinishTime() == null) return false;

        /// /////////////////////////////////////////////
        if (auction.getStatus() == AuctionStatus.CANCELLED) {
            return true;
        }
        /// ////////////////////////////////////////////////

        TimeSyncService timeSyncService = TimeSyncService.getInstance();
        long finishMillis = timeSyncService.toServerEpochMillis(auction.getFinishTime());
        long remainingMillis = finishMillis - timeSyncService.getServerTimeMillis();
        return remainingMillis <= 0;
    }

    private String formatRemaining(Auction auction){
        if(auction == null || auction.getFinishTime() == null){
            return "N/A";
        }
        /// ///////////////////////////////
        if(auction.getStatus() == AuctionStatus.CANCELLED){
            return "Auction cancelled";
        }
        /// /////////////////////////////////
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
            price.textProperty().unbind();
        }
        currentAuction = null;
    }
    // Giao tiếp chức năng trung gian để lồng VBox đồ họa vào FlowPane
    @FunctionalInterface
    public interface PaneContainer {
        void addNode(VBox cardVisual);
    }
}