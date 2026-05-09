package controller;

import auction.logic.enums.AuctionStatus;
import auction.logic.enums.ItemCategory;
import auction.logic.model.Auction;
import auction.logic.model.Item;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.Duration;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import javafx.util.Callback;

public class ControlHistory implements Initializable {
    //_______________________
    // FOR PURCHASES
    //_______________________
    /*
    Cách thông tin được chạy trong TableView:
        + Đầu tiên: chạy gọi initialized() -> mục đich để nạp các nut bấm, ... vào
    trong bộ nhớ. setDataForColumn(), được gọi đó chính là hướng dẫn cho các data sau
    này được ghi như thế nào?
           -------> NOTE: lúc này dữ liệu trong bảng vẫn trống

        +Tiếp theo: mới nhận data từ MainBoard || Database rồi mới gọi loadDataForPurchases();
     */
    private List<Auction> auctionList = new ArrayList<>();
    public void setAuctionList(List<Auction> auctionList){
        this.auctionList = auctionList;
        loadDataForPurchases();
        loadPieChart();
    }

    @FXML
    private TableView<Auction> tablePurchases;
    @FXML
    private TableColumn<Auction,String> itemColPurchases;
    @FXML
    private TableColumn<Auction,String> categoryColPurchases;
    @FXML
    private TableColumn<Auction,Double> winpriceColPurchases;
    @FXML
    private TableColumn<Auction, LocalDateTime> dateColPurchases;
    @FXML
    private TableColumn<Auction,AuctionStatus> statusColPurchases;
    @FXML
    private ComboBox<ItemCategory> categoryComboPurchases;
    @FXML
    private ImageView imagePurchaseIcon;
    @FXML
    private PieChart piechartPurchases;

    //observableArrayList(Callback extractor)	tự động update khi property con thay đổi
    // cần phải thay đổi ngay
    public void setDataForColumn(){
        itemColPurchases.setCellValueFactory(cellData -> {
            //cellData mang thông tin Auction hiển thị lên trên màn hình
            // -> getValue() trả ra Aution của hàng;
            Item product = cellData.getValue().getItem();
            String name = (product != null) ? product.getName() : "Unknown!";
            //JavaFx không chấp nhận return name
            //Do nó bảng tableView chỉ chấp nhận đối tượng trả ve
            // la -> "Property"
            return new SimpleStringProperty(name);
        });

        categoryColPurchases.setCellValueFactory(cellData -> {
            Item product = cellData.getValue().getItem();
            String category = (product != null && product.getCategory() != null)
                    ? product.getCategory().name()
                    : "Unknown!";
            return new SimpleStringProperty(category);
        });

        winpriceColPurchases.setCellValueFactory(cellData ->{
            return cellData.getValue().currentPriceProperty().asObject();
        });

        dateColPurchases.setCellValueFactory(cellData -> {
            return new SimpleObjectProperty<>(cellData.getValue().getFinishTime());
        });

        dateColPurchases.setCellFactory(getCountdownCellFactory());

        statusColPurchases.setCellValueFactory(cellData -> {
            return new SimpleObjectProperty<>(cellData.getValue().getStatus());
        });
    }
    //
    //
    public void loadImage(String path){
        if (path == null || imagePurchaseIcon == null) {
            return;
        }
        var stream = getClass().getResourceAsStream(path);
        if (stream != null) {
            Image image = new Image(stream);
            imagePurchaseIcon.setImage(image);
        }
    }
    //
    public void loadPieChart(){
        Map<ItemCategory,Integer> count = new HashMap<>();
        for(Auction auction: auctionList){
            Item product = auction.getItem();
            if (product == null || product.getCategory() == null) {
                continue;
            }
            ItemCategory temp =  product.getCategory();
            count.put(temp,count.getOrDefault(temp,0) + 1);
        }
        int total = 0;
        for(Integer value: count.values()){
            total += value;
        }
        if(total == 0){
            piechartPurchases.setData(FXCollections.observableArrayList());
            return;// tranh chia cho 0
        }
        //load data
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        //-> PieChart.Data là 1 lát trong biểu đồ tròn
        // VD:
        //PieChart.Data data = new PieChart.Data("ELECTRONICS", 5);
        // ý nghĩa là lượng Electronics là 5
        // tạo các List chứa PieChart.Data -> chứa những lát cắt
        for(Map.Entry<ItemCategory,Integer> entry: count.entrySet()){
            double percent = (double) entry.getValue() / total * 100;
            String label = entry.getKey().name() + " (" + String.format("%.1f", percent) + "%)";
            pieData.add(new PieChart.Data(label, entry.getValue()));
        }

        piechartPurchases.setData(pieData);
    }

