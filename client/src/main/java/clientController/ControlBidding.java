package clientController;

import Utils.AlertShow;
import auction.logic.enums.AuctionStatus;
import javafx.animation.Timeline;
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
import stateManager.AuctionManager;
import stateManager.AuctionUpdateListener;
import service.AuctionService;
import javafx.animation.KeyFrame;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.ResourceBundle;
import service.TimeSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stateManager.UserSession;

public class ControlBidding implements Initializable {
  private static final Logger LOGGER = LoggerFactory.getLogger(ControlBidding.class);
  private Auction auction;
  private AuctionUpdateListener updateListener;
  private Timeline countdownTimeline;

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
        winnerLabel.setText(auction.getCurrentWinner().getUsername());
      } else {
        winnerLabel.setText("No winner yet");
      }
      applyTimeLeftStyle(auction);

      // Hủy đăng ký listener cũ của phiên trước đó (nếu có) trước khi đăng ký mới
      unregisterAuctionListener();
      registerAuctionListener();
    }
  }

  private void updateProductInfo(Auction auction) {
    if (auction == null || auction.getItem() == null) return;

    productNameLabel.setText(auction.getItem().getName());
    productDescription.setText(auction.getItem().getDescription() != null ?
            auction.getItem().getDescription() : "No description available");
    productCategory.setText(auction.getItem().getCategory() != null ?
            auction.getItem().getCategory().toString() : "N/A");
    productStartPrice.setText(String.format("$%.2f", auction.getCurrentPrice()));
    productTimeLeft.setText(formatTimeLeft(auction));

    if (auction.getItem().getImageBytes() != null && auction.getItem().getImageBytes().length > 0) {
      try {
        Image image = new Image(new java.io.ByteArrayInputStream(auction.getItem().getImageBytes()));
        productImage.setImage(image);
      } catch (Exception e) {
        LOGGER.warn("Không load được ảnh: " + e.getMessage(), e);
      }
    } else {
      productImage.setImage(new Image("/image/loginImage.jpg"));
    }
  }

  private String formatTimeLeft(Auction auction){
    if(auction == null || auction.getFinishTime() == null){
      return "N/A";
    }
    if (auction.getStatus() == AuctionStatus.CANCELLED) {
      return "Auction Canceled";
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

  private void applyTimeLeftStyle(Auction auction) {
    if (productTimeLeft == null) return;
    if (auction == null) {
      productTimeLeft.setText("N/A");
      productTimeLeft.setTextFill(javafx.scene.paint.Color.GRAY);
      return;
    }

    productTimeLeft.setText(formatTimeLeft(auction));
    boolean ended = isAuctionEnded(auction);
    boolean isCancelled = (auction.getStatus() == AuctionStatus.CANCELLED);

    productTimeLeft.setTextFill(ended || isCancelled ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.LIMEGREEN);

    if (fieldPrice != null) {
      fieldPrice.setDisable(ended || isCancelled);
    }
  }

  private boolean isAuctionEnded(Auction auction) {
    if (auction == null || auction.getFinishTime() == null) return false;
    TimeSyncService timeSyncService = TimeSyncService.getInstance();
    long finishMillis = timeSyncService.toServerEpochMillis(auction.getFinishTime());
    long remainingMillis = finishMillis - timeSyncService.getServerTimeMillis();
    return remainingMillis <= 0;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    startRealtimeCountdown();

    // TỰ ĐỘNG KHỬ KÍCH HOẠT: Khi rời khỏi màn hình Bidding này sang màn hình khác
    productNameLabel.sceneProperty().addListener((o, old, n) -> { if (n == null) dispose(); });
  }

  private void startRealtimeCountdown() {
    if (countdownTimeline != null) countdownTimeline.stop();
    // Sửa lại 1 KeyFrame duy nhất để tối ưu hiệu năng chạy ngầm mỗi giây
    countdownTimeline = new Timeline(
            new KeyFrame(javafx.util.Duration.seconds(1), e -> refreshTimeDisplay())
    );
    countdownTimeline.setCycleCount(Timeline.INDEFINITE);
    countdownTimeline.play();
  }

  private void refreshTimeDisplay() {
    if (auction != null) {
      applyTimeLeftStyle(auction);
    }
  }

  private void registerAuctionListener() {
    updateListener = new AuctionUpdateListener() {
      @Override public void onAuctionAdded(Auction a) {}

      @Override
      public void onAuctionUpdated(Auction a) {
        if (auction != null && a.getId().equals(auction.getId())) {
          Platform.runLater(() -> updateAuctionDisplay(a));
        }
      }

      @Override
      public void onAuctionsReplaced(List<Auction> auctions) {
        if (auction != null) {
          auctions.stream()
                  .filter(a -> a.getId().equals(auction.getId()))
                  .findFirst()
                  .ifPresent(updated -> Platform.runLater(() -> updateAuctionDisplay(updated)));
        }
      }

      @Override
      public void onAuctionRemoved(String auctionId) {
        if (auction != null && auction.getId().equals(auctionId)) {
          Platform.runLater(() -> {
            AlertShow.showAlert(Alert.AlertType.WARNING, "Thông báo", "Phiên đấu giá đã bị hủy!");
          });
        }
      }
    };
    AuctionManager.getInstance().registerListener(updateListener);
  }

  private void updateAuctionDisplay(Auction updatedAuction) {
    if (updatedAuction == null) return;

    if (this.auction != null && this.auction.getStatus() != AuctionStatus.CANCELLED && updatedAuction.getStatus() == AuctionStatus.CANCELLED) {
      AlertShow.showAlert(Alert.AlertType.WARNING, "Thông báo hệ thống", "Admin has already CANCELLED this AUCTION");
    }

    this.auction = updatedAuction;
    updateProductInfo(updatedAuction);

    priceCurrent.setText(String.format("%.2f", updatedAuction.getCurrentPrice()));
    step.setText(String.format("%.2f", updatedAuction.getMiniumStep()));

    if (updatedAuction.getCurrentWinner() != null) {
      winnerLabel.setText(updatedAuction.getCurrentWinner().getUsername());
    } else {
      winnerLabel.setText("No winner yet");
    }
  }

  public void unregisterAuctionListener() {
    if (updateListener != null) {
      AuctionManager.getInstance().unregisterListener(updateListener);
      updateListener = null;
    }
  }

  @FXML
  public void handleBid(ActionEvent ignored) {
    try {
      if (auction == null) {
        AlertShow.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy phiên đấu giá!");
        return;
      }

      if (auction.getStatus() == AuctionStatus.CANCELLED) {
        AlertShow.showAlert(Alert.AlertType.ERROR, "Lỗi hành động", "Phiên đấu giá đã bị Admin hủy bỏ. Bạn không thể đặt cược!");
        if (fieldPrice != null) fieldPrice.setDisable(true);
        return;
      }

      TimeSyncService timeSyncService = TimeSyncService.getInstance();
      if (auction.getFinishTime() != null) {
        long finishMillis = timeSyncService.toServerEpochMillis(auction.getFinishTime());
        long remainingMillis = finishMillis - timeSyncService.getServerTimeMillis();
        if (remainingMillis <= 0) {
          AlertShow.showAlert(Alert.AlertType.WARNING, "Đã kết thúc", "Phiên đấu giá đã kết thúc, không thể đặt giá.");
          return;
        }
      }

      String price = fieldPrice.getText().trim();
      double bidPrice = Double.parseDouble(price);

      Object resp = AuctionService.placeBid(auction.getId(), UserSession.getCurrentUser().getId(), bidPrice);

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
        Auction updated = (Auction) resp;
        this.auction = updated;
        updateProductInfo(updated);
        priceCurrent.setText(String.format("%.2f", updated.getCurrentPrice()));
        step.setText(String.format("%.2f", updated.getMiniumStep()));
        if (updated.getCurrentWinner() != null) {
          winnerLabel.setText(updated.getCurrentWinner().getUsername());
        } else {
          winnerLabel.setText("No winner yet");
        }
        AlertShow.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Bid thành công!");
      }

    } catch (NumberFormatException e) {
      AlertShow.showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá không hợp lệ!");
    } catch (Exception e) {
      AlertShow.showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra: " + e.getMessage());
    } finally {
      fieldPrice.clear();
    }
  }

  /**
   * Đồng bộ tên hàm dọn dẹp (dispose) chuẩn hóa cho toàn dự án
   */
  public void dispose() {
    if (countdownTimeline != null) {
      countdownTimeline.stop();
      countdownTimeline = null;
      LOGGER.info("Bidding Timeline stopped successfully!");
    }
    unregisterAuctionListener();
    auction = null;
  }
}