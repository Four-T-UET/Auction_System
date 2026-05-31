package adminController;

import Utils.AlertShow;
import Utils.ChangeScene;
import auction.logic.enums.AuctionStatus;
import stateManager.AuctionManager;
import stateManager.AuctionUpdateListener;
import auction.logic.model.Auction;
import stateManager.UserSession;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.util.Duration;
import service.AdminService;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ControlAdminDashBoard implements Initializable {
    private ObservableList<Auction> allAuctions;
    private AuctionUpdateListener updateListener;
    private final List<ControlAdminProductCard> activeControllers = new ArrayList<>();

    private Timeline masterTimeline;

    @FXML private BorderPane adminMainPane;
    @FXML private Pagination mainPagination;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            allAuctions = AuctionManager.getInstance().getMasterAuctionList();
            dividePage(allAuctions != null ? allAuctions : new ArrayList<>());
            registerAuctionListener();
            startMasterTimeline();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startMasterTimeline() {
        if (masterTimeline != null) masterTimeline.stop();
        masterTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    for (ControlAdminProductCard controller : activeControllers) {
                        if (controller != null) controller.refreshDisplay();
                    }
                })
        );
        masterTimeline.setCycleCount(Timeline.INDEFINITE);
        masterTimeline.play();
    }

    private void stopMasterTimeline() {
        if (masterTimeline != null) {
            masterTimeline.stop();
            masterTimeline = null;
        }
    }

    @FXML public void manageAuctionsBtn(ActionEvent event) { changeSceneSafely("/adminResource/AdminDashboard.fxml", true); }
    @FXML public void userManager(ActionEvent event) { changeSceneSafely("/adminResource/ManageUserAdmin.fxml", false); }

    @FXML
    public void logoutBtn(ActionEvent event) {
        try {
            cleanupResources();
            UserSession.clearAllData();
            ChangeScene.changeTOLogin(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openViewScreen(Auction auction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/adminResource/ViewForAdmin.fxml"));
            Parent viewScreen = loader.load();
            ControlView viewController = loader.getController();
            viewController.Display(auction);
            adminMainPane.setCenter(viewScreen);
        } catch (IOException e) {
            AlertShow.showAlert(Alert.AlertType.ERROR, "Lỗi Tải Trang", "Không thể mở giao diện chi tiết.");
        }
    }

    private void changeSceneSafely(String fxmlPath, boolean isRootChange) {
        try {
            cleanupResources();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            if (isRootChange) {
                Scene scene = adminMainPane.getScene();
                if (scene != null) scene.setRoot(root);
            } else {
                adminMainPane.setCenter(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertShow.showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Không thể tải giao diện.");
        }
    }

    private void cleanupResources() {
        unregisterAuctionListener();
        clearActiveControllers();
        stopMasterTimeline();
    }

    private void clearActiveControllers() {
        for (ControlAdminProductCard controller : activeControllers) {
            if (controller != null) controller.dispose();
        }
        activeControllers.clear();
    }

    private void registerAuctionListener() {
        updateListener = new AuctionUpdateListener() {
            @Override public void onAuctionAdded(Auction auction) { Platform.runLater(() -> dividePage(allAuctions)); }
            @Override public void onAuctionUpdated(Auction auction) {
                Platform.runLater(() -> {
                    for (ControlAdminProductCard controller : activeControllers) {
                        if (controller.getCurrentAuction() != null && controller.getCurrentAuction().getId().equals(auction.getId())) {
                            controller.setData(auction);
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
        if (listAuctions == null || listAuctions.isEmpty()) {
            mainPagination.setPageCount(1);
            mainPagination.setPageFactory((pageIndex) -> new ScrollPane(new FlowPane()));
            return;
        }
        int pageCount = (int) Math.ceil((double) listAuctions.size() / 8);
        mainPagination.setPageCount(pageCount);
        mainPagination.setPageFactory((pageIndex) -> createPage(listAuctions, pageIndex, 8));
    }

    private ScrollPane createPage(List<Auction> auctions, int pageIndex, int itemsPerPage) {
        clearActiveControllers(); // Dọn dẹp sạch sẽ danh sách cũ trước khi nạp trang mới

        FlowPane page = new FlowPane();
        page.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        page.setHgap(30);
        page.setVgap(12);
        page.prefWrapLengthProperty().bind(adminMainPane.widthProperty().subtract(280));

        int start = pageIndex * itemsPerPage;
        int end = Math.min(start + itemsPerPage, auctions.size());

        for (int i = start; i < end; i++) {
            Auction currentAuction = auctions.get(i);

            // 1. Gọi renderCard phiên bản rút gọn (chỉ truyền auction và container)
            ControlAdminProductCard controller = ControlAdminProductCard.renderCard(
                currentAuction,
                cardVisual -> page.getChildren().add(cardVisual)
            );

            if (controller != null) {

                controller.setOnCancelAction(e -> {
                    cancelAuction(controller.getCurrentAuction());
                });

                controller.setOnViewAction(e -> {
                    openViewScreen(controller.getCurrentAuction());
                });

                // Đưa controller vào MasterTimeline để quản lý giây
                activeControllers.add(controller);
            }
        }

        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return sp;
    }
    /// /////////////////////////////////////////////////////////////////
    public void cancelAuction(Auction auction) {
        if (auction == null) {
            AlertShow.showAlert(Alert.AlertType.ERROR, "Lỗi", "Auction không tồn tại.");
            return;
        }
        /// //////////////////////////////////////////////
        if ( auction.getStatus() == AuctionStatus.CANCELLED) {
            AlertShow.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Phiên đấu giá này đã được hủy trước đó rồi!");
            return;
        }
        /// //////////////////////////////////////////////
        try{
            boolean confirm = AlertShow.showConfirm(
                    "Xác nhận hủy",
                    "Bạn có chắc muốn hủy auction: " + auction.getItem().getName() + "?"
            );
            if (!confirm) {
                return;
            }

            Object response = AdminService.cancelAuctionService(auction);
            if (response instanceof String message) {
                if (message.startsWith("SUCCESS")) {
                    AlertShow.showAlert(Alert.AlertType.INFORMATION, "Thành công", message);
                } else {
                    AlertShow.showAlert(Alert.AlertType.ERROR, "Lỗi", message);
                }
            } else {
                AlertShow.showAlert(Alert.AlertType.ERROR, "Lỗi", "Phản hồi không hợp lệ từ server.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertShow.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hủy auction: " + e.getMessage());
        }

    }
    /// ///////////////////////////////////////////////////////////////////
}