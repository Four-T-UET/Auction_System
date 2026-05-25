package clientController;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;

public class ControlProductCard{
    private Timeline countdownTimeline;
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

    //HELPER
    public void setOnBidHandler(EventHandler<ActionEvent> eventHandler) {
        if(bidBut != null){
            bidBut.setOnAction(eventHandler);
        }
    }
    public void setBidButVisible(boolean visible) {
        if(bidBut != null){
            bidBut.setVisible(visible);
            bidBut.setManaged(visible);
        }
    }
    public void setData(Auction auction){
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
        // Disable bid button if auction already ended
        try {
            boolean ended = isAuctionEnded(auction);
            setBidButVisible(!ended);
        } catch (Exception ignored) {}
        if(countdownTimeline != null){
            countdownTimeline.stop();
            countdownTimeline = null;
        }
        refreshDisplay();
        startRealtimeUpdate();
    }

    private void loadImage(Auction auction) {
        try {
            Image image = resolveImage(auction);
            if (image != null) {
                itemImageView.setImage(image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static VBox renderCard(Auction myAuction,boolean isHidden) {
        try{
            FXMLLoader loader = new FXMLLoader(ControlProductCard.class.getResource("/clientResource/productCard.fxml"));
            VBox cardBox = loader.load();
            ControlProductCard controlProductCard = loader.getController();

            cardBox.setUserData(controlProductCard);
            if(isHidden){
                controlProductCard.bidBut.setDisable(isHidden);
            }
            controlProductCard.setData(myAuction);
            return cardBox;
        }catch (IOException e){
            System.out.println("Error loading FXML");
            e.printStackTrace();
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
        System.err.println("Error loading image from bytes: " + e.getMessage());
    }

    //  Thử load default image từ classpath
    try {
        java.io.InputStream stream = getClass().getResourceAsStream("/image/loginImage.jpg");
        if (stream != null) {
            return new Image(stream);
        }
    } catch (Exception e) {
        System.err.println("Error loading default image from resources: " + e.getMessage());
    }

    //  Nếu fail hết, trả về null
    System.err.println("Warning: No image available for auction");
    return null;
}

    // chage time
    private void startRealtimeUpdate(){
        if (countdownTimeline != null) countdownTimeline.stop();
        countdownTimeline = new Timeline(
            new KeyFrame(javafx.util.Duration.ZERO, e -> refreshDisplay()),
            new KeyFrame(javafx.util.Duration.seconds(1), e -> {
                refreshDisplay();
            })
        );
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void refreshDisplay() {
        if (currentAuction == null) {
            itemCurrentBid.setText("0.00");
            itemTimeLeft.setText("N/A");
            return;
        }

        itemCurrentBid.setText(String.format("%.2f", currentAuction.getCurrentPrice()));
        itemTimeLeft.setText(formatRemaining(currentAuction));
    }
    private boolean isAuctionEnded(Auction auction) {
        if (auction == null || auction.getFinishTime() == null) return false;
        ClientSocket clientSocket = ClientSocket.getInstance();
        long finishMillis = clientSocket.toServerEpochMillis(auction.getFinishTime());
        long remainingMillis = finishMillis - clientSocket.getServerTimeMillis();
        return remainingMillis <= 0;  // TRUE = hết giờ → nút biến mất
    }

    private String formatRemaining(Auction auction){
        if(auction == null || auction.getFinishTime() == null){
            return "N/A";
        }
        ClientSocket clientSocket = ClientSocket.getInstance();
        long finishMillis = clientSocket.toServerEpochMillis(auction.getFinishTime());
        long remainingMillis = finishMillis - clientSocket.getServerTimeMillis();

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
        if(countdownTimeline != null){
            countdownTimeline.stop();
            countdownTimeline = null;
        }
        if(currentAuction != null){
            itemCurrentBid.textProperty().unbind();
        }
        currentAuction = null;
    }

}
