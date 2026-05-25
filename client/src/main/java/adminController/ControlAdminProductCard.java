package adminController;

import auction.logic.model.Auction;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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

public class ControlAdminProductCard {
    private Timeline countdownTimeline;
    @FXML
    private Label name;
    @FXML
    private Label type;
    @FXML
    private Label price;
    @FXML
    private Label time;
    @FXML
    private ImageView itemImageView;
    @FXML
    private Button viewBtn;
    @FXML
    private Button cancelBut;

    private Auction currentAuction;

    public void setOnCancelHandler(EventHandler<ActionEvent> eventHandler) {
        if(cancelBut != null){
            cancelBut.setOnAction(eventHandler);
        }
    }
    public void setOnViewHandler(EventHandler<ActionEvent> eventHandler) {
        if(viewBtn != null){
            viewBtn.setOnAction(eventHandler);
        }
    }

    @FXML
    public void cancel(ActionEvent event) {
        // FXML onAction='#cancel' will call this; forward to any programmatically set handler if present
        if (cancelBut != null && cancelBut.getOnAction() != null) {
            cancelBut.getOnAction().handle(event);
        }
    }

    @FXML
    public void view(ActionEvent event) {
        if (viewBtn != null && viewBtn.getOnAction() != null) {
            viewBtn.getOnAction().handle(event);
        }
    }
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
        // Disable bid button if auction already ended

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
    public static VBox renderCard(Auction myAuction) {
        try{
            FXMLLoader loader = new FXMLLoader(ControlAdminProductCard.class.getResource("/adminResource/productCardForAdmin.fxml"));
            VBox cardBox = loader.load();
            ControlAdminProductCard controlProductCard = loader.getController();
            controlProductCard.setData(myAuction);

            cardBox.setUserData(controlProductCard);
            // set CSS ids so callers using lookup("#cancel"/"#view") can find buttons
            if (controlProductCard.cancelBut != null) controlProductCard.cancelBut.setId("cancel");
            if (controlProductCard.viewBtn != null) controlProductCard.viewBtn.setId("view");
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
            price.setText("0.00");
            time.setText("N/A");
            return;
        }

        price.setText(String.format("%.2f", currentAuction.getCurrentPrice()));
        time.setText(formatRemaining(currentAuction));
    }
    private boolean isAuctionEnded(Auction auction) {
        if (auction == null || auction.getFinishTime() == null) return false;
        ClientSocket clientSocket = ClientSocket.getInstance();
        long finishMillis = clientSocket.toServerEpochMillis(auction.getFinishTime());
        long remainingMillis = finishMillis - clientSocket.getServerTimeMillis();
        return remainingMillis <= 0;
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
            price.textProperty().unbind();
        }
        currentAuction = null;
    }
}