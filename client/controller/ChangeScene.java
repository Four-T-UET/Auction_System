package controller.etrade;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChangeScene {
    public static void LoginToSignUp(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(ChangeScene.class.getResource("/controller/etrade/register.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Sign Up");
        stage.show();
    }

    public static void SignUpToLogin(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(ChangeScene.class.getResource("/controller/etrade/loginScene.fxml"));
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();
    }
    public static void LoginToDashBoard(ActionEvent event) throws IOException {

        Parent root = FXMLLoader.load(ChangeScene.class.getResource("/controller/etrade/MainDashboard.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("DashBoard");
        stage.show();
    }
    public static void changeTODashBoard(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(ChangeScene.class.getResource("/controller/etrade/MainDashboard.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}
