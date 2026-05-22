package sever.service;

import auction.logic.enums.AuctionStatus;
import auction.logic.enums.ItemCategory;
import auction.logic.factory.*;
import auction.logic.model.Auction;
import auction.logic.model.Item;
import sever.dao.AuctionDAO;
import sever.dao.AuctionDAO.AuctionRawData;
import sever.dao.ItemDAO;
import sever.dao.ItemDAO.RawItemData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuctionService {
  private final AuctionDAO auctionDAO = AuctionDAO.getInstance();
  private final ItemDAO itemDAO = ItemDAO.getInstance();

  /**
   * Nghiệp vụ Tạo phiên đấu giá mới
   */
  public Auction createAuction(String itemID, double startPrice, double minStep, int duration) {
    try {
      // 1. Gọi ItemDAO mang dữ liệu thô lên (Kết nối DB đã tự đóng dưới DAO)
      RawItemData rawItem = itemDAO.findRawItemById(itemID);

      if (rawItem != null) {
        // 2. Sử dụng Factory dựng đối tượng Item đa hình thực tế
        ItemFactory factory = getFactory(rawItem.categoryStr);
        Item item = factory.createItem(rawItem.name, rawItem.description);
        item.setId(itemID);

        // Chuẩn hóa Enum an toàn
        if (rawItem.categoryStr != null) {
          try {
            item.setCategory(ItemCategory.valueOf(rawItem.categoryStr.toUpperCase().trim()));
          } catch (IllegalArgumentException e) {
            item.setCategory(ItemCategory.REAL_ESTATE);
          }
        }

        // 3. Khởi tạo thực thể Auction theo đúng Constructor của bạn
        Auction auction = new Auction(item, startPrice, minStep, duration);
        
        // ✅ Chuyển trạng thái từ PENDING → RUNNING ngay sau khi tạo
        auction.startAuction();

        // 4. Đẩy sang AuctionDAO lưu xuống database
        auctionDAO.save(auction, duration);

        return auction;
      }
    } catch (SQLException e) {
      System.err.println(">>> Lỗi xử lý nghiệp vụ khi tạo phiên đấu giá: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Nghiệp vụ Lấy danh sách phiên đấu giá đa hình
   */
  public List<Auction> getAllAuctions() {
    List<Auction> processedList = new ArrayList<>();
    try {
      // 1. Nhận danh sách thô từ DAO
      List<AuctionRawData> rawDataList = auctionDAO.pullAllAuctionsRawData();

      // 2. Duyệt qua dữ liệu thô và áp dụng tính đa hình của Factory Pattern
      for (AuctionRawData raw : rawDataList) {
        Auction auction = convertRawDataToAuction(raw);
        if (auction != null) {
          processedList.add(auction);
        }
      }
    } catch (SQLException e) {
      System.err.println(">>> Lỗi xử lý nghiệp vụ lấy danh sách đấu giá: " + e.getMessage());
      e.printStackTrace();
    }
    return processedList;
  }

  /**
   * Lấy thông tin chi tiết của một phiên đấu giá theo ID (dùng cho scheduler)
   */
  public Auction getAuctionById(String auctionId) {
    try {
      AuctionRawData raw = auctionDAO.findRawAuctionById(auctionId);
      if (raw == null) return null;
      return convertRawDataToAuction(raw);
    } catch (SQLException e) {
      System.err.println(">>> Lỗi khi lấy auction theo ID: " + e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Chuyển đổi dữ liệu thô thành đối tượng Auction hoàn chỉnh
   */
  private Auction convertRawDataToAuction(AuctionRawData raw) {
    ItemFactory factory = getFactory(raw.itemCategoryStr);
    Item polymorphicItem = factory.createItem(raw.itemName, raw.itemDescription);
    polymorphicItem.setId(raw.itemId);

    if (raw.itemCategoryStr != null) {
      try {
        polymorphicItem.setCategory(ItemCategory.valueOf(raw.itemCategoryStr.toUpperCase().trim()));
      } catch (IllegalArgumentException e) {
        polymorphicItem.setCategory(ItemCategory.REAL_ESTATE);
      }
    }

    Auction auction = new Auction(polymorphicItem, raw.startPrice, raw.minStep, raw.duration);
    auction.setId(raw.auctionId);
    if (raw.finishTime != null) {
      auction.setFinishTime(raw.finishTime);
    }
    if (raw.status != null) {
      try {
        auction.setStatus(AuctionStatus.valueOf(raw.status));
      } catch (IllegalArgumentException e) {
        auction.setStatus(AuctionStatus.PENDING);
      }
    }
    return auction;
  }

  /**
   * Hàm phụ trợ phân loại chuỗi text sang Factory tương ứng
   */
  private ItemFactory getFactory(String categoryStr) {
    if (categoryStr == null) return new RealEstateFactory();
    try {
      ItemCategory category = ItemCategory.valueOf(categoryStr.toUpperCase().trim());
      return switch (category) {
        case VEHICLE -> new VehicleFactory();
        case ARTS -> new ArtFactory();
        case ELECTRONICS -> new ElectronicsFactory();
        default -> new RealEstateFactory();
      };
    } catch (IllegalArgumentException e) {
      return new RealEstateFactory(); // Dự phòng nếu dữ liệu chuỗi trong DB bị gõ sai
    }
  }
}