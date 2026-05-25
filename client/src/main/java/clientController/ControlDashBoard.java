package clientController;

import auction.logic.enums.ItemCategory;
import auction.logic.manager.AuctionManager;
import auction.logic.manager.AuctionUpdateListener;
import auction.logic.model.Auction;
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
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class ControlDashBoard implements Initializable {
    private ObservableList<Auction> allAuctions;
    private AuctionUpdateListener updateListener;
    private final List<ControlProductCard> activeControllers = new java.util.ArrayList<>();

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
        String searchData = searchField.getText().toLowerCase();
        //TODO: lọc dư liệu từ allAuctions
    }

    @FXML
    private void returnToMain(ActionEvent event) {
        try {
            // Hủy đăng ký listener trước khi về
            unregisterAuctionListener();
            clearActiveControllers();
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
    /**
     * * Cập nhật auction list (gọi từ parent scene)
     * */
    public void setAuctions(List<Auction> auctionsDB) {
        if (auctionsDB != null) {
            allAuctions.setAll(auctionsDB);
        }
    }

    /**
     * Load Bidding screen vào mainPane khi nhấn Bid Now
     */
    private void openBiddingScreen(Auction auction) {
        try {
            clearActiveControllers();
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
            unregisterAuctionListener();
            clearActiveControllers();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientResource/DashBoardTOHistory.fxml"));
            Parent root = loader.load();
            mainPane.setCenter(root);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @FXML
    private void chageToWallet(ActionEvent event) {
        try{
            unregisterAuctionListener();
            clearActiveControllers();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientResource/DashBoardTOWallet.fxml"));
            Parent root = loader.load();
            mainPane.setCenter(root);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    @FXML
    private void chageToSelling(ActionEvent event) {
        try{
            unregisterAuctionListener();
            clearActiveControllers();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientResource/DashBoardTOSelling.fxml"));
            Parent root = loader.load();
            mainPane.setCenter(root);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    private void changeToAccount(ActionEvent event) {
        try{
            unregisterAuctionListener();
            clearActiveControllers();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientResource/DashBoardTOAccount.fxml"));
            Parent root = loader.load();
            mainPane.setCenter(root);
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    private void findFromSearch(){

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        categoryComBox.setItems(FXCollections.observableArrayList(ItemCategory.values()));
        categoryComBox.getSelectionModel().select(ItemCategory.REAL_ESTATE);
        allAuctions = AuctionManager.getInstance().getMasterAuctionList();
        if(!allAuctions.isEmpty()){
            dividePage(allAuctions);
        }
        // Đăng ký listener để tự động cập nhật khi có realtime updates từ server
        registerAuctionListener();
    }
    private void clearActiveControllers() {
        for (ControlProductCard controller : activeControllers) {
            if (controller != null) {
                controller.dispose(); // Tắt Timeline ngầm của Card
            }
        }
        activeControllers.clear();
    }

    private void registerAuctionListener() {
        updateListener = new AuctionUpdateListener() {
            @Override public void onAuctionAdded(Auction auction) { Platform.runLater(() -> dividePage(allAuctions)); }

            @Override
            public void onAuctionUpdated(Auction auction) {
                //  CHỈ CẬP NHẬT CÁI CARD BỊ THAY ĐỔI GIÁ, KHÔNG VẼ LẠI CẢ TRANG
                Platform.runLater(() -> {
                    for (ControlProductCard controller : activeControllers) {
                        if (controller.getCurrentAuction() != null &&
                                controller.getCurrentAuction().getId().equals(auction.getId())) {
                            controller.setData(auction); // Đẩy thông tin mới (giá mới) vào card
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
            pagination.setPageCount(1);
            pagination.setPageFactory((pageIndex) -> new ScrollPane(new FlowPane()));
            return;
        }
        pagination.setPageCount(pageCount);
        pagination.setPageFactory((pageIndex) -> createPage(listAuctions, pageIndex, NUM_ITEM));
    }

    private ScrollPane createPage(List<Auction> auctions, int pageIndex, int itemsPerPage) {
        int start = pageIndex * itemsPerPage;
        int end = Math.min(start + itemsPerPage, auctions.size());

        FlowPane page = new FlowPane();
        page.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        page.setHgap(30);
        page.setVgap(12);
        // Bind wrap length to pagination width so cards wrap when window resizes
        page.prefWrapLengthProperty().bind(pagination.widthProperty().subtract(20));

        for (int i = start; i < end; i++) {
            VBox card = createAuctionCard(auctions.get(i));
            if (card != null) {
                // Lấy controller ra từ UserData và lưu lại
                ControlProductCard controller = (ControlProductCard) card.getUserData();
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
            VBox card = ControlProductCard.renderCard(auction, false);
            if (card != null) {
                Button bidBtn = (Button) card.lookup("#bidBut");
                if (bidBtn != null) {
                    bidBtn.setOnAction(e -> openBiddingScreen(auction));
                }
            }
            return card;
        } catch (Exception e) {
            System.err.println("Error creating auction card: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}
