package adminController;

import Utils.AlertShow;
import stateManager.UserManager;
import stateManager.UserUpdateListener;
import auction.logic.model.User;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import service.AdminService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class ControlManageUser implements Initializable {
    private UserUpdateListener updateListener;
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    @FXML private Label totalUsersLabel;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> idCol;
    @FXML private TableColumn<User, String> usernameCol;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        setUpColumn();
        userTable.setItems(userList);

        registerUserListener();
        AdminService.pullUsers();
        refreshDerivedLists();
    }

    public void setUpColumn(){
        bindString(idCol, user -> textOf(user, User::getId));
        bindString(usernameCol, user -> textOf(user, User::getUsername));
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

    /**
     * Đăng ký listener với UserManager để nhận realtime updates
     */
    private void registerUserListener() {
        // Tạo listener
        updateListener = new UserUpdateListener() {
            @Override
            public void onUserAdded(User user) {
                runOnFxThread(() -> refreshDerivedLists());
            }

            @Override
            public void onUserUpdated(User user) {
                runOnFxThread(() -> refreshDerivedLists());
            }

            @Override
            public void onUserReplaced(List<User> users) { // Chú ý sửa lỗi chính tả onUserRelaced thành onUserReplaced (nếu có)
                runOnFxThread(() -> refreshDerivedLists());
            }

            @Override
            public void onUserRemoved(String userId) {
                runOnFxThread(() -> refreshDerivedLists());


            Platform.runLater(() -> {
                AlertShow.showAlert(Alert.AlertType.INFORMATION, "Report", "User has been removed");
            });

            }
        };

        // Đăng ký listener vào UserManager (KHÔNG dùng AuctionManager)
        UserManager.getInstance().registerListener(updateListener);
    }


    public void unregisterAuctionListener() {
        if (updateListener != null) {
            UserManager.getInstance().unregisterListener(updateListener);
            updateListener = null;
        }
    }


    /**
     * Hàm tiện ích giúp đảm bảo code luôn chạy trên luồng JavaFX an toàn
     */
    private void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
    private void refreshDerivedLists() {
        // 1. Lấy danh sách tổng từ Manager
        List<User> master = UserManager.getInstance().getMasterUserList();
        if (master == null) return;
        userList.setAll(master);
        totalUsersLabel.setText(String.valueOf(master.size()));

    }

}
