package sever.dao;

import auction.logic.model.Auction;
import sever.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

  private static final AuctionDAO instance = new AuctionDAO();
  private AuctionDAO() {};
  public static AuctionDAO getInstance(){
    return instance;
  }


  public void save(Auction auction, long duration) throws SQLException {
    String insertSQL = "INSERT INTO auctions (id, item_id, startPrice, minStep, duration, finish_time, status) VALUES (?,?,?,?,?,?,?)";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

      pstmt.setString(1, auction.getId());
      pstmt.setString(2, auction.getItem().getId());
      pstmt.setDouble(3, auction.getCurrentPrice());
      pstmt.setDouble(4, auction.getMiniumStep());
      pstmt.setLong(5, duration);
      pstmt.setObject(6, auction.getFinishTime());
      pstmt.setString(7, auction.getStatus().name()); // Lưu trạng thái

      pstmt.executeUpdate();
    }
  }

  /**
   * Lấy danh sách thô: Tự mượn và giải phóng kết nối về Pool ngay tại tầng DAO.
   */
  public List<AuctionRawData> pullAllAuctionsRawData() throws SQLException {
    List<AuctionRawData> list = new ArrayList<>();

    String query = "SELECT a.id AS auction_id, a.item_id, a.startPrice, a.minStep, a.duration, " +
            "a.finish_time, a.status, " +
            "i.name, i.description, i.category " +
            "FROM auctions a " +
            "JOIN items i ON a.item_id = i.id " +
            "ORDER BY a.duration DESC LIMIT 50";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query);
         ResultSet rs = pstmt.executeQuery()) {

      while (rs.next()) {
        AuctionRawData raw = new AuctionRawData();
        raw.auctionId = rs.getString("auction_id");
        raw.itemId = rs.getString("item_id");
        raw.startPrice = rs.getDouble("startPrice");
        raw.minStep = rs.getDouble("minStep");
        raw.duration = rs.getLong("duration");
        // ✅ Lấy finishTime từ database
        raw.finishTime = rs.getObject("finish_time", java.time.LocalDateTime.class);
        raw.status = rs.getString("status");
        raw.itemName = rs.getString("name");
        raw.itemDescription = rs.getString("description");
        raw.itemCategoryStr = rs.getString("category");

        list.add(raw);
      }
    }
    return list;
  }

  /**
   * Update startPrice column (used to persist current price after a successful bid)
   */
  public void updateStartPrice(String auctionId, double newPrice) throws SQLException {
    String sql = "UPDATE auctions SET startPrice = ? WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setDouble(1, newPrice);
      pstmt.setString(2, auctionId);
      pstmt.executeUpdate();
    }
  }

  public void updateStatus(String auctionId, String status) throws SQLException {
    String sql = "UPDATE auctions SET status = ? WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, status);
      pstmt.setString(2, auctionId);
      pstmt.executeUpdate();
    }
  }

  /**
   * Lấy dữ liệu thô của một auction cụ thể theo ID
   */
  public AuctionRawData findRawAuctionById(String auctionId) throws SQLException {
    String query = "SELECT a.id AS auction_id, a.item_id, a.startPrice, a.minStep, a.duration, " +
            "a.finish_time, a.status, " +
            "i.name, i.description, i.category " +
            "FROM auctions a " +
            "JOIN items i ON a.item_id = i.id " +
            "WHERE a.id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
      pstmt.setString(1, auctionId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          AuctionRawData raw = new AuctionRawData();
          raw.auctionId = rs.getString("auction_id");
          raw.itemId = rs.getString("item_id");
          raw.startPrice = rs.getDouble("startPrice");
          raw.minStep = rs.getDouble("minStep");
          raw.duration = rs.getLong("duration");
          raw.finishTime = rs.getObject("finish_time", java.time.LocalDateTime.class);
          raw.status = rs.getString("status");
          raw.itemName = rs.getString("name");
          raw.itemDescription = rs.getString("description");
          raw.itemCategoryStr = rs.getString("category");
          return raw;
        }
      }
    }
    return null;
  }

  /**
   * Lớp tĩnh phụ trợ trung chuyển dữ liệu thô từ câu lệnh SQL JOIN
   * giải quyết triệt để đặc tính abstract của lớp Item.
   */
  public static class AuctionRawData implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public String auctionId;
    public String itemId;
    public double startPrice;
    public double minStep;
    public long duration;
    public java.time.LocalDateTime finishTime;
    public String status;  // Thêm trạng thái
    public String itemName;
    public String itemDescription;
    public String itemCategoryStr;
  }
}