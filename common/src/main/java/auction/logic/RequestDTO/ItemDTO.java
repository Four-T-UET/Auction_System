package auction.logic.RequestDTO;

import auction.logic.enums.ItemCategory;
import java.io.Serializable;

public class ItemDTO implements Serializable {
  private String name;
  private String description;
  private ItemCategory category;

  public ItemDTO(String name, String description, ItemCategory category){
    this.name = name;
    this.description = description;
    this.category = category;
  }

  public String getName(){
    return this.name;
  }

  public String getDescription(){
    return this.description;
  }

  public ItemCategory getCategory(){
    return this.category;
  }
}
