package controller.etrade;

import enums.ItemCategory;
import model.Auction;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ControlDashBoard implements Initializable {
    private List<Auction> allAuctions;

    @FXML
    private BorderPane mainPane;
    @FXML
    private Button acountBut;
    @FXML
    private Button historyBut;
    @FXML
    private Button walletBut;
    @FXML
    private Button sellingBut;
    @FXML
    private Button settingBut;
    @FXML
    private Button mainScene;
    @FXML
    private Button findItem;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<ItemCategory> categoryComBox;
    @FXML
    private Pagination pagination;

    @FXML
    public void chooseCategory(){

    }
    @FXML
    public void Find(ActionEvent event) {
        ItemCategory type = categoryComBox.getValue();
        String searchData = searchField.getText().toLowerCase();
//        compareData(type, searchData);
    }

    @FXML
    private void returnToMain(ActionEvent event) {
        // Because the main scene's center has the find bar (VBox/HBox) + pagination
        // we can reload the MainDashboard.fxml to reset everything back
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/etrade/MainDashboard.fxml"));
            Parent root = loader.load();
            Scene scene = mainPane.getScene();
            if (scene != null) {
                scene.setRoot(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // list<auction> này đã được lọc qua
    public void setAuctions(List<Auction> AuctionsDB) {
        this.allAuctions = AuctionsDB;
    }


    /**
     * Load Bidding screen vào mainPane khi nhấn Bid Now
     */
    private void openBiddingScreen(Auction auction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/etrade/Bidding.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/etrade/DashBoardTOHistory.fxml"));
            Parent root = loader.load();
            mainPane.setCenter(root);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @FXML
    private void chageToWallet(ActionEvent event) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/etrade/DashBoardTOWallet.fxml"));
            Parent root = loader.load();
            mainPane.setCenter(root);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    @FXML
    private void chageToSelling(ActionEvent event) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/etrade/DashBoardTOSelling.fxml"));
            Parent root = loader.load();
            mainPane.setCenter(root);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    private void changeToAccount(ActionEvent event) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/etrade/DashBoardTOAccount.fxml"));
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
        categoryComBox.getSelectionModel().select(ItemCategory.ALL);
        findItem.setOnAction(event ->{
            Find(event);
        });
        historyBut.setOnAction(event ->{
            chageToHistory(event);
        });
        walletBut.setOnAction(event ->{
            chageToWallet(event);
        });
        sellingBut.setOnAction(event ->{
            chageToSelling(event);
        });
        acountBut.setOnAction(event ->{
            changeToAccount(event);
        });
        mainScene.setOnAction(event ->{
            returnToMain(event);
        });
        // Load auctions từ database
        this.allAuctions = AuctionDB.getAuctions();
        dividePage(allAuctions);

    }

    private void dividePage(List<Auction> listAuctions){
        final int NUM_ITEM = 9;
        int pageCount = (int) Math.ceil((double) listAuctions.size() / NUM_ITEM);
        pagination.setPageCount((pageCount));
        pagination.setPageFactory((Integer pageIndex) -> {
            int start = pageIndex*NUM_ITEM;
            int end = Math.min(start + NUM_ITEM, listAuctions.size());

            FlowPane page = new FlowPane();
            page.setHgap(25.0);
            page.setVgap(25.0);
            page.setPadding(new javafx.geometry.Insets(30));
            page.setAlignment(javafx.geometry.Pos.TOP_CENTER);

            // Đảm bảo FlowPane tự động chiếm hết chiều ngang có thể
            page.setMaxWidth(Double.MAX_VALUE);

            for(int i = start;i < end;i++){
                page.getChildren().add(ControlProductCard.renderCard(listAuctions.get(i),this::openBiddingScreen));
            }

            // Bọc FlowPane trong ScrollPane để khi số lượng sản phẩm lớn sẽ không bị mất trên màn hình nhỏ
            javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(page);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #f4f7f6;");

            return scrollPane;
        });
    }
}
