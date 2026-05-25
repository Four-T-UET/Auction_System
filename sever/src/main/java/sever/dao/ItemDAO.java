package sever.dao;

import auction.logic.enums.ItemCategory;
import auction.logic.factory.ArtFactory;
import auction.logic.factory.ElectronicsFactory;
import auction.logic.factory.ItemFactory;
import auction.logic.factory.RealEstateFactory;
import auction.logic.factory.VehicleFactory;
import auction.logic.model.Item;
import auction.logic.ResponseDTO.ItemResponseDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import sever.config.DatabaseConnection;

public class ItemDAO {
  private static final ItemDAO instance = new ItemDAO();
  private ItemDAO() {}
  public static ItemDAO getInstance(){
    return instance;
  }

  public void addItem(Item item) {
    String insertSQL = "INSERT INTO items (id, name, description, category, image_byte) VALUES (?,?,?,?,?)";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
      pstmt.setString(1,item.getId());
      pstmt.setString(2, item.getName());
      pstmt.setString(3, item.getDescription());
      pstmt.setString(4,item.getCategory().toString());
      pstmt.setBytes(5,item.getImageBytes());
      pstmt.executeUpdate();
    } catch (SQLException e) {
      System.err.println(" Lỗi khi INSERT item:");
      e.printStackTrace();
    }
  }

  public ItemResponseDTO findRawItemById(String itemID) throws SQLException {
    String query = "SELECT id, name, description, category, image_byte FROM items WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
      pstmt.setString(1, itemID);
      try (ResultSet res = pstmt.executeQuery()) {
        if (res.next()) {
          return new ItemResponseDTO(
              res.getString("id"),
              res.getString("name"),
              res.getString("description"),
              res.getString("category"),
              res.getBytes("image_byte")
          );
        }
      }
    }
    return null;
  }
}
