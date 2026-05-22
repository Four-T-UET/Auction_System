package sever.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
  // 1. Biến static duy nhất chứa "Bể kết nối" (Pool)
  private static HikariDataSource dataSource;

  // 2. Khối static: Chạy đúng 1 lần duy nhất khi Server khởi động để cấu hình Pool
  static {
    try {
      HikariConfig config = new HikariConfig();

      // Cấu hình thông tin DB của bạn
      config.setJdbcUrl("jdbc:mysql://auctiondtb.mysql.database.azure.com:3306/auction_db");
      config.setUsername("buiduythang");
      config.setPassword("hsDUYTHANG195@");
      // --- Cấu hình sức mạnh cho Pool (Singleton tối ưu) ---
      config.setMaximumPoolSize(10); // Tối đa 10 kết nối dùng chung
      config.setMinimumIdle(2);      // Luôn giữ ít nhất 2 kết nối chờ sẵn
      config.setConnectionTimeout(30000); // Đợi tối đa 30s để lấy kết nối
      config.setIdleTimeout(600000);      // Sau 10p không dùng thì giải phóng bớt xe

      // Tối ưu riêng cho MySQL
      config.addDataSourceProperty("cachePrepStmts", "true");
      config.addDataSourceProperty("prepStmtCacheSize", "250");
      config.addDataSourceProperty("prepStmtLimit", "2048");

      dataSource = new HikariDataSource(config);
      System.out.println(">>> Database Pool (HikariCP) initialized successfully!");

    } catch (Exception e) {
      System.err.println(">>> Lỗi khởi tạo Pool: " + e.getMessage());
      e.printStackTrace();
    }
  }

  // 3. Phương thức lấy kết nối (Các lớp DAO sẽ gọi hàm này)
  public static Connection getConnection() throws SQLException {
    if (dataSource == null) {
      throw new SQLException("DataSource chưa được khởi tạo!");
    }
    return dataSource.getConnection(); // Lấy "xe" có sẵn từ gara ra chạy
  }

  // Constructor private để ngăn người khác new DatabaseConnection() linh tinh
  private DatabaseConnection() {}
}