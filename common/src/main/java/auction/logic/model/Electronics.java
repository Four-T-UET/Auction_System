package auction.logic.model;

import auction.logic.enums.ItemCategory;

public class Electronics extends Item {

    public Electronics(String name, String description, byte[] imageByte) {
        super(name, description, ItemCategory.ELECTRONICS,  imageByte);
    }
}