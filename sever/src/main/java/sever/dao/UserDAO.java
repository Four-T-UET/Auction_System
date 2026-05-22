package sever.dao;

import auction.logic.model.Clients;
import auction.logic.model.User;

import java.sql.*;

import sever.config.DatabaseConnection;

public class UserDAO {
  private final WalletDAO walletDAO = new WalletDAO();

  public User getUser(String user) {
    String query = "SELECT id, username, password FROM users WHERE username = ?";

    if (user == null) {
      return null;
    }

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(query)) {
      ps.setString(1, user); // gán tham số thứ 1 username = user

      ResultSet res = ps.executeQuery();
      if (res.next()) {
        Clients client = new Clients(res.getString("username"), res.getString("password"));
        client.setId(res.getString("id"));
        WalletDAO.WalletSnapshot snapshot = walletDAO.getWalletByClientId(client.getId());
        if (snapshot != null) {
          client.getWallet().setBalance(snapshot.getBalance());
          client.getWallet().settotalLockBalance(snapshot.getLocked());
        }
        return client;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public Clients getUserById(String userId) throws SQLException {
    String query = "SELECT id, username, password FROM users WHERE id = ?";
    if (userId == null || userId.isBlank()) {
      return null;
    }
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(query)) {
      ps.setString(1, userId);
      ResultSet res = ps.executeQuery();
      if (res.next()) {
        Clients client = new Clients(res.getString("username"), res.getString("password"));
        client.setId(res.getString("id"));
        WalletDAO.WalletSnapshot snapshot = walletDAO.getWalletByClientId(client.getId());
        if (snapshot != null) {
          client.getWallet().setBalance(snapshot.getBalance());
          client.getWallet().settotalLockBalance(snapshot.getLocked());
        }
        return client;
      }
    }
    return null;
  }

  public static User insertUser(String userName, String password) {
    String insertUserSQL = "INSERT INTO users (id ,username, password) VALUES (?,?,?)";
    String insertWalletSQL = "INSERT INTO wallets (client_id, balance, locked_balance) VALUES (?,?,?)";
    try (Connection conn = DatabaseConnection.getConnection()) {
      conn.setAutoCommit(false);
      try (PreparedStatement userStmt = conn.prepareStatement(insertUserSQL);
           PreparedStatement walletStmt = conn.prepareStatement(insertWalletSQL)) {

        Clients clients = new Clients(userName, password);
        userStmt.setString(1, clients.getId());
        userStmt.setString(2, userName.trim());
        userStmt.setString(3, password);
        userStmt.executeUpdate();

        walletStmt.setString(1, clients.getId());
        walletStmt.setDouble(2, 0.0);
        walletStmt.setDouble(3, 0.0);
        walletStmt.executeUpdate();

        conn.commit();
        System.out.println(" Đã INSERT user '" + userName + "' thành công!");
        return clients;
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (SQLException e) {
      System.err.println(" Lỗi khi INSERT user:");
      e.printStackTrace();
      return null;
    }
  }

}
