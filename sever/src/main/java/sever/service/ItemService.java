package sever.service;

import auction.logic.enums.ItemCategory;
import auction.logic.factory.*;
import auction.logic.model.Item;
import sever.dao.ItemDAO;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemService {
  private final ItemDAO itemDAO = ItemDAO.getInstance();

  public Item createAndSaveItem(String name, ItemCategory category, String description) {
    return createAndSaveItem(name, category, description, null);
  }

  public Item createAndSaveItem(String name, ItemCategory category, String description, byte[] imageBytes) {
    // factory method
    ItemFactory factory = getFactory(category);
    Item item = factory.createItem(name, description, imageBytes);

    //Lưu vào DB thông qua DAO
    itemDAO.addItem(item);

    return item;
  }

  public ItemFactory getFactory(ItemCategory category) {
    return switch (category) {
      case VEHICLE -> new VehicleFactory();
      case ARTS -> new ArtFactory();
      case ELECTRONICS -> new ElectronicsFactory();
      default -> new RealEstateFactory();
    };
  }
}