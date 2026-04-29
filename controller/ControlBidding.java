package controller.etrade;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import model.Auction;
import model.Clients;

import java.net.URL;
import java.util.ResourceBundle;

public class ControlBidding implements Initializable {
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
    private Button Bidding;

    public void setAuction(Auction auction) {
        this.auction = auction;
        if (auction != null) {
            priceCurrent.setText(String.format("%.2f", auction.getCurrentPrice()));
            step.setText(String.format("%.2f", auction.getMiniumStep()));
            ProductCard.getChildren().setAll(ControlProductCard.renderCard(auction, null, false));
        }
    }

    @FXML
    public void handleBid(ActionEvent event) {
        String price = fieldPrice.getText().trim();

        try {
            if (auction == null) {
                showAlert(Alert.AlertType.ERROR, "Không tìm thấy phiên đấu giá!");
                return;
            }

            double my_price = Double.parseDouble(price);

            // Lấy user hiện tại từ session
            Clients currentBidder = UserSession.getCurrentUser();

            if (currentBidder == null) {
                showAlert(Alert.AlertType.ERROR, "Bạn chưa đăng nhập!");
                return;
            }

            // Kiểm tra giá bid
            if (my_price <= 0) {
                showAlert(Alert.AlertType.ERROR, "Giá phải lớn hơn 0!");
                return;
            }

            // Thực hiện bid
            if (this.auction.setCurrentWinner(currentBidder, my_price)) {
                // Lưu vào database (memory)
                AuctionDB.insertAuction(this.auction);

                // Cập nhật UI
                auction.currentPriceProperty().set(my_price);
                priceCurrent.setText(String.format("%.2f", my_price));

                // Xóa text field
                fieldPrice.clear();

                showAlert(Alert.AlertType.INFORMATION, "Bid thành công!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Giá của bạn thấp hơn mức tối thiểu!");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Giá không hợp lệ!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Có lỗi xảy ra: " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Lỗi" : "Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
