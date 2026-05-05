package auction.logic.model;

import java.util.UUID;

public abstract class Entity {
    private String id;
    public Entity(){
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }
}
