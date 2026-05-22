package sever.dao;

import auction.logic.enums.ItemCategory;
import auction.logic.factory.ArtFactory;
import auction.logic.factory.ElectronicsFactory;
import auction.logic.factory.ItemFactory;
import auction.logic.factory.RealEstateFactory;
import auction.logic.factory.VehicleFactory;
import auction.logic.model.Item;

import java.io.Serializable;
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
      System.out.println(item.getId());
      pstmt.executeUpdate();

    } catch (SQLException e) {
      System.err.println(" Lỗi khi INSERT item:");
      e.printStackTrace();
    }
  }
  public RawItemData findRawItemById(String itemID) throws SQLException {
    String query = "SELECT name, description, category, image_byte FROM items WHERE id = ?";

    // try-with-resources: conn và pstmt tự động đóng khi kết thúc khối lệnh
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

      pstmt.setString(1, itemID);
      try (ResultSet res = pstmt.executeQuery()) { // rs cũng tự động đóng
        if (res.next()) {
          RawItemData raw = new RawItemData();
          raw.name = res.getString("name");
          raw.description = res.getString("description");
          raw.categoryStr = res.getString("category");
          raw.imageByte = res.getBytes("image_byte");
          return raw;
        }
      }
    }
    return null;
  }
  public static class RawItemData implements Serializable {
    private static final long serialVersionUID = 1L;
    public String name;
    public String description;
    public String categoryStr;
    public byte[] imageByte;
  }

}
