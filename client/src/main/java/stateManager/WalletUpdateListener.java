package stateManager;
public interface WalletUpdateListener {
  void onWalletUpdated(double balance, double locked);
}