package sever.dao;

import auction.logic.ResponseDTO.UserResponseDTO;
import auction.logic.ResponseDTO.WalletResponseDTO;
import auction.logic.manager.Admin;
import auction.logic.model.Clients;
import auction.logic.model.User;
import sever.config.DatabaseConnection;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDAO {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserDAO.class);
  private static final WalletDAO walletDAO = WalletDAO.getInstance();


  public User getUser(String user) {
    String query = "SELECT id, username, password, role FROM users WHERE username = ?";

    if (user == null) {
      return null;
    }

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(query)) {
      ps.setString(1, user); // gán tham số thứ 1 username = user

      ResultSet res = ps.executeQuery();

      if (res.next()) {
        String id = res.getString("id");
        String role = res.getString("role");
        String username = res.getString("username");
        String password = res.getString("password");

        if(role.equals("ADMIN")){
          return new Admin(username, password);
        }
        Clients client = new Clients(username, password, role);
        client.setId(id);
        WalletResponseDTO walletResponseDTO = walletDAO.getWalletByClientId(client.getId());
        if (walletResponseDTO != null) {
          client.getWallet().setBalance(walletResponseDTO.getBalance());
          client.getWallet().settotalLockBalance(walletResponseDTO.getLocked());
        }
        return client;
      }
    } catch (SQLException e) {
      LOGGER.error("Lỗi khi truy vấn user dtb", e);
    }
    return null;
  }

  public Clients getUserById(String userId) throws SQLException {
    String query = "SELECT id, username, password , role FROM users WHERE id = ?";
    if (userId == null || userId.isBlank()) {
      return null;
    }
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(query)) {
      ps.setString(1, userId);
      ResultSet res = ps.executeQuery();
      if (res.next()) {
        Clients client = new Clients(res.getString("username"), res.getString("password"), res.getString("role"));
        client.setId(res.getString("id"));
        WalletResponseDTO walletResponseDTO = walletDAO.getWalletByClientId(client.getId());
        if (walletResponseDTO != null) {
          client.getWallet().setBalance(walletResponseDTO.getBalance());
          client.getWallet().settotalLockBalance(walletResponseDTO.getLocked());
        }
        return client;
      }
    }
    return null;
  }

  public static User insertUser(String userName, String password, String role) {
    String insertUserSQL = "INSERT INTO users (id ,username, password, role) VALUES (?,?,?,?)";
    String insertWalletSQL = "INSERT INTO wallets (client_id, balance, locked_balance) VALUES (?,?,?)";
    try (Connection conn = DatabaseConnection.getConnection()) {
      conn.setAutoCommit(false);
      try (PreparedStatement user = conn.prepareStatement(insertUserSQL);
           PreparedStatement wallet = conn.prepareStatement(insertWalletSQL)) {

        Clients clients = new Clients(userName, password,role);
        user.setString(1, clients.getId());
        user.setString(2, userName.trim());
        user.setString(3, password);
        user.setString(4,role);
        user.executeUpdate();

        wallet.setString(1, clients.getId());
        wallet.setDouble(2, 0.0);
        wallet.setDouble(3, 0.0);
        wallet.executeUpdate();

        conn.commit();
        LOGGER.info(" Đã INSERT user '" + userName + "' thành công");
        return clients;
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (SQLException e) {
      LOGGER.error("Lỗi khi INSERT user", e);
      return null;
    }
  }
  public List<UserResponseDTO> pullAllUsers() throws SQLException {
    List<UserResponseDTO> list = new ArrayList<>();

    // Câu lệnh query lấy các trường thông tin cơ bản của User
    String query = "SELECT id, username FROM users ORDER BY id DESC";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query);
         ResultSet rs = pstmt.executeQuery()) {

      while (rs.next()) {
        UserResponseDTO raw = new UserResponseDTO();
        raw.userId = rs.getString("id");
        raw.username = rs.getString("username");

        list.add(raw);
      }
    }
    return list;
  }
}
