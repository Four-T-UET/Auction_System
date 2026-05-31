package clientController;

import stateManager.UserSession;
import stateManager.WalletManager;
import stateManager.WalletUpdateListener;
import auction.logic.model.Clients;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import service.WalletService;

import java.net.URL;
import java.util.ResourceBundle;

import static Utils.AlertShow.showAlert;

public class ControlWallet implements Initializable {
  private Clients client;
  private WalletUpdateListener walletListener;
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
    try {
      String depositMoney = depositField.getText().trim();
      double money = Double.parseDouble(depositMoney);
      if (this.client == null) {
        showAlert(Alert.AlertType.ERROR, "Error", "User not logged in");
        return;
      }
      Object response = WalletService.deposit(this.client.getId(), money);
      if (response instanceof auction.logic.ResponseDTO.WalletResponseDTO walletResponse) {
        WalletManager.getInstance().updateWallet(walletResponse.getBalance(), walletResponse.getLocked());
        updateWalletLabels();
      } else if (response instanceof String message) {
        showAlert(Alert.AlertType.ERROR, "Error", message);
      } else {
        showAlert(Alert.AlertType.ERROR, "Error", "Wallet update failed");
      }
    } catch (NumberFormatException e) {
      showAlert(Alert.AlertType.ERROR, "Error", "Invalid input");
    } catch (IllegalArgumentException e) {
      showAlert(Alert.AlertType.ERROR, "Error", "Negative input");
    } catch (Exception e) {
      showAlert(Alert.AlertType.ERROR, "Error", "Something went wrong: " + e.getMessage());
    } finally {
      depositField.clear();
    }
  }

  @FXML
  public void handleWithdraw(ActionEvent event) {
    try {
      String withdrawMoney = withdrawField.getText().trim();
      double money = Double.parseDouble(withdrawMoney);
      if (this.client == null) {
        showAlert(Alert.AlertType.ERROR, "Error", "User not logged in");
        return;
      }

      Object response = WalletService.withdraw(this.client.getId(), money);
      if (response instanceof auction.logic.ResponseDTO.WalletResponseDTO walletResponse) {
        WalletManager.getInstance().updateWallet(walletResponse.getBalance(), walletResponse.getLocked());
        updateWalletLabels();
      } else if (response instanceof String message) {
        showAlert(Alert.AlertType.ERROR, "Error", message);
      } else {
        showAlert(Alert.AlertType.ERROR, "Error", "Wallet update failed");
      }
    } catch (NumberFormatException e) {
      showAlert(Alert.AlertType.ERROR, "Error", "Invalid input");
    } catch (IllegalArgumentException e) {
      showAlert(Alert.AlertType.ERROR, "Error", "Negative input");
    } catch (IllegalStateException e) {
      showAlert(Alert.AlertType.ERROR, "Error", "Not enough money: " + e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      showAlert(Alert.AlertType.ERROR, "Error", "Something went wrong: " + e.getMessage());
    } finally {
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
    WalletManager.getInstance().updateWallet(this.client.getWallet().getBalance(), this.client.getWallet().getLockBalance());

    depositBtn.setOnAction(event -> handleDeposit(event));
    withdrawBtn.setOnAction(event -> handleWithdraw(event));

    walletListener = (balance, locked) -> Platform.runLater(() -> {
      if (client != null) {
        client.getWallet().setBalance(balance);
        client.getWallet().settotalLockBalance(locked);
        updateWalletLabels();
      }
    });
    WalletManager.getInstance().registerListener(walletListener);

    // TỰ ĐỘNG KHỬ KÍCH HOẠT: Khi WalletView bị gỡ khỏi màn hình (chuyển cảnh)
    balanceLabel.sceneProperty().addListener((o, old, n) -> { if (n == null) dispose(); });
  }

  public void dispose() {
    if (walletListener != null) {
      WalletManager.getInstance().unregisterListener(walletListener);
      walletListener = null;
    }
  }

  private void updateWalletLabels() {
    balanceLabel.setText(String.format("$ %.2f", this.client.getWallet().getBalance()));
    frozenLabel.setText(String.format("$ %.2f", this.client.getWallet().getLockBalance()));
  }
}