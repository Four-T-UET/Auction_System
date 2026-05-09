package auction.logic.model;

import auction.logic.enums.ItemCategory;

public abstract class Item extends Entity {
    private String name;
    private String description;
    private String imagePath;
    private byte[] imageBytes;
    private ItemCategory category;

    public Item(String name, String description, String imagePath){
        super();
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.imageBytes = null;
        this.category = ItemCategory.NOT_DEFINE;
    }

    // Getter - setter
    public String getName(){return this.name;}
    public void setName(String name){this.name = name;}
    public String getDescription(){return this.description;}
    public void setDescription(String description){this.description = description;}
    public String getImagePath(){return this.imagePath;}
    public void setImagePath(String imagePath){this.imagePath = imagePath;}
    public byte[] getImageBytes(){return this.imageBytes;}
    public void setImageBytes(byte[] imageBytes){this.imageBytes = imageBytes;}
    public ItemCategory getCategory(){return this.category;}
    public void setCategory(ItemCategory category){this.category = category;}

}

