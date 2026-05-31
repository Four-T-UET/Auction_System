package auction.logic.model;

import java.io.Serializable;
import java.util.UUID;

public abstract class Entity implements Serializable {
    private String id;

    public Entity() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id){
        this.id=id;
}

}
