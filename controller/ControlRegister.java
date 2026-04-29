package controller.etrade;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

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
        if (!validate(user, pass, repass)) {
            return; // Nếu không hợp lệ thì dừng việc tạo tài khoản
        }

        LoginDB.insertUser(user, pass);// connect to database

        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Thành công");
        successAlert.setHeaderText(null);
        successAlert.setContentText("Đăng ký thành công! Vui lòng quay lại màn hình Login.");

        Optional<ButtonType> result = successAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try{
                // Gọi hàm SignUpToLogin và truyền registerButton vào để hàm biết Stage nào cần đổi
                ChangeScene.SignUpToLogin(event);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    // Hàm Helper để hiển thị thông báo giảm lặp code
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private boolean validate(String user,String pass,String repass){
        // 1. Kiểm tra không được để trống
        if(LoginDB.isUserExists(user)){
            return false;
        }
        if(user.isEmpty() || pass.isEmpty() || repass.isEmpty()){
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng điền đầy đủ tên đăng nhập và mật khẩu!");
            return false;
        }

        // 2. Kiểm tra mật khẩu nhập lại có khớp không
        if(!pass.equals(repass)){
            showAlert(Alert.AlertType.ERROR, "Lỗi đăng ký", "Mật khẩu nhập lại không khớp!");
            return false;
        }

        // 3. (Tuỳ chọn) Kiểm tra độ dài/độ an toàn của mật khẩu
        if(pass.length() < 6){
            showAlert(Alert.AlertType.WARNING, "Mật khẩu yếu", "Mật khẩu phải có ít nhất 6 ký tự!");
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
}
