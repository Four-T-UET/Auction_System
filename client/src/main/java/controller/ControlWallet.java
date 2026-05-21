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
import auction.logic.ResponseDTO.WalletResponseDTO;
import service.WalletService;

import static controller.AlertShow.showAlert;

public class ControlWallet implements Initializable {
    private Clients client;
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
            Object response = WalletService.deposit(this.client.getId(), money);
            if (response instanceof WalletResponseDTO walletResponse) {
                this.client.getWallet().setBalance(walletResponse.getBalance());
                this.client.getWallet().settotalLockBalance(walletResponse.getLockedBalance());
                updateWalletLabels();
            } else if (response instanceof String message) {
                showAlert(Alert.AlertType.ERROR, "Error", message);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Wallet update failed");
            }
            depositField.clear();
        }catch(NumberFormatException e){
            showAlert(Alert.AlertType.ERROR,"Error" ,"Invaild input");
        }catch (IllegalArgumentException e){
            showAlert(Alert.AlertType.ERROR,"Error" ,"Negative input");
        }catch (Exception e){
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

            Object response = WalletService.withdraw(this.client.getId(), money);
            if (response instanceof WalletResponseDTO walletResponse) {
                this.client.getWallet().setBalance(walletResponse.getBalance());
                this.client.getWallet().settotalLockBalance(walletResponse.getLockedBalance());
                updateWalletLabels();
            } else if (response instanceof String message) {
                showAlert(Alert.AlertType.ERROR, "Error", message);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Wallet update failed");
            }
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
        if (this.client == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User not logged in");
            balanceLabel.setText("$ 0.00");
            frozenLabel.setText("$ 0.00");
            depositBtn.setDisable(true);
            withdrawBtn.setDisable(true);
            return;
        }
        updateWalletLabels();

        depositBtn.setOnAction(event -> {
            handleDeposit(event);
        });
        withdrawBtn.setOnAction(event -> {
            handleWithdraw(event);
        });
    }

    private void updateWalletLabels() {
        balanceLabel.setText(String.format("$ %.2f", this.client.getWallet().getBalance()));
        frozenLabel.setText(String.format("$ %.2f", this.client.getWallet().getLockBalance()));
    }
}
