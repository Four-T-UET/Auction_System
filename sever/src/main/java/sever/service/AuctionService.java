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
import sever.manager.ClientRuntimeManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuctionService {
  private final AuctionDAO auctionDAO = AuctionDAO.getInstance();
  private final ItemDAO itemDAO = ItemDAO.getInstance();

  public Auction createAuction(String itemID, double startPrice, double minStep, int duration, String sellerId) {
    try {
      ItemResponseDTO rawItem = itemDAO.findRawItemById(itemID);
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
        return auction;
      }
    } catch (SQLException e) {
      System.err.println(">>> Lỗi xử lý nghiệp vụ khi tạo phiên đấu giá: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  public List<Auction> getAllAuctions() {
    List<Auction> processedList = new ArrayList<>();
    try {
      List<AuctionResponseDTO> rawDataList = auctionDAO.pullAllAuctionsRawData();
      for (AuctionResponseDTO raw : rawDataList) {
        Auction auction = convertDTOToAuction(raw);
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

  public Auction getAuctionById(String auctionId) {
    try {
      AuctionResponseDTO raw = auctionDAO.findRawAuctionById(auctionId);
      if (raw == null) return null;
      return convertDTOToAuction(raw);
    } catch (SQLException e) {
      System.err.println(">>> Lỗi khi lấy auction theo ID: " + e.getMessage());
      e.printStackTrace();
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
