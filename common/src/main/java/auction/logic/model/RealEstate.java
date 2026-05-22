package auction.logic.model;

import auction.logic.enums.ItemCategory;

public class RealEstate extends Item {
    public RealEstate(String name, String description){
        super(name, description, ItemCategory.REAL_ESTATE);
    }

}


