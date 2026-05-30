package sever.service;

import auction.logic.enums.AuctionStatus;
import auction.logic.enums.ItemCategory;
import auction.logic.factory.*;
import auction.logic.model.Auction;
import auction.logic.model.Item;
import auction.logic.model.Clients;
import auction.logic.ResponseDTO.AuctionResponseDTO;
import auction.logic.ResponseDTO.ItemResponseDTO;
import sever.dao.AuctionDAO;
import sever.dao.ItemDAO;
import sever.manager.AuctionRuntimeManager;
import sever.manager.ClientRuntimeManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import sever.scheduler.AuctionStatusScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuctionService.class);

  private final AuctionDAO auctionDAO = AuctionDAO.getInstance();
  private final ItemDAO itemDAO = ItemDAO.getInstance();

  public Auction createAuction(String itemID, double startPrice, double minStep, int duration, String sellerId) {
    try {
      ItemResponseDTO rawItem = itemDAO.findItemById(itemID);
      if (rawItem != null) {
        ItemFactory factory = getFactory(rawItem.category);
        Item item = factory.createItem(rawItem.name, rawItem.description, rawItem.imageBytes);
        item.setId(itemID);
        if (rawItem.category != null) {
          try {
            item.setCategory(ItemCategory.valueOf(rawItem.category.toUpperCase().trim()));
          } catch (IllegalArgumentException e) {
            item.setCategory(ItemCategory.REAL_ESTATE);
          }
        }
        Auction auction = new Auction(item, startPrice, minStep, duration);
        auction.startAuction();
        if (sellerId != null && !sellerId.isBlank()) {
          Clients seller = ClientRuntimeManager.getInstance().getOrLoad(sellerId);
          if (seller != null) {
            auction.setSellerSnapshot(seller);
          }
        }
        auctionDAO.save(auction, duration, sellerId);
        AuctionStatusScheduler.getInstance().scheduleAuctionEnd(auction.getId(), auction.getFinishTime());
        return auction;
      }
    } catch (SQLException e) {
      LOGGER.error("[AuctionService]: Lỗi xử lý nghiệp vụ khi tạo phiên đấu giá", e);
    }
    return null;
  }
  /// /////////////////////////////////////////////
  public Auction cancelActiveAuction(String auctionId) {
    if (auctionId == null || auctionId.isBlank()) {
      LOGGER.warn("[AuctionService]: Auction ID là null");
      return null;
    }

    try {
      AuctionResponseDTO responseDB = auctionDAO.findAuctionById(auctionId);
      if (responseDB == null) {
        return null;
      }

      AuctionStatus currentStatus;
      try {
        currentStatus = AuctionStatus.valueOf(responseDB.status);
      } catch (IllegalArgumentException e) {
        LOGGER.warn("[AuctionService]: Status không hợp lệ: " + responseDB.status, e);
        currentStatus = AuctionStatus.FINISHED;
      }

      if (currentStatus == AuctionStatus.FINISHED
              || currentStatus == AuctionStatus.PAID
              || currentStatus == AuctionStatus.CANCELLED) {
        LOGGER.warn("[AuctionService]: Không thể cancel Auction: " + currentStatus);
        return null;
      }

      Auction auction = convertDTOToAuction(responseDB);
      if (auction == null) return null;

      auction.cancelAuction(); // release tiền ở đây

      auctionDAO.updateStatus(auctionId, AuctionStatus.CANCELLED.name());
      return auction;

    } catch (SQLException e) {
      LOGGER.error("[AuctionService]: Lỗi khi cancel Auction", e);
      return null;
    }
  }
  /// ////////////////////////////////////////////////////////

  public List<Auction> getAllAuctions() {
    List<Auction> processedList = new ArrayList<>();
    try {
      List<AuctionResponseDTO> rawDataList = auctionDAO.pullAllAuctionsData();
      for (AuctionResponseDTO raw : rawDataList) {
        /// //////////////
        // 1) Ưu tiên lấy từ cache
        Auction cached = AuctionRuntimeManager.getInstance().getFromCache(raw.auctionId);
        if (cached != null) {
          processedList.add(cached);
          continue;
        }
        /// ////////////////
        // không có trong cache thì mới tạo mới từ DB
        Auction auction = convertDTOToAuction(raw);
        if (auction != null) {
          /// ///////////////////////////////////////
          // Câập nhật vào trong cache
          AuctionRuntimeManager.getInstance().addOrUpdate(auction);
          processedList.add(auction);
          /// /////////////////////////////////////////
        }
      }
    } catch (SQLException e) {
      LOGGER.error("[AuctionService]: Lỗi xử lý lấy danh sách đấu giá", e);
      e.printStackTrace();
    }
    return processedList;
  }

  public Auction getAuctionById(String auctionId) {
    try {
      AuctionResponseDTO raw = auctionDAO.findAuctionById(auctionId);
      if (raw == null) return null;
      return convertDTOToAuction(raw);
    } catch (SQLException e) {
      LOGGER.error("[AuctionService]: Lỗi khi lấy auction theo ID", e);
      return null;
    }
  }

  private Auction convertDTOToAuction(AuctionResponseDTO raw) {
    ItemFactory factory = getFactory(raw.itemCategoryStr);
    Item polymorphicItem = factory.createItem(raw.itemName, raw.itemDescription, raw.itemImageByte);
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
    if (raw.currentWinnerId != null && !raw.currentWinnerId.isBlank()) {
      Clients winner = ClientRuntimeManager.getInstance().getOrLoad(raw.currentWinnerId);
      if (winner != null) {
        auction.setCurrentWinnerSnapshot(winner);
      }
    }
    if (raw.sellerId != null && !raw.sellerId.isBlank()) {
      Clients seller = ClientRuntimeManager.getInstance().getOrLoad(raw.sellerId);
      if (seller != null) {
        auction.setSellerSnapshot(seller);
      }
    }
    return auction;
  }

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
      return new RealEstateFactory();
    }
  }
}
