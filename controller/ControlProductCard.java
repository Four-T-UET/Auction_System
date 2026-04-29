package controller.etrade;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import model.Auction;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

public class ControlProductCard implements Initializable {
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
    private BidButtonListener bidListener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bidBut.setOnAction(event -> {
            try{
            BidNow(event);}
            catch (IOException e){
                e.printStackTrace();
            }
        });
    }

    // Interface để callback khi nhấn Bid
    @FunctionalInterface
    public interface BidButtonListener {
        void onBidClicked(Auction auction);
    }
    
    // Set listener từ Dashboard
    public void setBidListener(BidButtonListener listener) {
        this.bidListener = listener;
    }
    
    @FXML
    public void BidNow(ActionEvent event) throws IOException {
        if (bidListener != null && currentAuction != null) {
            bidListener.onBidClicked(currentAuction);
        }
    }
    
    public void setData(Auction auction){
        this.currentAuction = auction;
        if (auction == null || auction.getItem() == null) {
            return;
        }

        itemName.setText(auction.getItem().getName());
        itemType.setText(auction.getItem().getCategory() != null ? auction.getItem().getCategory().toString() : "Unknown");
        itemCurrentBid.textProperty().bind(auction.currentPriceProperty().asString("%.2f"));
        loadImage(auction);
        startRealtimeUpdate();
    }

    private String formatFinishTime(Auction auction) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return auction.getFinishTime() != null ? auction.getFinishTime().format(formatter) : "N/A";
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

    private Image resolveImage(Auction auction) {
        if (auction.getItem().getImageBytes() != null && auction.getItem().getImageBytes().length > 0) {
            return new Image(new ByteArrayInputStream(auction.getItem().getImageBytes()));
        }

        String imagePath = auction.getItem().getImagePath();
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }

        if (imagePath.startsWith("/")) {
            var stream = getClass().getResourceAsStream(imagePath);
            return stream != null ? new Image(stream) : null;
        }

        File file = new File(imagePath);
        if (file.exists()) {
            return new Image(file.toURI().toString());
        }

        var stream = getClass().getResourceAsStream("/controller/etrade/" + imagePath);
        return stream != null ? new Image(stream) : null;
    }

//    public static VBox renderCard(Auction myAuction, BidButtonListener bidListener) {
//        try{
//            FXMLLoader loader = new FXMLLoader(ControlProductCard.class.getResource("/app/etrade/productCard.fxml"));
//            VBox cardBox = loader.load();
//            ControlProductCard controlProductCard = loader.getController();
//            controlProductCard.setBidListener(bidListener);
//            controlProductCard.setData(myAuction);
//            return cardBox;
//        }catch (IOException e){
//            e.printStackTrace();
//            return null;
//        }
//    }
    public static VBox renderCard(Auction myAuction) {
        try{
            FXMLLoader loader = new FXMLLoader(ControlProductCard.class.getResource("/controller/etrade/productCard.fxml"));
            VBox cardBox = loader.load();
            ControlProductCard controlProductCard = loader.getController();
            controlProductCard.setData(myAuction);
            return cardBox;
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
    public static VBox renderCard(Auction myAuction, BidButtonListener bidListener,boolean isHidden) {
        try{
            FXMLLoader loader = new FXMLLoader(ControlProductCard.class.getResource("/controller/etrade/productCard.fxml"));
            VBox cardBox = loader.load();
            ControlProductCard controlProductCard = loader.getController();
            controlProductCard.setBidListener(bidListener);
            controlProductCard.setData(myAuction);
            return cardBox;
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
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
        long minutes = dur.toMinutes();
        long seconds = dur.getSeconds();
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
