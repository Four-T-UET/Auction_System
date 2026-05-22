package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import service.AuthService;

import java.util.Optional;

import java.net.URL;
import java.util.ResourceBundle;

public class ControlRegister implements Initializable {
    @FXML
    private TextField userName;
    @FXML
    private PasswordField password;
    @FXML
    private PasswordField reEnterpass;
    @FXML
    private Button registerButton;

    @FXML
    public void register(ActionEvent event) {
        String user = userName.getText().trim();
        String pass = password.getText();
        String repass = reEnterpass.getText();

        // Validate
        if (!validateInput(user, pass, repass)) {
            return; // Nếu không hợp lệ thì dừng việc tạo tài khoản
        }
        boolean registerSuccess =  AuthService.registerRequest(user, pass);
        if (!registerSuccess) {
            AlertShow.showAlert(Alert.AlertType.ERROR,"Error","Username is existed");
            return;
        }
        AlertShow.showAlert(Alert.AlertType.INFORMATION, "Info", "Register Successfully");
        try{
            // Gọi hàm SignUpToLogin và truyền registerButton vào để hàm biết Stage nào cần đổi
            ChangeScene.SignUpToLogin(event);
        }
        catch (Exception e){
            AlertShow.showAlert(Alert.AlertType.ERROR,"Error","Something went wrong. Try again.");
        }
    }
    private boolean validateInput(String user,String pass,String repass){
        // 1. Kiểm tra không được để trống
        if(user.isEmpty() || pass.isEmpty() || repass.isEmpty()){
            AlertShow.showAlert(Alert.AlertType.WARNING, "", "Vui lòng điền đầy đủ tên đăng nhập và mật khẩu!");
            return false;
        }

        // 2. Kiểm tra mật khẩu nhập lại có khớp không
        if(!pass.equals(repass)){
            AlertShow.showAlert(Alert.AlertType.ERROR, "Lỗi đăng ký", "Mật khẩu nhập lại không khớp!");
            return false;
        }

        // 3. (Tuỳ chọn) Kiểm tra độ dài/độ an toàn của mật khẩu
        if(pass.length() < 6){
            AlertShow.showAlert(Alert.AlertType.WARNING, "Mật khẩu yếu", "Mật khẩu phải có ít nhất 6 ký tự!");
            return false;
        }
        return true;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        registerButton.setOnAction(event -> {
            register(event);
        });

    }

    @FXML
    public void goToLogin(ActionEvent event) {
        try {
            ChangeScene.SignUpToLogin(event);
        } catch (Exception e) {
            AlertShow.showAlert(Alert.AlertType.ERROR, "Error", "Không thể chuyển đến trang đăng nhập: " + e.getMessage());
        }
    }
}
