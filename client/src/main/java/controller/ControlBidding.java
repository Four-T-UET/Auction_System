package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import auction.logic.model.Auction;
import auction.logic.model.Clients;

import java.net.URL;
import java.util.ResourceBundle;



public class ControlBidding {
    private Auction auction;

    @FXML
    private VBox ProductCard;
    @FXML
    private Label priceCurrent;
    @FXML
    private Label step;
    @FXML
    private TextField fieldPrice;
    @FXML
    private Label winnerLabel;

    public void setAuction(Auction auction) {
        this.auction = auction;
        if (auction != null) {
            priceCurrent.setText(String.format("%.2f", auction.getCurrentPrice()));
            step.setText(String.format("%.2f", auction.getMiniumStep()));
            winnerLabel.setText(auction.getCurrentWinner().getId());
            ProductCard.getChildren().setAll(ControlProductCard.renderCard(auction, true));
        }
    }

    @FXML
    public void handleBid(ActionEvent event) {
        try {
            String price = fieldPrice.getText().trim();
            if (auction == null) {
                AlertShow.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy phiên đấu giá!");
                return;
            }
            //TODO:Kiểm tra giá, gửi request lên sever cùng với price, this.auction

            // Cập nhật UI
            auction.currentPriceProperty().set(Double.parseDouble(price));
            priceCurrent.setText(String.format("%.2f", price));
            winnerLabel.setText("Bạn đang dẫn đầu!");
            AlertShow.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Bid thành công!");

        } catch (NumberFormatException e) {
            AlertShow.showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá không hợp lệ!");
        } catch (Exception e) {
            AlertShow.showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra: " + e.getMessage());
        }finally {
            fieldPrice.clear();
        }
    }
}
