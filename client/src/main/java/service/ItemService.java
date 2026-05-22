package service;

import auction.logic.RequestDTO.ItemDTO;
import auction.logic.enums.ItemCategory;

public class ItemService {
  public static Object addItem(String name , ItemCategory category ,String description){
    try{
      ClientSocket clientSocket=ClientSocket.getInstance();
      ItemDTO  itemDTO = new ItemDTO(name, description, category);
      clientSocket.send(itemDTO);
      Object response = clientSocket.receive();
      return response;
    }catch(RuntimeException e){
      e.printStackTrace();
      return null;
    }
  }
}
