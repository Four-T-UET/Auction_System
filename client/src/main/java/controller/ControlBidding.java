package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import auction.logic.model.Auction;
import auction.logic.manager.AuctionManager;
import auction.logic.manager.AuctionUpdateListener;
import service.AuctionService;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;



public class ControlBidding implements Initializable {
    private Auction auction;
    private AuctionUpdateListener updateListener;

    @FXML
    private ImageView productImage;
    @FXML
    private Label productNameLabel;
    @FXML
    private Label productDescription;
    @FXML
    private Label productCategory;
    @FXML
    private Label productStartPrice;
    @FXML
    private Label productTimeLeft;
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
            updateProductInfo(auction);
            priceCurrent.setText(String.format("%.2f", auction.getCurrentPrice()));
            step.setText(String.format("%.2f", auction.getMiniumStep()));
            if (auction.getCurrentWinner() != null) {
                winnerLabel.setText(auction.getCurrentWinner().getId());
            } else {
                winnerLabel.setText("No winner yet");
            }

            // Đăng ký listener cho auction này
            registerAuctionListener();
        }
    }

    /**
     * Cập nhật thông tin sản phẩm trên bên trái
     */
    private void updateProductInfo(Auction auction) {
        if (auction == null || auction.getItem() == null) return;

        // Cập nhật tên sản phẩm
        productNameLabel.setText(auction.getItem().getName());

        // Cập nhật mô tả
        productDescription.setText(auction.getItem().getDescription() != null ?
                auction.getItem().getDescription() : "No description available");

        // Cập nhật category
        productCategory.setText(auction.getItem().getCategory() != null ?
                auction.getItem().getCategory().toString() : "N/A");

        // Cập nhật giá hiện tại (là giá công khai hiện tại, không phải startPrice)
        productStartPrice.setText(String.format("$%.2f", auction.getCurrentPrice()));

        // Cập nhật thời gian còn lại
        productTimeLeft.setText(formatTimeLeft(auction.getFinishTime()));

        // Cập nhật hình ảnh
        if (auction.getItem().getImageBytes() != null && auction.getItem().getImageBytes().length > 0) {
            try {
                Image image = new Image(new java.io.ByteArrayInputStream(auction.getItem().getImageBytes()));
                productImage.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading product image: " + e.getMessage());
            }
        }
    }

    /**
     * Format thời gian còn lại thành chuỗi dễ đọc
     */
    private String formatTimeLeft(LocalDateTime finishTime) {
        if (finishTime == null) return "N/A";

        LocalDateTime now = LocalDateTime.now();
        if (finishTime.isBefore(now)) {
            return "Auction ended";
        }

        java.time.Duration duration = java.time.Duration.between(now, finishTime);
        long totalSeconds = duration.getSeconds();
        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds % (24 * 3600)) / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        if (days > 0) {
            return days + "d " + hours + "h";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }


    /**
     * Được gọi khi FXML được load
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Có thể add initialization logic tại đây nếu cần
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
                    Platform.runLater(() -> updateAuctionDisplay(a));
                }
            }

            @Override
            public void onAuctionsReplaced(List<Auction> auctions) {
                // Cập nhật lại auction hiện tại nếu có trong danh sách mới
                if (auction != null) {
                    auctions.stream()
                            .filter(a -> a.getId().equals(auction.getId()))
                            .findFirst()
                            .ifPresent(updated -> Platform.runLater(() -> updateAuctionDisplay(updated)));
                }
            }

            @Override
            public void onAuctionRemoved(String auctionId) {
                // Nếu auction hiện tại bị xóa, có thể đóng bidding screen
                if (auction != null && auction.getId().equals(auctionId)) {
                    Platform.runLater(() -> {
                        AlertShow.showAlert(Alert.AlertType.WARNING, "Thông báo",
                                "Phiên đấu giá đã bị hủy!");
                        // TODO: có thể gọi returnToMain() hoặc close bidding screen
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
    private void updateAuctionDisplay(Auction updatedAuction) {
        if (updatedAuction == null) return;

        // Cập nhật reference
        this.auction = updatedAuction;

        // Cập nhật thông tin sản phẩm
        updateProductInfo(updatedAuction);

        // Cập nhật giao diện đấu giá
        priceCurrent.setText(String.format("%.2f", updatedAuction.getCurrentPrice()));
        step.setText(String.format("%.2f", updatedAuction.getMiniumStep()));

        if (updatedAuction.getCurrentWinner() != null) {
            winnerLabel.setText(updatedAuction.getCurrentWinner().getId());
        } else {
            winnerLabel.setText("No winner yet");
        }
    }

    /**
     * Hủy đăng ký listener khi không còn cần (tránh memory leak)
     * Gọi method này khi scene được thay đổi hoặc controller bị destroy
     */
    public void unregisterAuctionListener() {
        if (updateListener != null) {
            AuctionManager.getInstance().unregisterListener(updateListener);
        }
    }

    @FXML
    public void handleBid(ActionEvent ignored) {
        try {
            String price = fieldPrice.getText().trim();
            if (auction == null) {
                AlertShow.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy phiên đấu giá!");
                return;
            }
            // Parse input
            double bidPrice = Double.parseDouble(price);

            // send bid to server
            Object resp = AuctionService.placeBid(auction.getId(), bidPrice);

            if (resp == null) {
                AlertShow.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không nhận được phản hồi từ server");
            } else if (resp instanceof String) {
                String s = (String) resp;
                if (s.startsWith("FAILED")) {
                    AlertShow.showAlert(Alert.AlertType.ERROR, "Bid thất bại", s);
                } else {
                    AlertShow.showAlert(Alert.AlertType.INFORMATION, "Thông báo", s);
                }
            } else if (resp instanceof Auction) {
                // Server returned updated auction → update UI
                Auction updated = (Auction) resp;
                this.auction = updated;
                updateProductInfo(updated);
                priceCurrent.setText(String.format("%.2f", updated.getCurrentPrice()));
                step.setText(String.format("%.2f", updated.getMiniumStep()));
                if (updated.getCurrentWinner() != null) {
                    winnerLabel.setText(updated.getCurrentWinner().getId());
                } else {
                    winnerLabel.setText("No winner yet");
                }
                AlertShow.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Bid thành công!");
            } else {
                AlertShow.showAlert(Alert.AlertType.ERROR, "Lỗi", "Phản hồi không hợp lệ từ server");
            }

        } catch (NumberFormatException e) {
            AlertShow.showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá không hợp lệ!");
        } catch (Exception e) {
            AlertShow.showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra: " + e.getMessage());
        }finally {
            fieldPrice.clear();
        }
    }
}
