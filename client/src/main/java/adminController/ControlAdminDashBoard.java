package adminController;

import Utils.AlertShow;
import Utils.ChangeScene;
import auction.logic.manager.AuctionManager;
import auction.logic.manager.AuctionUpdateListener;
import auction.logic.model.Auction;
// ...existing code...
import clientController.UserSession;
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
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ControlAdminDashBoard implements Initializable {
    private ObservableList<Auction> allAuctions;
    private AuctionUpdateListener updateListener;
    // Quản lý danh sách các controller đang hiển thị trên trang hiện tại
    private final List<ControlAdminProductCard> activeControllers = new java.util.ArrayList<>();

    @FXML private BorderPane adminMainPane;
    @FXML private Pagination mainPagination;

    @FXML
    public void manageAuctionsBtn(ActionEvent event) {
        try {
            // Hủy đăng ký listener trước khi về
            unregisterAuctionListener();
            clearActiveControllers();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/adminResource/AdminDashboard.fxml"));
            Parent root = loader.load();
            Scene scene = adminMainPane.getScene();
            if (scene != null) {
                scene.setRoot(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void userManager(ActionEvent event) {
        try {
            // Hủy đăng ký listener trước khi về
            unregisterAuctionListener();
            clearActiveControllers();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/adminResource/ManageUserAdmin.fxml"));
            Parent root = loader.load();
            Scene scene = adminMainPane.getScene();
            if (scene != null) {
                scene.setRoot(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void logoutBtn(ActionEvent event) throws IOException {
        UserSession.clearAllData();
        ChangeScene.changeTOLogin(event);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        allAuctions = AuctionManager.getInstance().getMasterAuctionList();
        if(!allAuctions.isEmpty()){
            dividePage(allAuctions);
        }
        registerAuctionListener();
    }
    private void clearActiveControllers() {
        for (ControlAdminProductCard controller : activeControllers) {
            if (controller != null) {
                controller.dispose(); // Dừng hoàn toàn Timeline chạy ngầm của Card cũ
            }
        }
        activeControllers.clear();
    }
    private void registerAuctionListener() {
        updateListener = new AuctionUpdateListener() {
            @Override
            public void onAuctionAdded(Auction auction) {
                // Thêm mới sản phẩm -> Số lượng thay đổi, phải tính lại trang
                Platform.runLater(() -> dividePage(allAuctions));
            }

            @Override
            public void onAuctionUpdated(Auction auction) {
                //  CẬP NHẬT CỤC BỘ: Người ta nâng giá thì chỉ sửa đúng cái Card đó thôi!
                Platform.runLater(() -> {
                    for (ControlAdminProductCard controller : activeControllers) {
                        // Giả sử class Auction của bạn có hàm getId() hoặc một định danh tương đương
                        if (controller.getCurrentAuction() != null &&
                                controller.getCurrentAuction().getId().equals(auction.getId())) {

                            controller.setData(auction); // Đẩy data mới vào card, tự động đổi giá tiền!
                            break;
                        }
                    }
                });
            }

            @Override
            public void onAuctionsReplaced(List<Auction> auctions) {
                Platform.runLater(() -> dividePage(allAuctions));
            }

            @Override
            public void onAuctionRemoved(String auctionId) {
                // Xóa sản phẩm -> Số lượng thay đổi, phải vẽ lại trang
                Platform.runLater(() -> dividePage(allAuctions));
            }
        };

        AuctionManager.getInstance().registerListener(updateListener);
    }

    /**     * Hủy đăng ký listener khi scene đóng (tránh memory leak)     */
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
            mainPagination.setPageCount(1);
            mainPagination.setPageFactory((pageIndex) -> new ScrollPane(new FlowPane()));
            return;
        }
        mainPagination.setPageCount(pageCount);
        mainPagination.setPageFactory((pageIndex) -> createPage(listAuctions, pageIndex, NUM_ITEM));
    }

    private ScrollPane createPage(List<Auction> auctions, int pageIndex, int itemsPerPage) {
        // 1. DỌN DẸP SẠCH SẼ các Timeline cũ trước khi vẽ trang mới
        clearActiveControllers();

        int start = pageIndex * itemsPerPage;
        int end = Math.min(start + itemsPerPage, auctions.size());

        FlowPane page = new FlowPane();
        page.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        page.setHgap(30);
        page.setVgap(12);
        // Bind wrap length to pagination width so cards wrap when window resizes
        // Use available center area width (BorderPane width minus sidebar) so cards can wrap across full main pane
        // Sidebar approx width 240 + padding => subtract 280 for safe margin
        page.prefWrapLengthProperty().bind(adminMainPane.widthProperty().subtract(280));

        for (int i = start; i < end; i++) {
            VBox card = createAuctionCard(auctions.get(i));
            if (card != null) {
                // 2. LẤY CONTROLLER RA từ UserData và đưa vào danh sách quản lý
                ControlAdminProductCard controller = (ControlAdminProductCard) card.getUserData();
                if (controller != null) {
                    activeControllers.add(controller);
                }
                page.getChildren().add(card);
            }
        }

        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true);  // IMPORTANT: let FlowPane use pagination width
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return sp;
    }

    private VBox createAuctionCard(Auction auction) {
        try {
            VBox card = ControlAdminProductCard.renderCard(auction);
            if (card != null) {
                Button cancelBut = (Button) card.lookup("#cancel");
                Button viewBut =  (Button) card.lookup("#view");
                if (cancelBut != null &&  viewBut != null) {
                    cancelBut.setOnAction(e -> cancleAuction(auction));
                    viewBut.setOnAction(e -> openViewScreen(auction));
                }
            }
            return card;
        } catch (Exception e) {
            System.err.println("Error creating auction card: " + e.getMessage());
            e.printStackTrace();
            return null;
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
            e.printStackTrace();
        }
    }
    public void cancleAuction(Auction auction) {
        AlertShow.showAlert(Alert.AlertType.INFORMATION,"Erase", "DO you want to delete this auction");
    }

}
