package sever.dao;

import auction.logic.model.Auction;
import auction.logic.ResponseDTO.AuctionResponseDTO;
import sever.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

  private static final AuctionDAO instance = new AuctionDAO();
  private AuctionDAO() {};
  public static AuctionDAO getInstance(){
    return instance;
  }

  public void save(Auction auction, long duration, String sellerId) throws SQLException {
    String insertSQL = "INSERT INTO auctions (id, item_id, startPrice, minStep, duration, finish_time, status, current_winner_id, seller_id) VALUES (?,?,?,?,?,?,?,?,?)";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
      pstmt.setString(1, auction.getId());
      pstmt.setString(2, auction.getItem().getId());
      pstmt.setDouble(3, auction.getCurrentPrice());
      pstmt.setDouble(4, auction.getMiniumStep());
      pstmt.setLong(5, duration);
      pstmt.setObject(6, auction.getFinishTime());
      pstmt.setString(7, auction.getStatus().name());
      pstmt.setString(8, auction.getCurrentWinner() != null ? auction.getCurrentWinner().getId() : null);
      pstmt.setString(9, sellerId);
      pstmt.executeUpdate();
    }
  }

  public List<AuctionResponseDTO> pullAllAuctionsData() throws SQLException {
    List<AuctionResponseDTO> list = new ArrayList<>();
    String query = "SELECT a.id AS auction_id, a.item_id, a.startPrice, a.minStep, a.duration, " +
            "a.finish_time, a.status, a.current_winner_id, a.seller_id, " +
            "i.name, i.description, i.category, i.image_byte " +
            "FROM auctions a JOIN items i ON a.item_id = i.id ORDER BY a.duration DESC LIMIT 50";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query);
         ResultSet rs = pstmt.executeQuery()) {
      while (rs.next()) {
        list.add(new AuctionResponseDTO(
            rs.getString("auction_id"),
            rs.getString("item_id"),
            rs.getDouble("startPrice"),
            rs.getDouble("minStep"),
            rs.getLong("duration"),
            rs.getObject("finish_time", LocalDateTime.class),
            rs.getString("status"),
            rs.getString("current_winner_id"),
            rs.getString("seller_id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("category"),
            rs.getBytes("image_byte")
        ));
      }
    }
    return list;
  }

  public void updatePriceAndWinner(String auctionId, double newPrice, String currentWinnerId) throws SQLException {
    String sql = "UPDATE auctions SET startPrice = ?, current_winner_id = ? WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setDouble(1, newPrice);
      pstmt.setString(2, currentWinnerId);
      pstmt.setString(3, auctionId);
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

  public void updateFinishTime(String auctionId, LocalDateTime finishTime) throws SQLException {
    String sql = "UPDATE auctions SET finish_time = ? WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setObject(1, finishTime);
      pstmt.setString(2, auctionId);
      pstmt.executeUpdate();
    }
  }

  public AuctionResponseDTO findAuctionById(String auctionId) throws SQLException {
    String query = "SELECT a.id AS auction_id, a.item_id, a.startPrice, a.minStep, a.duration, " +
            "a.finish_time, a.status, a.current_winner_id, a.seller_id, " +
            "i.name, i.description, i.category, i.image_byte " +
            "FROM auctions a JOIN items i ON a.item_id = i.id WHERE a.id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
      pstmt.setString(1, auctionId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return new AuctionResponseDTO(
              rs.getString("auction_id"),
              rs.getString("item_id"),
              rs.getDouble("startPrice"),
              rs.getDouble("minStep"),
              rs.getLong("duration"),
              rs.getObject("finish_time", LocalDateTime.class),
              rs.getString("status"),
              rs.getString("current_winner_id"),
              rs.getString("seller_id"),
              rs.getString("name"),
              rs.getString("description"),
              rs.getString("category"),
              rs.getBytes("image_byte")
          );
        }
      }
    }
    return null;
  }

  public String findSellerIdByAuctionId(String auctionId) throws SQLException {
    String query = "SELECT seller_id FROM auctions WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
      pstmt.setString(1, auctionId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getString("seller_id");
        }
      }
    }
    return null;
  }
}