    //-------------
    //FOR SELLING
    //-------------
    private List<Auction> auctionSelling = new ArrayList<>();
    public void setAuctionSelling(List<Auction> auctionSelling){
        this.auctionSelling = auctionSelling;
        loadDataForSelling();
    }
    @FXML
    private TableView<Auction> tableSelling;
    @FXML
    private TableColumn<Auction,String> itemColSelling;
    @FXML
    private TableColumn<Auction,String> indexColSelling;
    @FXML
    private TableColumn<Auction,Double> currentbidColSelling ;
    @FXML
    private TableColumn<Auction,LocalDateTime> enddateColSelling;
    @FXML
    private TableColumn<Auction,AuctionStatus> statusColSelling;
    @FXML
    private ComboBox<ItemCategory> typeComboSelling;

    public void setDataForSelling(){
        itemColSelling.setCellValueFactory(cellData ->{
             Item item = cellData.getValue().getItem();
             String name = (item != null) ? item.getName() : "Unknown";
            return new SimpleStringProperty(name);
        });
        
        indexColSelling.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getId());
        });

        currentbidColSelling.setCellValueFactory(cellData -> {
            return cellData.getValue().currentPriceProperty().asObject();
        });
        
        enddateColSelling.setCellValueFactory(cellData -> {
            return new SimpleObjectProperty<>(cellData.getValue().getFinishTime());
        });

        enddateColSelling.setCellFactory(getCountdownCellFactory());
        
        statusColSelling.setCellValueFactory(cellData -> {
            return new SimpleObjectProperty<>(cellData.getValue().getStatus());
        });
        
    }
    public void setScatterChart(Auction sellingData){
        CategoryAxis xAxis = new CategoryAxis(); // category axis for String
        xAxis.setLabel("Time");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Current Bid");
        ScatterChart<String, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
        scatterChart.setTitle("Auction Selling Data");

    }

    private Callback<TableColumn<Auction, LocalDateTime>, TableCell<Auction, LocalDateTime>> getCountdownCellFactory() {
        return column -> new TableCell<Auction, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    LocalDateTime now = LocalDateTime.now();
                    if (now.isAfter(item)) {
                        setText("Ended");
                    } else {
                        Duration duration = Duration.between(now, item);
                        long days = duration.toDays();
                        long hours = duration.toHoursPart();
                        long minutes = duration.toMinutesPart();
                        long seconds = duration.toSecondsPart();

                        if (days > 0) {
                            setText(String.format("%d ngày %02d:%02d:%02d", days, hours, minutes, seconds));
                        } else {
                            setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                        }
                    }
                }
            }
        };
    }

    //khởi tạo
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //-----PURCHASES------
        setDataForColumn();
        setDataForSelling();
        setDataForCategory();
        
        loadDataForPurchases();
        loadDataForSelling();
        
        loadPieChart();
        loadImage("/controller/etrade/loginImage.jpg");

        // --- CHẠY ĐỒNG HỒ ĐẾM NGƯỢC ---
        Timeline clock = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> {
            tablePurchases.refresh();
            if (tableSelling != null) {
                tableSelling.refresh();
            }
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    public void loadDataForPurchases(){
        ObservableList<Auction> auctionObservableList = FXCollections.observableArrayList(this.auctionList);
        tablePurchases.setItems(auctionObservableList);
    }

    public void loadDataForSelling(){
        ObservableList<Auction> auctionObservableSelling = FXCollections.observableArrayList(this.auctionSelling);
        tableSelling.setItems(auctionObservableSelling);
    }

    public void setDataForCategory(){
        ObservableList<ItemCategory> itemCategories = FXCollections.observableArrayList(ItemCategory.values());
        //ItemCategory.value() trả về tất cả các giá trị
        //ta không cần phải liệt kê thủ công ARTS,VEHICLE,ELECTRONICS
        categoryComboPurchases.setItems(itemCategories);
        if(typeComboSelling != null) {
            typeComboSelling.setItems(itemCategories);
        }
    }
}
