package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import auction.logic.model.Clients;
import service.AuthService;

public class Login {
    @FXML
    private TextField textUsername;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button signInButton;
    @FXML
    private Button signUpButton;
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

        signInButton.setOnAction(event -> handleLogin(event));
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String user = textUsername.getText().trim();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            AlertShow.showAlert(Alert.AlertType.ERROR,"Error","Vui lòng nhập tài khoản và mật khẩu!");
            return;
        }


        Object checkLogin = AuthService.verifyWithServer(user, pass);

        if(checkLogin instanceof Clients){

            try{
                UserSession.setCurrentUser((Clients) checkLogin);
                ChangeScene.LoginToDashBoard(event);
            }catch (Exception e){
                e.printStackTrace();
            }
        } else {
            AlertShow.showAlert(Alert.AlertType.ERROR,"Error","Tài khoản hoặc mật khẩu không chính xác!");
        }
    }
}
