package auction.logic.factory;


import auction.logic.model.Art;
import auction.logic.model.Item;

public class ArtFactory extends ItemFactory {
    @Override
    public Item createItem(String name, String description, byte[] imageByte) {
        return new Art(name, description, imageByte);
    }
}
