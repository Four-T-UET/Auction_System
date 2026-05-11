package controller;

import auction.logic.enums.ItemCategory;
import auction.logic.model.Auction;
import javafx.collections.FXCollections;
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
    private List<Auction> allAuctions;

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
        //TODO: request đến server, lọc dữu liệu.
    }

    @FXML
    private void returnToMain(ActionEvent event) {
        try {
            //TODO: Tạo request đến server pull dữ liệu về, các auction có
            // sẽ load lại mainPane
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainDashboard.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Bidding.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DashBoardTOHistory.fxml"));
            Parent root = loader.load();
            mainPane.setCenter(root);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @FXML
    private void chageToWallet(ActionEvent event) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DashBoardTOWallet.fxml"));
            Parent root = loader.load();
            mainPane.setCenter(root);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    @FXML
    private void chageToSelling(ActionEvent event) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DashBoardTOSelling.fxml"));
            Parent root = loader.load();
            mainPane.setCenter(root);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    private void changeToAccount(ActionEvent event) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DashBoardTOAccount.fxml"));
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
        // Load auctions từ config
        //TODO: tạo request đến server để có thể lấy dữ liệu
//        dividePage(allAuctions);

    }

    private void dividePage(List<Auction> listAuctions) {
        final int NUM_ITEM = 6;
        int pageCount = (int) Math.ceil((double) listAuctions.size() / NUM_ITEM);
        pagination.setPageCount(pageCount);
        pagination.setPageFactory((pageIndex) -> createPage(listAuctions, pageIndex, NUM_ITEM));
    }

    private ScrollPane createPage(List<Auction> auctions, int pageIndex, int itemsPerPage) {
        int start = pageIndex * itemsPerPage;
        int end = Math.min(start + itemsPerPage, auctions.size());

        FlowPane page = new FlowPane();
        for (int i = start; i < end; i++) {
            page.getChildren().add(createAuctionCard(auctions.get(i)));
        }

        return new ScrollPane(page);
    }

    private VBox createAuctionCard(Auction auction) {
        try {
            return ControlProductCard.renderCard(auction,false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
