package auction.logic.model;

import auction.logic.enums.ItemCategory;

public abstract class Item extends Entity {
    private String name;
    private String description;
    private byte[] imageBytes;
    private ItemCategory category;

    public Item(String name, String description){
        super();
        this.name = name;
        this.description = description;
        this.imageBytes = null;
    }

    // Getter - setter
    public String getName(){return this.name;}
    public void setName(String name){this.name = name;}
    public String getDescription(){return this.description;}
    public void setDescription(String description){this.description = description;}
    public byte[] getImageBytes(){return this.imageBytes;}
    public void setImageBytes(byte[] imageBytes){this.imageBytes = imageBytes;}
    public ItemCategory getCategory(){return this.category;}
    public void setCategory(ItemCategory category){this.category = category;}

}

