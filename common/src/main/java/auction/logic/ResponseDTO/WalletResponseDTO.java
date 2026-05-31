package auction.logic.ResponseDTO;

import java.io.Serializable;

public class WalletResponseDTO implements Serializable {
    private final double balance;
    private final double lockedBalance;

    public WalletResponseDTO(double balance, double lockedBalance) {
        this.balance = balance;
        this.lockedBalance = lockedBalance;
    }

    public double getBalance() {
        return balance;
    }

    public double getLocked() {
        return lockedBalance;
    }

}

