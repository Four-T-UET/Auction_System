package sever.dao;

import auction.logic.ResponseDTO.WalletResponseDTO;
import sever.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WalletDAO {
    private static final WalletDAO instance = new WalletDAO();
    public static WalletDAO getInstance(){
        return instance;
    }
    private WalletDAO (){}

    public WalletResponseDTO getWalletByClientId(String clientId) throws SQLException {
        String query = "SELECT balance, locked_balance FROM wallets WHERE client_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, clientId);
            ResultSet res = ps.executeQuery();
            if (res.next()) {
                return new WalletResponseDTO(res.getDouble("balance"), res.getDouble("locked_balance"));
            }
        }
        return null;
    }

    public WalletResponseDTO updateWalletBalance(String clientId, double amount, boolean isDeposit) throws SQLException {
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
                return new WalletResponseDTO(updatedBalance, locked);
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void updateWallet(String clientId, double balance, double lockedBalance) throws SQLException {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("Client id is required");
        }
        String updateSql = "UPDATE wallets SET balance = ?, locked_balance = ? WHERE client_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setDouble(1, balance);
            ps.setDouble(2, lockedBalance);
            ps.setString(3, clientId);
            ps.executeUpdate();
        }
    }
}


