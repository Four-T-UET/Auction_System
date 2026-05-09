package sever.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
  public static Connection getConnection() throws SQLException {
    String url = "jdbc:mysql://localhost:3306/uet_login_db";
    String user = "root"; // Thay bằng user của bạn
    String pass = "19052007"; // Thay bằng pass của bạn
    return DriverManager.getConnection(url, user, pass);
  }
}