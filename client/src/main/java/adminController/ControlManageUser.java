package adminController;

import auction.logic.manager.AuctionUpdateListener;
import auction.logic.model.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.net.URL;
import java.util.ResourceBundle;


public class ControlManageUser implements Initializable {
    private AuctionUpdateListener updateListener;
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    @FXML private Label totalUsersLabel;
    @FXML private TextField searchField;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> idCol;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, Void> deleteCol;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        setUpColumn();
        setupDeleteColumn();
    }

    public void setUpColumn(){
        bindString(idCol, user -> textOf(user, User::getId));
        bindString(usernameCol, user -> textOf(user, User::getUsername));
    }
    private void setupDeleteColumn() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = param -> new TableCell<>() {

            // Tạo nút bấm
            private final Button deleteBtn = new Button("🗑");

            {
                // 1. Thêm Style cho nút giống với FXML của bạn (chữ đỏ, trỏ chuột hình bàn tay)
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d32f2f; -fx-font-size: 18px; -fx-cursor: hand;");

                                // 2. Bắt sự kiện khi click vào nút
                deleteBtn.setOnAction(event -> {
                    // Lấy ra object User của cái hàng mà chứa nút bị bấm
                    User selectedUser = getTableView().getItems().get(getIndex());

                    // Gọi hàm xử lý xóa
                    confirmAndDelete(selectedUser);
                });
            }

            // 3. Render nút lên giao diện
            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    // Nếu hàng trống thì không hiển thị gì cả
                    setGraphic(null);
                } else {
                    // Nếu có dữ liệu thì chèn nút vào
                    setGraphic(deleteBtn);
                }
            }
        };

        // Gắn CellFactory vào cột Delete
        deleteCol.setCellFactory(cellFactory);
    }

    private void confirmAndDelete(User selectedUser) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa người dùng: " + selectedUser.getUsername() + "?");
        alert.setContentText("Hành động này không thể hoàn tác.");
        //TODO: gửi reqest ở đây
    }

    private String textOf(User user, java.util.function.Function<User, String> fn) {
        return user == null ? "Unknown" : fn.apply(user);
    }

    private void bindString(TableColumn<User, String> col, java.util.function.Function<User, String> fn) {
        col.setCellValueFactory(cd -> {
            if (cd == null || cd.getValue() == null) return new SimpleStringProperty("");
            try {
                return new SimpleStringProperty(fn.apply(cd.getValue()));
            } catch (Exception e) {
                return new SimpleStringProperty("Error");
            }
        });
    }



}
