package auction.logic.factory;

import auction.logic.model.Item;

public abstract class ItemFactory {
    public abstract Item createItem(String name, String description, byte[] imageByte);
}
