package auction.logic.RequestDTO;

import auction.logic.enums.ItemCategory;
import java.io.Serializable;

public class ItemDTO implements Serializable {
  private String name;
  private String description;
  private ItemCategory category;
  private byte[] imageByte;

  public ItemDTO(String name, String description, ItemCategory category) {
    this.name = name;
    this.description = description;
    this.category = category;
    this.imageByte = null;
  }

  public ItemDTO(String name, String description, ItemCategory category, byte[] imageByte) {
    this.name = name;
    this.description = description;
    this.category = category;
    this.imageByte = imageByte;
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

  public byte[] getImageByte(){
    return this.imageByte;
  }

  public void setImageByte(byte[] imageByte) {
    this.imageByte = imageByte;
  }
}
