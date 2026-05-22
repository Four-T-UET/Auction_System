package controller;

import auction.logic.enums.AuctionStatus;
import auction.logic.enums.ItemCategory;
import auction.logic.manager.AuctionManager;
import auction.logic.manager.AuctionUpdateListener;
import auction.logic.model.Auction;
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
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import service.ClientSocket;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ControlHistory implements Initializable {
    private final ObservableList<Auction> purchaseData = FXCollections.observableArrayList();
    private final ObservableList<Auction> biddingData = FXCollections.observableArrayList();
    private AuctionUpdateListener updateListener;
    private Timeline clock;
    private boolean cleanedUp;

    @FXML private TableView<Auction> tablePurchases;
    @FXML private TableColumn<Auction, String> itemColPurchases;
    @FXML private TableColumn<Auction, String> categoryColPurchases;
    @FXML private TableColumn<Auction, Double> winpriceColPurchases;
    @FXML private TableColumn<Auction, LocalDateTime> dateColPurchases;
    @FXML private TableColumn<Auction, AuctionStatus> statusColPurchases;
    @FXML private ComboBox<ItemCategory> categoryComboPurchases;
    @FXML private PieChart piechartPurchases;

    @FXML private TableView<Auction> tableSelling;
    @FXML private TableColumn<Auction, String> itemColSelling;
    @FXML private TableColumn<Auction, String> indexColSelling;
    @FXML private TableColumn<Auction, Double> currentbidColSelling;
    @FXML private TableColumn<Auction, LocalDateTime> enddateColSelling;
    @FXML private TableColumn<Auction, AuctionStatus> statusColSelling;
    @FXML private ComboBox<ItemCategory> typeComboSelling;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTables();
        setupColumns();
        setupCategories();
        refreshDerivedLists();
        registerAuctionListener();
        bindCleanupToWindowClose();
        startCountdownClock();
    }

    private void setupTables() {
        tablePurchases.setItems(purchaseData);
        tableSelling.setItems(biddingData);
    }

    private void setupColumns() {
        bindString(itemColPurchases, a -> textOf(a.getItem(), Item::getName));
        bindString(categoryColPurchases, a -> textOf(a.getItem(), i -> i.getCategory() == null ? "Unknown!" : i.getCategory().name()));
        bindObject(winpriceColPurchases, Auction::getCurrentPrice);
        bindObject(dateColPurchases, Auction::getFinishTime);
        bindObject(statusColPurchases, Auction::getStatus);
        dateColPurchases.setCellFactory(createCountdownCellFactory());

        bindString(itemColSelling, a -> textOf(a.getItem(), Item::getName));
        bindString(indexColSelling, Auction::getId);
        bindObject(currentbidColSelling, Auction::getCurrentPrice);
        bindObject(enddateColSelling, Auction::getFinishTime);
        bindObject(statusColSelling, Auction::getStatus);
        enddateColSelling.setCellFactory(createCountdownCellFactory());
    }

    private void setupCategories() {
        ObservableList<ItemCategory> categories = FXCollections.observableArrayList(ItemCategory.values());
        categoryComboPurchases.setItems(categories);
        if (typeComboSelling != null) typeComboSelling.setItems(categories);
    }

    private void refreshDerivedLists() {
        Clients currentUser = UserSession.getCurrentUser();
        List<Auction> master = AuctionManager.getInstance().getMasterAuctionList();

        purchaseData.setAll(master.stream().filter(a -> isPurchaseAuction(a, currentUser)).collect(Collectors.toList()));
        biddingData.setAll(master.stream().filter(a -> isBiddingAuction(a, currentUser)).collect(Collectors.toList()));
        loadPieChart();
    }

    private boolean isPurchaseAuction(Auction auction, Clients currentUser) {
        return hasStatus(auction, AuctionStatus.FINISHED, AuctionStatus.PAID)
                && (currentUser == null || sameUser(auction.getCurrentWinner(), currentUser));
    }

    private boolean isBiddingAuction(Auction auction, Clients currentUser) {
        return hasStatus(auction, AuctionStatus.PENDING, AuctionStatus.RUNNING)
                && (currentUser == null || sameUser(auction.getCurrentWinner(), currentUser));
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
        col.setCellValueFactory(cd -> new SimpleStringProperty(fn.apply(cd.getValue())));
    }

    private <T> void bindObject(TableColumn<Auction, T> col, java.util.function.Function<Auction, T> fn) {
        col.setCellValueFactory(cd -> new SimpleObjectProperty<>(fn.apply(cd.getValue())));
    }

    private String textOf(Item item, java.util.function.Function<Item, String> fn) {
        return item == null ? "Unknown" : fn.apply(item);
    }

    private Callback<TableColumn<Auction, LocalDateTime>, TableCell<Auction, LocalDateTime>> createCountdownCellFactory() {
        return col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCountdown(item));
            }
        };
    }

    private String formatCountdown(LocalDateTime endTime) {
        ClientSocket clientSocket = ClientSocket.getInstance();
        long endMillis = clientSocket.toServerEpochMillis(endTime);
        long remainingMillis = endMillis - clientSocket.getServerTimeMillis();
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
            if (newScene != null) {
                newScene.windowProperty().addListener((windowObs, oldWindow, newWindow) -> {
                    if (newWindow != null) newWindow.setOnHidden(e -> cleanup());
                });
            }
        });
    }

    public void cleanup() {
        if (cleanedUp) return;
        cleanedUp = true;
        unregisterAuctionListener();
        if (clock != null) clock.stop();
    }
}
