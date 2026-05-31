package auction.logic.RequestDTO;

import auction.logic.enums.WalletAction;
import java.io.Serializable;

public class WalletDTO implements Serializable {
    private final String userId;
    private final WalletAction action;
    private final double amount;

    public WalletDTO(String userId, WalletAction action, double amount) {
        this.userId = userId;
        this.action = action;
        this.amount = amount;
    }

    public String getUserId() {
        return userId;
    }

    public WalletAction getAction() {
        return action;
    }

    public double getAmount() {
        return amount;
    }
}

