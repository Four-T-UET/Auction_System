package sever.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseConnection {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnection.class);
  // Biến static duy nhất chứa "Bể kết nối" (Pool)
  private static HikariDataSource dataSource;

  // khối static: Chạy đúng 1 lần duy nhất khi Server khởi động để cấu hình Pool
  static {
    try {
      HikariConfig config = new HikariConfig();
      // Cấu hình thông tin Dtb
      config.setJdbcUrl("jdbc:mysql://auctiondtb.mysql.database.azure.com:3306/auction_db");
      config.setUsername("buiduythang");
      config.setPassword("hsDUYTHANG195@");
      // Cấu hình Pool (Singleton tối ưu)
      config.setMaximumPoolSize(10); // Tối đa 10 kết nối dùng chung
      config.setMinimumIdle(2);      // Luôn giữ ít nhất 2 kết nối chờ sẵn
      config.setConnectionTimeout(30000); // Đợi tối đa 30s để lấy kết nối
      config.setIdleTimeout(300000);      // Sau 10p không dùng thì giải phóng

      // Tối ưu riêng cho MySQL
      config.addDataSourceProperty("cachePrepStmts", "true");
      config.addDataSourceProperty("prepStmtCacheSize", "250");
      config.addDataSourceProperty("prepStmtLimit", "2048");

      dataSource = new HikariDataSource(config);
      LOGGER.info(" [DTB connection] : Database Pool (HikariCP) cấu hình thành công");

    } catch (Exception e) {
      LOGGER.error("Lỗi khởi tạo Pool", e);
    }
  }

  // Phương thức lấy kết nối (Các lớp DAO sẽ gọi hàm này)
  public static Connection getConnection() throws SQLException {
    if (dataSource == null) {
      throw new SQLException("DataSource chưa được khởi tạo!");
    }
    return dataSource.getConnection(); // Lấy các kết nối có sẵn
  }

  //  private constructor để ngăn người khác khởi tạo DatabaseConnection lung tung
  private DatabaseConnection() {}
}