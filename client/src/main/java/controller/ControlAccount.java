package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.io.IOException;

public class ControlAccount {
  @FXML private Label usernameLabel;
  @FXML private Label emailValue;
  @FXML private Label dateValue;

  @FXML public void logoutBtn(ActionEvent event) throws IOException {
    // Xoá tất cả dữ liệu khi logout
    UserSession.clearAllData();
    // Chuyển về trang login
    ChangeScene.changeTOLogin(event);
  }
  

}
