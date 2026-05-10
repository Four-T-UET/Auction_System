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
    private ImageView loginImage;

    @FXML
    public void initialize() {
        // Nút Đăng ký (Sign Up) sẽ chuyển sang màn hình đăng ký
        signUpButton.setOnAction(event -> {
            try {
                ChangeScene.LoginToSignUp(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        signInButton.setOnAction(event -> handleLogin(event));
    }

    private AuthService authService = new AuthService();


    @FXML
    public void handleLogin(ActionEvent event) {
        String user = textUsername.getText().trim();
        String pass = passwordField.getText();
        
        if (user.isEmpty() || pass.isEmpty()) {
            showAlert("Vui lòng nhập tài khoản và mật khẩu!");
            return;
        }


        Object checkLogin = authService.verifyWithServer(user, pass);

         if(checkLogin instanceof Clients){

             try{
                 UserSession.setCurrentUser( (Clients) checkLogin);
                 ChangeScene.LoginToDashBoard(event);
             }catch (Exception e){
                 e.printStackTrace();
             }
         } else {
             showAlert("Tài khoản hoặc mật khẩu không chính xác!");
         }
    }
//
//    private boolean authenticate(String username, String password){
////        LoginDB.ConnectToDatabase();
////        return LoginDB.isPassword(username, password);
//        if(username.equals("tung123sd") && password.equals("123456")){
//            return true;
//        }
//        return false;
//    }

    private void showAlert(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi đăng nhập");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
