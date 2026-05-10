package sever.dao;

import auction.logic.model.Clients;
import auction.logic.model.User;

import java.sql.*;

import sever.config.DatabaseConnection;

public class UserDAO {
  public User getUser(String user) {
    String query = "SELECT username, password FROM users WHERE username = ?";

    if (user == null) {
      return null;
    }

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(query)) {
      ps.setString(1, user); // gán tham số thứ 1 username = user
//      ps.setString(2, pass); // gán tham số thứ 2 password = pass   --> ps thực hiện query

      ResultSet res = ps.executeQuery();
      if (res.next()) {
        return new Clients(res.getString("username"), res.getString("password"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }


  public static User insertUser(String userName, String password) {
    String insertSQL = "INSERT INTO users (id ,username, password) VALUES (?,?,?)";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

      Clients clients = new Clients(userName, password);
      pstmt.setString(1,clients.getId());
      pstmt.setString(2, userName.trim());
      pstmt.setString(3, password);

      int rowsAffected = pstmt.executeUpdate();//
      System.out.println(" Đã INSERT user '" + userName + "' thành công!");
      return clients;

    } catch (SQLException e) {
      System.err.println(" Lỗi khi INSERT user:");
      e.printStackTrace();
      return null;
    }
  }
}
