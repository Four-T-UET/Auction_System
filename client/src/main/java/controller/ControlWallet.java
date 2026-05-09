package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.net.URL;
import java.util.ResourceBundle;
import auction.logic.model.Clients;

public class ControlWallet implements Initializable {
    private Clients client = new Clients("tung","123");
    @FXML
    private Label balanceLabel;
    @FXML
    private Label frozenLabel;
    @FXML
    private TextField depositField;
    @FXML
    private TextField withdrawField;
    @FXML
    private Button depositBtn;
    @FXML
    private Button withdrawBtn;


    @FXML
    public void handleDeposit(ActionEvent event) {
        try{
            String depositeMoney = depositField.getText().trim();
            double money = Double.parseDouble(depositeMoney);
            if (this.client == null) {
                showAlert(Alert.AlertType.ERROR,"Error" ,"User not logged in");
                return;
            }
            this.client.getWallet().deposit(money);
            double balance = this.client.getWallet().getBalance();
            balanceLabel.setText(String.format("$ %.2f", balance));
            depositField.clear();
        }catch(NumberFormatException e){
            showAlert(Alert.AlertType.ERROR,"Error" ,"Invaild input");
        }catch (IllegalArgumentException e){
            showAlert(Alert.AlertType.ERROR,"Error" ,"Negative input");
        }catch (Exception e){
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,"Error" ,"Something went wrong: " + e.getMessage());
        }finally {
            depositField.clear();
        }
    }
    @FXML
    public void handleWithdraw(ActionEvent event) {
        try{
            String withdrawMoney = withdrawField.getText().trim();
            double money = Double.parseDouble(withdrawMoney);
            if (this.client == null) {
                showAlert(Alert.AlertType.ERROR,"Error" ,"User not logged in");
                return;
            }
            this.client.getWallet().withdraw(money);
            double balance = this.client.getWallet().getBalance();
            balanceLabel.setText(String.format("$ %.2f", balance));
        }catch(NumberFormatException e){
            showAlert(Alert.AlertType.ERROR,"Error" ,"Invaild input");
        }catch (IllegalArgumentException e){
            showAlert(Alert.AlertType.ERROR,"Error" ,"Negative input");
        }catch (IllegalStateException e){
            showAlert(Alert.AlertType.ERROR,"Error" ,"Not enough money: " + e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,"Error" ,"Something went wrong: " + e.getMessage());
        }finally {
            withdrawField.clear();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.client = UserSession.getCurrentUser();
        balanceLabel.setText(String.format("$ %.2f", this.client.getWallet().getBalance()));
        frozenLabel.setText("%.2f".formatted(this.client.getWallet().getLockBalance()));
        if (this.client == null) {
            // For testing if bypassed Login
            this.client = new Clients("test", "123");
        }

        balanceLabel.setText(String.format("$ %.2f", this.client.getWallet().getBalance()));
        frozenLabel.setText(String.format("$ %.2f", this.client.getWallet().getLockBalance()));

        depositBtn.setOnAction(event -> {
            handleDeposit(event);
        });
        withdrawBtn.setOnAction(event -> {
            handleWithdraw(event);
        });
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
