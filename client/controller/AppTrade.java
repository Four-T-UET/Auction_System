package controller.etrade;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppTrade extends Application {
    public static void main(String[] args){
        launch(args);
    }

    public void start(Stage stage) throws Exception{
        // Setup test data
        setupTestData();

        // Tự động đăng nhập user "test" để test các tính năng cần UserSession (như Wallet, History)
        UserSession.setCurrentUser(new Clients("test", "test"));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/controller/etrade/MainDashboard.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setWidth(1100);
        stage.setHeight(700);
        stage.setScene(scene);
        stage.setTitle("ETrade");
        stage.show();
    }

    /**
     * Setup test data: users, auctions, products
     */
    private void setupTestData() {
        LoginDB.insertUser("test", "test");
        LoginDB.insertUser("seller", "seller");

        // Setup test auctions
        List<Auction> testAuctions = createTestAuctions();
        
        // Ensure not adding duplicates by checking first existing dummy data
        if(AuctionDB.getAuctions().isEmpty()) {
             testAuctions.forEach(AuctionDB::insertAuction);
        }
    }

    /**
     * Create test auctions
     */
    private List<Auction> createTestAuctions() {
        List<Auction> auctions = new ArrayList<>();

        // Test Electronics (Dùng loginImage.jpg để có hình ảnh hiện lên thay cho sample_image.jpg bị trống)
        Item electronics1 = new Electronics("MacBook Pro 16", "Laptop powerful", "/controller/etrade/loginImage.jpg");
        Auction auction1 = new Auction(electronics1, 1200.0, 50.0, LocalDateTime.now(), 7);
        auction1.startAuction();
        auctions.add(auction1);

        // Test Art
        Item art1 = new Art("Van Gogh Painting", "Reproduction", "/controller/etrade/loginImage.jpg");
        Auction auction2 = new Auction(art1, 300.0, 25.0, LocalDateTime.now(), 5);
        auction2.startAuction();
        auctions.add(auction2);

        // Test Vehicle
        Item vehicle1 = new Vehicle("Honda Civic 2023", "Sports car", "/controller/etrade/loginImage.jpg");
        Auction auction3 = new Auction(vehicle1, 15000.0, 500.0, LocalDateTime.now(), 10);
        auction3.startAuction();
        auctions.add(auction3);

        // Test Real Estate
        Item realEstate1 = new RealEstate("Beach House", "Luxury beachfront property", "/controller/etrade/loginImage.jpg");
        Auction auction4 = new Auction(realEstate1, 400000.0, 5000.0, LocalDateTime.now(), 14);
        auction4.startAuction();
        auctions.add(auction4);

        // Thêm vài sản phẩm nữa để test phân trang (Pagination)
        for (int i = 1; i <= 6; i++) {
            Item dummy = new Electronics("Dummy Item " + i, "Desc " + i, null);
            Auction dummyAu = new Auction(dummy, 10.0 * i, 2.0, LocalDateTime.now(), 3);
            dummyAu.startAuction();
            auctions.add(dummyAu);
        }

        return auctions;
    }
}
