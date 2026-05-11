package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import auction.logic.model.Auction;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

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
            return;
        }

        itemName.setText(auction.getItem().getName());
        itemType.setText(auction.getItem().getCategory() != null ? auction.getItem().getCategory().toString() : "Unknown");
        itemCurrentBid.textProperty().bind(auction.currentPriceProperty().asString("%.2f"));
        loadImage(auction);
        if(countdownTimeline != null){
            countdownTimeline.stop();
            countdownTimeline = null;
        }
        startRealtimeUpdate();
    }

    private void loadImage(Auction auction) {
        try {
            Image image = resolveImage(auction);
            if (image != null) {
                itemImageView.setImage(image);
            }
        } catch (Exception e) {
            AlertShow.showAlert(Alert.AlertType.ERROR, "Error", "Co loi trong viec load anh");
        }
    }

    public static VBox renderCard(Auction myAuction,boolean isHidden) {
        try{
            FXMLLoader loader = new FXMLLoader(ControlProductCard.class.getResource("/productCard.fxml"));
            VBox cardBox = loader.load();
            ControlProductCard controlProductCard = loader.getController();
            if(isHidden){
                controlProductCard.bidBut.setDisable(isHidden);
            }
            controlProductCard.setData(myAuction);
            return cardBox;
        }catch (IOException e){
            AlertShow.showAlert(Alert.AlertType.ERROR, "Error", "Co loi trong viec renderCard");
            return null;
        }
    }

    private Image resolveImage(Auction auction) {
        if (auction.getItem().getImageBytes() != null && auction.getItem().getImageBytes().length > 0) {
            return new Image(new ByteArrayInputStream(auction.getItem().getImageBytes()));
        }
        return new Image(ClassLoader.getSystemResourceAsStream("resource/loginImage.jpg"));
    }

    // chage time
    private void startRealtimeUpdate(){
        if (countdownTimeline != null) countdownTimeline.stop();
        countdownTimeline = new Timeline(
            new KeyFrame(javafx.util.Duration.seconds(1), e -> {
                itemTimeLeft.setText(formatRemaining(currentAuction));
            })
        );
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }
    private String formatRemaining(Auction auction){
        if(auction == null || auction.getFinishTime() == null){
            return "N/A";
        }
        java.time.Duration dur = java.time.Duration.between(java.time.LocalDateTime.now(),auction.getFinishTime());

        if(dur.isNegative() || dur.isZero()){
            return "END";
        }
        long hours = dur.toHours();
        long minutes = dur.toMinutes() % 60;
        long seconds = dur.getSeconds() % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
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
