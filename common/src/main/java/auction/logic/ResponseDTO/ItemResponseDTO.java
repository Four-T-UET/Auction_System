package auction.logic.ResponseDTO;

import java.io.Serializable;

public class ItemResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    public String id;
    public String name;
    public String description;
    public String category;
    public byte[] imageBytes;

    public ItemResponseDTO(String id, String name, String description, String category, byte[] imageBytes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.imageBytes = imageBytes;
    }
}
