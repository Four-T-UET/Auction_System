package sever.dao;

import auction.logic.model.Clients;
import auction.logic.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import sever.config.DatabaseConnection;

public class UserDAO {
  public User getUser(String user) {
    String query = "SELECT username, password FROM users WHERE username = ?";

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
}