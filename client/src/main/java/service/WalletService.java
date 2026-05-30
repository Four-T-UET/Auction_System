package service;

import auction.logic.RequestDTO.WalletDTO;
import auction.logic.ResponseDTO.WalletResponseDTO;
import auction.logic.enums.WalletAction;

public class WalletService {
    public static Object deposit(String userId, double amount) {
        return walletRequest(userId, WalletAction.DEPOSIT, amount);
    }

    public static Object withdraw(String userId, double amount) {
        return walletRequest(userId, WalletAction.WITHDRAW, amount);
    }

    private static Object walletRequest(String userId, WalletAction action, double amount) {
        try {
            ClientSocket clientSocket = ClientSocket.getInstance();
            WalletDTO walletDTO = new WalletDTO(userId, action, amount);
            clientSocket.send(walletDTO);
            Object response = clientSocket.receive();
            return response;
        } catch (RuntimeException e) {
            return null;
        }
    }
}

