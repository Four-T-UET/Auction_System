package clientController;

import Utils.ChangeScene;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import java.io.IOException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import stateManager.UserSession;

public class ControlAccount implements Initializable {
  private final String PATH = "/image/avatar.jpg";
  @FXML private ImageView avatarImage;
  @FXML private Label usernameLabel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    Image avatar = new Image(getClass().getResourceAsStream(PATH));
    avatarImage.setImage(avatar);
    usernameLabel.setText(UserSession.getCurrentUser().getUsername());
  }
  @FXML public void logoutBtn(ActionEvent event) throws IOException {
    // Xoá tất cả dữ liệu khi logout
    UserSession.clearAllData();
    // Chuyển về trang login
    ChangeScene.changeTOLogin(event);
  }

  

}
