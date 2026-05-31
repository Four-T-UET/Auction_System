package auction.logic.factory;

import auction.logic.model.Item;
import auction.logic.model.Vehicle;

public class VehicleFactory extends ItemFactory {
    @Override
    public Item createItem(String name, String description, byte[] imageByte) {
        return new Vehicle(name, description, imageByte);
    }
}
