package sever.handler;

import auction.logic.RequestDTO.ItemDTO;
import auction.logic.enums.ItemCategory;
import auction.logic.model.Item;
import java.io.ObjectOutputStream;
import sever.dao.ItemDAO;
import sever.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(ItemHandler.class);
  public void handle(ItemDTO itemDTO, ObjectOutputStream out) {
    try{
      ItemService itemService = new ItemService();
      String name = itemDTO.getName();
      String description = itemDTO.getDescription();
      ItemCategory category = itemDTO.getCategory();
      byte[] imageByte = itemDTO.getImageByte();

      Item itemFromDB = itemService.createAndSaveItem(name, category, description, imageByte);
      Object response;

      if (itemFromDB != null) {
        response = itemFromDB;
        LOGGER.info("[Item Handler] : Them san pham thanh cong " + name);
      } else {
        response = "FAILED";
      }

      // Ghi Object phản hồi

      out.writeObject(response);
      out.flush();
    } catch (Exception e) {
      LOGGER.error("[Item Handler] : Lỗi khi xử ly item request", e);
    }
  }
}
