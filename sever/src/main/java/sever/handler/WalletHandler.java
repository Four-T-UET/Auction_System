package sever.handler;

import auction.logic.RequestDTO.WalletDTO;
import auction.logic.ResponseDTO.WalletResponseDTO;
import auction.logic.enums.WalletAction;
import java.io.ObjectOutputStream;
import sever.dao.WalletDAO;

public class WalletHandler {
    private final WalletDAO walletDAO = new WalletDAO();

    public void handle(WalletDTO walletDTO, ObjectOutputStream out) {
        try {
            if (walletDTO == null) {
                out.writeObject("FAILED: Invalid request");
                out.flush();
                return;
            }
            String userId = walletDTO.getUserId();
            WalletAction action = walletDTO.getAction();
            double amount = walletDTO.getAmount();

            boolean isDeposit = action == WalletAction.DEPOSIT;
            WalletDAO.WalletSnapshot snapshot = walletDAO.updateWalletBalance(userId, amount, isDeposit);
            if (snapshot == null) {
                out.writeObject("FAILED: User not found");
            } else {
                out.writeObject(new WalletResponseDTO(snapshot.getBalance(), snapshot.getLocked()));
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
                out.writeObject("FAILED: Server error");
                out.flush();
            } catch (Exception ignored) {
            }
        }
    }
}
