package auction.logic.model;

import auction.logic.enums.ItemCategory;

public class Vehicle extends Item {
    public Vehicle(String name, String description, byte[] imageByte) {
        super(name, description, ItemCategory.VEHICLE, imageByte);
    }
}
