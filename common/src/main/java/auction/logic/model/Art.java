package auction.logic.model;

import auction.logic.enums.ItemCategory;

public class Art extends Item {
    public Art(String name, String description){
        super(name, description,ItemCategory.ARTS);
    }
}
