import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import service.ClientSocket;


public class AppTrade extends Application {
    public static void main(String[] args){
        launch(args);
    }

    public void start(Stage stage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientResource/loginScene.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setWidth(1280);
        stage.setHeight(720);
        stage.setScene(scene);
        stage.setTitle("ETrade");
        stage.show();
        ClientSocket clientSocket = ClientSocket.getInstance(); // tạo ClientSocket duy nhất 1 lần;

    }
}