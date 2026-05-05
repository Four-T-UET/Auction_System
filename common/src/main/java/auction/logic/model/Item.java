package auction.logic.model;

public abstract class Item extends Entity{
    private String name;
    private String description;
    public Item(String name,String description){
        super();
        this.name = name;
        this.description = description;
    }
    // Getter - setter
    public String getName(){return this.name;}
    public void setName(String name){this.name = name;}
    public String getDescription(){return this.description;}
    public void setDescription(String description){this.description = description;}
    
}

