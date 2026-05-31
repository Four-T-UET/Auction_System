package stateManager;

import javafx.application.Platform;
import java.util.concurrent.CopyOnWriteArrayList;

public class WalletManager {
  private static WalletManager instance;
  private final CopyOnWriteArrayList<WalletUpdateListener> listeners = new CopyOnWriteArrayList<>();
  private volatile double balance;
  private volatile double locked;
  private WalletManager() {}

  public static WalletManager getInstance() {
    if (instance == null) {
      instance = new WalletManager();
    }
    return instance;
  }
  public double getBalance() {
    return balance;
  }

  public double getLocked() {
    return locked;
  }
  public void registerListener(WalletUpdateListener l) {
    if (l != null) listeners.addIfAbsent(l);
  }

  public void unregisterListener(WalletUpdateListener l) {
    listeners.remove(l);
  }
  private void notifyUpdated(double newBalance, double newLocked) {
    if (Platform.isFxApplicationThread()) {
      for (WalletUpdateListener l : listeners) {
        try {
          l.onWalletUpdated(newBalance, newLocked);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } else {
      Platform.runLater(() -> notifyUpdated(newBalance, newLocked));
    }
  }
  public void updateWallet(double balance, double locked) {
    if (Platform.isFxApplicationThread()) {
      doUpdateWallet(balance, locked);
    } else {
      Platform.runLater(() -> doUpdateWallet(balance, locked));
    }
  }
  private void doUpdateWallet(double balance, double locked) {
    this.balance = balance;
    this.locked = locked;
    notifyUpdated(balance, locked);
  }
}