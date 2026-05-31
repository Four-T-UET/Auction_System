package auction.logic.factory;

import auction.logic.model.Item;
import auction.logic.model.RealEstate;

public class RealEstateFactory extends ItemFactory{
    @Override
    public Item createItem(String name, String description, byte[] imageByte) {
        return new RealEstate(name, description,imageByte );
    }
}

