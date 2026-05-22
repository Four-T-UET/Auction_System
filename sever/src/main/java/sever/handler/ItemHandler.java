package sever.handler;

import auction.logic.RequestDTO.ItemDTO;
import auction.logic.enums.ItemCategory;
import auction.logic.model.Item;
import java.io.ObjectOutputStream;
import sever.dao.ItemDAO;
import sever.service.ItemService;

public class ItemHandler {
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
        System.out.println("Them san pham thanh cong " + name);
      } else {
        response = "FAILED";
      }

      // Ghi Object phản hồi

      out.writeObject(response);
      out.flush();


    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}





