package sever.handler;

import auction.logic.RequestDTO.WalletDTO;
import auction.logic.ResponseDTO.WalletResponseDTO;
import auction.logic.enums.WalletAction;
import auction.logic.model.Clients;
import java.io.ObjectOutputStream;
import sever.dao.WalletDAO;
import sever.manager.ClientRuntimeManager;

public class WalletHandler {
    private final WalletDAO walletDAO = WalletDAO.getInstance();

    public void handle(WalletDTO walletDTO, ObjectOutputStream out) {
        try {
            if (walletDTO == null) {
                out.writeObject("FAILED: request không hợp lệ");
                out.flush();
                return;
            }
            String userId = walletDTO.getUserId();
            WalletAction action = walletDTO.getAction();
            double amount = walletDTO.getAmount();

            boolean isDeposit = action == WalletAction.DEPOSIT;
            WalletResponseDTO walletResponseDTO = walletDAO.updateWalletBalance(userId, amount, isDeposit);
            if (walletResponseDTO == null) {
                out.writeObject("FAILED: Không tìm thấy User");
            } else {
                // Cập nhật cache trong bộ nhớ
                Clients client = ClientRuntimeManager.getInstance().getOrLoad(userId);
                if (client != null) {
                    client.getWallet().setBalance(walletResponseDTO.getBalance());
                    client.getWallet().settotalLockBalance(walletResponseDTO.getLocked());
                    ClientRuntimeManager.getInstance().addOrUpdate(client);
                }
                out.writeObject(new WalletResponseDTO(walletResponseDTO.getBalance(), walletResponseDTO.getLocked()));
            }
            out.flush();
        } catch (IllegalArgumentException | IllegalStateException e) {
            try {
                out.writeObject("FAILED: " + e.getMessage());
                out.flush();
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            try {
                out.writeObject("FAILED: Lỗi server");
                out.flush();
            } catch (Exception ignored) {
            }
        }
    }
}