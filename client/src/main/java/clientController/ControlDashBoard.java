package clientController;

import auction.logic.enums.ItemCategory;
import stateManager.AuctionManager;
import stateManager.AuctionUpdateListener;
import auction.logic.model.Auction;
import java.util.ArrayList;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ControlDashBoard implements Initializable {
  private ObservableList<Auction> allAuctions;
  private AuctionUpdateListener updateListener;
  private final List<ControlProductCard> activeControllers = new java.util.ArrayList<>();
  private Timeline masterTimeline;
  @FXML
  private BorderPane mainPane;
  @FXML
  private Button findItem;
  @FXML
  private TextField searchField;
  @FXML
  private ComboBox<ItemCategory> categoryComBox;
  @FXML
  private Pagination pagination;

  @FXML
  public ItemCategory chooseCategory(){
    return categoryComBox.getSelectionModel().getSelectedItem();
  }

  @FXML
  public void Find(ActionEvent event) {
    ItemCategory type = categoryComBox.getValue();
    String searchData = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

    List<Auction> filteredList = new ArrayList<>();

    for (Auction a : allAuctions) {
      if ((type == null || a.getItem().getCategory() == type) &&
              (searchData.isEmpty() || a.getItem().getName().toLowerCase().contains(searchData))) {

        filteredList.add(a);
      }
    }

    dividePage(filteredList);
  }

  @FXML
  private void returnToMain(ActionEvent event) {
    try {
      dispose(); // Dọn dẹp tài nguyên trước khi tải lại màn hình chính mới
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientResource/MainDashboard.fxml"));
      Parent root = loader.load();
      Scene scene = mainPane.getScene();
      if (scene != null) {
        scene.setRoot(root);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void setAuctions(List<Auction> auctionsDB) {
    if (auctionsDB != null) {
      allAuctions.setAll(auctionsDB);
    }
  }

  private void openBiddingScreen(Auction auction) {
    try {
      dispose(); // Dừng cập nhật danh sách đấu giá khi vào chi tiết phiên Bid
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientResource/Bidding.fxml"));
      Parent biddingView = loader.load();
      ControlBidding biddingController = loader.getController();
      biddingController.setAuction(auction);
      mainPane.setCenter(biddingView);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void chageToHistory(ActionEvent event) {
    try {
      dispose();
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientResource/DashBoardTOHistory.fxml"));
      mainPane.setCenter(loader.load());
    } catch (IOException e){
      e.printStackTrace();
    }
  }

  @FXML
  private void chageToWallet(ActionEvent event) {
    try {
      dispose();
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientResource/DashBoardTOWallet.fxml"));
      mainPane.setCenter(loader.load());
    } catch(IOException e){
      e.printStackTrace();
    }
  }

  @FXML
  private void chageToSelling(ActionEvent event) {
    try {
      dispose();
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientResource/DashBoardTOSelling.fxml"));
      mainPane.setCenter(loader.load());
    } catch (IOException e){
      e.printStackTrace();
    }
  }

  @FXML
  private void changeToAccount(ActionEvent event) {
    try {
      dispose();
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientResource/DashBoardTOAccount.fxml"));
      mainPane.setCenter(loader.load());
    } catch (IOException e){
      e.printStackTrace();
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    categoryComBox.setItems(FXCollections.observableArrayList(ItemCategory.values()));
    categoryComBox.getSelectionModel().select(ItemCategory.REAL_ESTATE);
    allAuctions = AuctionManager.getInstance().getMasterAuctionList();
    if(!allAuctions.isEmpty()){
      dividePage(allAuctions);
    }

    registerAuctionListener();
    startMasterTimeline();

    // TỰ ĐỘNG KHỬ KÍCH HOẠT TỔNG LỰC: Đảm bảo an toàn nếu toàn bộ Dashboard bị thay thế (ví dụ: Đăng xuất)
    mainPane.sceneProperty().addListener((o, old, n) -> { if (n == null) dispose(); });
  }

  /**
   * Hàm giải phóng tài nguyên tập trung (Tắt timeline, hủy listener của danh sách và các thẻ con)
   */
  public void dispose() {
    if (masterTimeline != null) {
      masterTimeline.stop();
      masterTimeline = null;
    }
    unregisterAuctionListener();
    clearActiveControllers();
  }

  private void clearActiveControllers() {
    for (ControlProductCard controller : activeControllers) {
      if (controller != null) {
        controller.dispose();
      }
    }
    activeControllers.clear();
  }

  private void registerAuctionListener() {
    updateListener = new AuctionUpdateListener() {
      @Override public void onAuctionAdded(Auction auction) { Platform.runLater(() -> dividePage(allAuctions)); }

      @Override
      public void onAuctionUpdated(Auction auction) {
        Platform.runLater(() -> {
          for (int i = 0; i < allAuctions.size(); i++) {
            if (allAuctions.get(i).getId().equals(auction.getId())) {
              allAuctions.set(i, auction);
              break;
            }
          }

          for (ControlProductCard controller : activeControllers) {
            if (controller.getCurrentAuction() != null &&
                    controller.getCurrentAuction().getId().equals(auction.getId())) {
              controller.setData(auction, false);
              break;
            }
          }
        });
      }

      @Override public void onAuctionsReplaced(List<Auction> auctions) { Platform.runLater(() -> dividePage(allAuctions)); }
      @Override public void onAuctionRemoved(String auctionId) { Platform.runLater(() -> dividePage(allAuctions)); }
    };
    AuctionManager.getInstance().registerListener(updateListener);
  }

  private void unregisterAuctionListener() {
    if (updateListener != null) {
      AuctionManager.getInstance().unregisterListener(updateListener);
      updateListener = null;
    }
  }

  private void dividePage(List<Auction> listAuctions) {
    final int NUM_ITEM = 8;
    int pageCount = (int) Math.ceil((double) listAuctions.size() / NUM_ITEM);
    if (listAuctions.isEmpty()) {
      pagination.setPageCount(1);
      pagination.setPageFactory((pageIndex) -> new ScrollPane(new FlowPane()));
      return;
    }
    pagination.setPageCount(pageCount);
    pagination.setPageFactory((pageIndex) -> createPage(listAuctions, pageIndex, NUM_ITEM));
  }

  private ScrollPane createPage(List<Auction> auctions, int pageIndex, int itemsPerPage) {
    clearActiveControllers();
    int start = pageIndex * itemsPerPage;
    int end = Math.min(start + itemsPerPage, auctions.size());

    FlowPane page = new FlowPane();
    page.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
    page.setHgap(30);
    page.setVgap(12);
    page.prefWrapLengthProperty().bind(pagination.widthProperty().subtract(20));

    for (int i = start; i < end; i++) {
      Auction currentAuction = auctions.get(i);

      ControlProductCard controller = ControlProductCard.renderCard(
              currentAuction,
              false,
              cardVisual -> page.getChildren().add(cardVisual),
              latestAuction -> openBiddingScreen(latestAuction)
      );

      if (controller != null) {
        activeControllers.add(controller);
      }
    }

    ScrollPane sp = new ScrollPane(page);
    sp.setFitToWidth(true);
    sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    return sp;
  }

  private void startMasterTimeline() {
    if (masterTimeline != null) masterTimeline.stop();
    masterTimeline = new Timeline(
            new KeyFrame(javafx.util.Duration.seconds(1), event -> {
              for (ControlProductCard controller : activeControllers) {
                if (controller != null) {
                  controller.refreshDisplay();
                }
              }
            })
    );
    masterTimeline.setCycleCount(Timeline.INDEFINITE);
    masterTimeline.play();
  }
}