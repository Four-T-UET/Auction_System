package auction.logic.model;

import auction.logic.enums.ItemCategory;

public class RealEstate extends Item {
    public RealEstate(String name, String description, String imagepath){
        super(name, description,imagepath);
        this.setCategory(ItemCategory.REAL_ESTATE);
    }

}


