package clientController;

import auction.logic.enums.AuctionStatus;
import auction.logic.enums.ItemCategory;
import stateManager.AuctionManager;
import stateManager.AuctionUpdateListener;
import auction.logic.manager.BidHistory;
import auction.logic.model.Auction;
import auction.logic.model.BidTransaction;
import auction.logic.model.Clients;
import auction.logic.model.Item;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;


import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import service.TimeSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stateManager.UserSession;

public class ControlHistory implements Initializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ControlHistory.class);
    private final ObservableList<Auction> purchaseData = FXCollections.observableArrayList();

    private final ObservableList<Auction> sellingData = FXCollections.observableArrayList();

    private AuctionUpdateListener updateListener;
    private Timeline clock;
    private boolean cleanedUp;
    private Auction selectedSellingAuction;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML private TableView<Auction> tablePurchases;
    @FXML private TableColumn<Auction, String> itemColPurchases;
    @FXML private TableColumn<Auction, String> categoryColPurchases;
    @FXML private TableColumn<Auction, Double> winpriceColPurchases;
    @FXML private TableColumn<Auction, LocalDateTime> timeleftPurchase;
    @FXML private TableColumn<Auction, String> statusColPurchases;
    @FXML private PieChart piechartPurchases;
    @FXML private ImageView imagePurchaseIcon;

    @FXML private TableView<Auction> tableSelling;
    @FXML private TableColumn<Auction, String> itemColSelling;
    @FXML private TableColumn<Auction, String> indexColSelling;
    @FXML private TableColumn<Auction, Double> currentbidColSelling;
    @FXML private TableColumn<Auction, LocalDateTime> timeLeftSelling;
    @FXML private LineChart<String, Number> lineChart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTables();
        setupColumns();
        refreshDerivedLists();
        registerAuctionListener();
        bindCleanupToWindowClose();
        startCountdownClock();
        setupTableSelection();
    }

    private void setupTables() {
        tablePurchases.setItems(purchaseData);
        // Đã sửa: truyền sellingData vào tableSelling
        tableSelling.setItems(sellingData);
    }

    private void setupColumns() {
        bindString(itemColPurchases, a -> textOf(a.getItem(), Item::getName));
        bindString(categoryColPurchases, a -> textOf(a.getItem(), i -> i.getCategory() == null ? "Unknown!" : i.getCategory().name()));
        bindObject(winpriceColPurchases, Auction::getCurrentPrice);
        bindObject(timeleftPurchase, Auction::getFinishTime);
        bindString(statusColPurchases, a -> {
            if(a.getStatus() == AuctionStatus.FINISHED && a.getCurrentWinner() == null) {
                return "UN_SOLDED";
            }
            return a.getStatus() != null ? a.getStatus().name() : "UNKNOWN";
        });
        timeleftPurchase.setCellFactory(createCountdownCellFactory());

        bindString(itemColSelling, a -> textOf(a.getItem(), Item::getName));
        bindString(indexColSelling, Auction::getId);
        bindObject(currentbidColSelling, Auction::getCurrentPrice);
        bindObject(timeLeftSelling, Auction::getFinishTime);
        timeLeftSelling.setCellFactory(createCountdownCellFactory());
    }

    private void refreshDerivedLists() {
        Clients currentUser = UserSession.getCurrentUser();
        List<Auction> master = AuctionManager.getInstance().getMasterAuctionList();

        if (master == null) return;
        purchaseData.setAll(master.stream().filter(a -> isPurchaseAuction(a, currentUser)).collect(Collectors.toList()));

        sellingData.setAll(master.stream().filter(a -> isSellingAuction(a, currentUser)).collect(Collectors.toList()));
        loadPieChart();
        loadBidChart();
    }

    private boolean isPurchaseAuction(Auction auction, Clients currentUser) {
        return hasStatus(auction, AuctionStatus.FINISHED, AuctionStatus.PAID)
            && (currentUser == null || sameUser(auction.getCurrentWinner(), currentUser));
    }

    // Đã sửa: Tên hàm từ isBiddingAuction thành isSellingAuction
    private boolean isSellingAuction(Auction auction, Clients currentUser) {
        return hasStatus(auction, AuctionStatus.PENDING, AuctionStatus.RUNNING, AuctionStatus.FINISHED, AuctionStatus.CANCELLED, AuctionStatus.PAID)
            && (currentUser == null || sameUser(auction.getSeller(), currentUser));
    }

    private boolean hasStatus(Auction auction, AuctionStatus... statuses) {
        return auction != null && Arrays.asList(statuses).contains(auction.getStatus());
    }

    private boolean sameUser(Clients a, Clients b) {
        return a != null && b != null && Objects.equals(a.getUsername(), b.getUsername());
    }

    private void loadPieChart() {
        Map<ItemCategory, Long> counts = purchaseData.stream()
            .map(Auction::getItem)
            .filter(Objects::nonNull)
            .map(Item::getCategory)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        if (total == 0) {
            piechartPurchases.setData(FXCollections.observableArrayList());
            return;
        }

        piechartPurchases.setData(FXCollections.observableArrayList(
            counts.entrySet().stream()
                .map(e -> new PieChart.Data(
                    e.getKey().name() + " (" + String.format("%.1f", e.getValue() * 100.0 / total) + "%)",
                    e.getValue()))
                .collect(Collectors.toList())
        ));
    }

    private void bindString(TableColumn<Auction, String> col, java.util.function.Function<Auction, String> fn) {
        col.setCellValueFactory(cd -> {
            if (cd == null || cd.getValue() == null) return new SimpleStringProperty("");
            try {
                return new SimpleStringProperty(fn.apply(cd.getValue()));
            } catch (Exception e) {
                return new SimpleStringProperty("Error");
            }
        });
    }

    private <T> void bindObject(TableColumn<Auction, T> col, java.util.function.Function<Auction, T> fn) {
        col.setCellValueFactory(cd -> {
            if (cd == null || cd.getValue() == null) return new SimpleObjectProperty<>(null);
            try {
                return new SimpleObjectProperty<>(fn.apply(cd.getValue()));
            } catch (Exception e) {
                return new SimpleObjectProperty<>(null);
            }
        });
    }

    private String textOf(Item item, java.util.function.Function<Item, String> fn) {
        return item == null ? "Unknown" : fn.apply(item);
    }

    private Callback<TableColumn<Auction, LocalDateTime>, TableCell<Auction, LocalDateTime>> createCountdownCellFactory() {
        return col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);

                // 1. Nếu ô này trống hoặc không có dữ liệu, xóa chữ và dừng xử lý
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                // 2. Lấy đối tượng Auction của dòng hiện tại ra
                Auction auction = getTableRow().getItem();

                // 3. Lấy chuỗi chữ hiển thị (Đã xử lý Cancelled / Ended ở hàm formatCountdown)
                String countdownText = formatCountdown(auction);
                setText(countdownText);

            }
        };
    }
    /// //////////////////////////
    private String formatCountdown(Auction auction) {
        if (auction == null) return "N/A";

        // !!! CHỐT CHẶN: Nếu phiên đấu giá đã bị HỦY, dừng đếm ngược ngay lập tức
        if (auction.getStatus() == AuctionStatus.CANCELLED) {
            return "Auction cancelled";
        }

        // Nếu không bị hủy, lấy endTime ra và chuyển tiếp cho hàm format cũ xử lý
        return formatCountdown(auction.getFinishTime());
    }
    /// ///////////////////////////////////

    private String formatCountdown(LocalDateTime endTime) {
        TimeSyncService timeSyncService = TimeSyncService.getInstance();
        long finishMillis = timeSyncService.toServerEpochMillis(endTime);
        long remainingMillis = finishMillis - timeSyncService.getServerTimeMillis();
        if (remainingMillis <= 0) return "Auction ended";

        Duration d = Duration.ofMillis(remainingMillis);
        long totalSeconds = Math.max(0, d.getSeconds());
        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds % (24 * 3600)) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return days > 0
            ? String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds)
            : String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void startCountdownClock() {
        clock = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> {
            tablePurchases.refresh();
            tableSelling.refresh();
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void registerAuctionListener() {
        updateListener = new AuctionUpdateListener() {
            @Override public void onAuctionAdded(Auction auction) { runOnFxThread(ControlHistory.this::refreshDerivedLists); }
            @Override public void onAuctionUpdated(Auction auction) { runOnFxThread(ControlHistory.this::refreshDerivedLists); }
            @Override public void onAuctionsReplaced(List<Auction> auctions) { runOnFxThread(ControlHistory.this::refreshDerivedLists); }
            @Override public void onAuctionRemoved(String auctionId) { runOnFxThread(ControlHistory.this::refreshDerivedLists); }
        };
        AuctionManager.getInstance().registerListener(updateListener);
    }

    private void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) action.run();
        else Platform.runLater(action);
    }

    public void unregisterAuctionListener() {
        if (updateListener != null) {
            AuctionManager.getInstance().unregisterListener(updateListener);
            updateListener = null;
        }
    }

    private void bindCleanupToWindowClose() {
        tablePurchases.sceneProperty().addListener((sceneObs, oldScene, newScene) -> {
            //  Nếu newScene == null nghĩa là tab này đã bị tháo khỏi màn hình chính
            if (newScene == null) {
                cleanup();
                return;
            }

            // Nếu vẫn còn trên màn hình thì lắng nghe sự kiện đóng cửa sổ ứng dụng
            newScene.windowProperty().addListener((windowObs, oldWindow, newWindow) -> {
                if (newWindow != null) newWindow.setOnHidden(e -> cleanup());
            });
        });
    }
    private void setupTableSelection() {
        // Lắng nghe sự kiện click chọn dòng trên tablePurchases
        tablePurchases.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Nếu có hàng được chọn -> hiển thị ảnh của phiên đấu giá đó
                updateRightSideImage(newSelection);
            } else {
                // Nếu không chọn hàng nào (hoặc bảng trống) -> hiển thị ảnh mặc định
                setPurchaseDefaultImage();
            }
        });

        // Lắng nghe sự kiện click chọn dòng trên tableSelling -> cập nhật LineChart
        tableSelling.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
            selectedSellingAuction = newSelection;
            loadBidChart();
        });
    }
    private void updateRightSideImage(Auction auction) {
        if (auction == null || auction.getItem() == null) {
            setPurchaseDefaultImage();
            return;
        }

        byte[] imageBytes = auction.getItem().getImageBytes();

        // 1. Thử load ảnh từ mảng bytes của Database trước
        if (imageBytes != null && imageBytes.length > 0) {
            try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(imageBytes)) {
                Image image = new Image(bais);
                imagePurchaseIcon.setImage(image);
                return; // Đã load thành công thì dừng lại
            } catch (Exception e) {
                LOGGER.warn("Lỗi chuyển đổi ảnh sản phẩm: " + e.getMessage(), e);
            }
        }

        // 2. Nếu không có mảng bytes hoặc lỗi, tự động chuyển về ảnh mặc định
        setPurchaseDefaultImage();
    }

    private void setPurchaseDefaultImage() {
        try {
            // Sử dụng chung đường dẫn ảnh default giống các class cũ của bạn
            java.io.InputStream stream = getClass().getResourceAsStream("/image/loginImage.jpg");
            if (stream != null) {
                imagePurchaseIcon.setImage(new Image(stream));
            } else {
                imagePurchaseIcon.setImage(null);
            }
        } catch (Exception e) {
            LOGGER.warn("Không thể load ảnh mặc định: " + e.getMessage(), e);
            imagePurchaseIcon.setImage(null);
        }
    }

    private void loadBidChart() {
        if (lineChart == null) return;

        lineChart.getData().clear();

        Auction auction = selectedSellingAuction;
        if (auction == null) {
            // Nếu chưa chọn auction nào, thử lấy auction đầu tiên trong sellingData
            if (!sellingData.isEmpty()) {
                auction = sellingData.get(0);
            } else {
                return;
            }
        }

        BidHistory bidHistory = auction.getBidHistory();
        if (bidHistory == null) return;

        List<BidTransaction> transactions = bidHistory.getTransactions();
        if (transactions == null || transactions.isEmpty()) return;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Bid History - " + (auction.getItem() != null ? auction.getItem().getName() : auction.getId()));

        // === TÍNH TOÁN GIỚI HẠN 10 CHẤM CUỐI ===
        // Nếu tổng số lượt bid ít hơn 10, bắt đầu từ 0.
        // Nếu nhiều hơn 10, ví dụ có 25 lượt bid, vòng lặp sẽ chạy từ vị trí số 15 (25 - 10).
        int startIndex = Math.max(0, transactions.size() - 10);

        for (int i = startIndex; i < transactions.size(); i++) {
            BidTransaction tx = transactions.get(i);
            String timeLabel = tx.getTime() != null ? tx.getTime().format(TIME_FORMAT) : "N/A";
            series.getData().add(new XYChart.Data<>(timeLabel, tx.getAmount()));
        }

        lineChart.getData().add(series);
    }

    public void cleanup() {
        if (cleanedUp) return;
        cleanedUp = true;
        unregisterAuctionListener();
        if (clock != null) clock.stop();
    }
}