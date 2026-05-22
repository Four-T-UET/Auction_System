package sever.dao;

import sever.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WalletDAO {
    public WalletSnapshot getWalletByClientId(String clientId) throws SQLException {
        String query = "SELECT balance, locked_balance FROM wallets WHERE client_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, clientId);
            ResultSet res = ps.executeQuery();
            if (res.next()) {
                return new WalletSnapshot(res.getDouble("balance"), res.getDouble("locked_balance"));
            }
        }
        return null;
    }

    public WalletSnapshot updateWalletBalance(String clientId, double amount, boolean isDeposit) throws SQLException {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("Client id is required");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        String selectSql = "SELECT balance, locked_balance FROM wallets WHERE client_id = ? FOR UPDATE";
        String updateSql = "UPDATE wallets SET balance = ? WHERE client_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement select = conn.prepareStatement(selectSql)) {
                select.setString(1, clientId);
                ResultSet res = select.executeQuery();
                if (!res.next()) {
                    conn.rollback();
                    return null;
                }
                double balance = res.getDouble("balance");
                double locked = res.getDouble("locked_balance");
                double updatedBalance = isDeposit ? balance + amount : balance - amount;
                if (!isDeposit && updatedBalance < 0) {
                    conn.rollback();
                    throw new IllegalStateException("Insufficient balance");
                }

                try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                    update.setDouble(1, updatedBalance);
                    update.setString(2, clientId);
                    update.executeUpdate();
                }

                conn.commit();
                return new WalletSnapshot(updatedBalance, locked);
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static class WalletSnapshot {
        private final double balance;
        private final double locked;

        public WalletSnapshot(double balance, double locked) {
            this.balance = balance;
            this.locked = locked;
        }

        public double getBalance() {
            return balance;
        }

        public double getLocked() {
            return locked;
        }
    }
}

