package clientController;

import Utils.AlertShow;
import Utils.ChangeScene;
import auction.logic.model.Clients;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import service.AuctionService;
import service.AuthService;

public class Login {
    @FXML
    private TextField textUsername;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ImageView loginImage;
    @FXML
    private Button signInButton;
    @FXML
    private Button signUpButton;
    @FXML
    private ProgressIndicator loadingSpinner;
    @FXML
    public void initialize() {
        // Nút Đăng ký (Sign Up) sẽ chuyển sang màn hình đăng ký
        signUpButton.setOnAction(event -> {
            try {
                ChangeScene.LoginToSignUp(event);
            } catch (Exception e) {
                AlertShow.showAlert(Alert.AlertType.ERROR,"Error","Something went wrong. Please try again.");
            }
        });

        signInButton.setOnAction(this::handleLogin);
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String user = textUsername.getText().trim();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            AlertShow.showAlert(Alert.AlertType.ERROR,"Error","Vui lòng nhập tài khoản và mật khẩu!");
            return;
        }
        // Show loading spinner and disable controls
        loadingSpinner.setVisible(true);
        signInButton.setDisable(true);
        signUpButton.setDisable(true);

        Task<Object> loginTask = new Task<>() {
            @Override
            protected Object call() {
                // perform authentication on background thread
                Object result = AuthService.verifyWithServer(user, pass);
                if (result instanceof Clients) {
                    // load auctions or other heavy data in background
                    AuctionService.pullAuction();
                }
                return result;
            }
        };

        loginTask.setOnSucceeded(workerStateEvent -> {
            loadingSpinner.setVisible(false);
            Object checkLogin = loginTask.getValue();
            if (checkLogin instanceof Clients) {
                try {
                    UserSession.setCurrentUser((Clients) checkLogin);
                    // Switch to dashboard on UI thread
                    try {
                        ChangeScene.LoginToDashBoard(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                        AlertShow.showAlert(Alert.AlertType.ERROR, "Error", "Không thể mở Dashboard: " + e.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                signInButton.setDisable(false);
                signUpButton.setDisable(false);
                AlertShow.showAlert(Alert.AlertType.ERROR, "Error", "Tài khoản hoặc mật khẩu không chính xác!");
            }
        });

        loginTask.setOnFailed(workerStateEvent -> {
            loadingSpinner.setVisible(false);
            signInButton.setDisable(false);
            signUpButton.setDisable(false);
            Throwable ex = loginTask.getException();
            ex.printStackTrace();
            AlertShow.showAlert(Alert.AlertType.ERROR, "Error", "Lỗi khi đăng nhập: " + ex.getMessage());
        });

        Thread t = new Thread(loginTask);
        t.setDaemon(true);
        t.start();
    }
}
