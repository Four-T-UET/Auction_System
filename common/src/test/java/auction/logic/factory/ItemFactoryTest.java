package auction.logic.factory;

import auction.logic.enums.ItemCategory;
import auction.logic.model.Item;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class ItemFactoryTest {
    private final byte[] fakeImage = new byte[]{1, 2, 3};
    @Test
    @DisplayName("ArtFactory - Tạo Art item đúng category")
    void artFactory_shouldCreateArtItem() {
        ItemFactory factory = new ArtFactory();
        Item item = factory.createItem("Bức tranh", "Tranh sơn dầu", fakeImage);

        assertThat(item).isNotNull();
        assertThat(item.getName()).isEqualTo("Bức tranh");
        assertThat(item.getCategory()).isEqualTo(ItemCategory.ARTS);
    }

    @Test
    @DisplayName("ElectronicsFactory - Tạo Electronics item đúng category")
    void electronicsFactory_shouldCreateElectronicsItem() {
        ItemFactory factory = new ElectronicsFactory();
        Item item = factory.createItem("Laptop", "Laptop gaming", fakeImage);

        assertThat(item.getCategory()).isEqualTo(ItemCategory.ELECTRONICS);
        assertThat(item.getDescription()).isEqualTo("Laptop gaming");
    }

    @Test
    @DisplayName("VehicleFactory - Tạo Vehicle item đúng category")
    void vehicleFactory_shouldCreateVehicleItem() {
        ItemFactory factory = new VehicleFactory();
        Item item = factory.createItem("Xe máy", "Xe số 2024", fakeImage);

        assertThat(item.getCategory()).isEqualTo(ItemCategory.VEHICLE);
    }

    @Test
    @DisplayName("RealEstateFactory - Tạo RealEstate item đúng category")
    void realEstateFactory_shouldCreateRealEstateItem() {
        ItemFactory factory = new RealEstateFactory();
        Item item = factory.createItem("Căn hộ", "Quận 7", fakeImage);

        assertThat(item.getCategory()).isEqualTo(ItemCategory.REAL_ESTATE);
    }

    @Test
    @DisplayName("Tất cả Factory - Item được tạo phải có ID")
    void allFactories_shouldGenerateId() {
        ItemFactory artFactory = new ArtFactory();
        ItemFactory vehicleFactory = new VehicleFactory();

        Item art = artFactory.createItem("A", "B", fakeImage);
        Item vehicle = vehicleFactory.createItem("C", "D", fakeImage);

        assertThat(art.getId()).isNotNull().isNotBlank();
        assertThat(vehicle.getId()).isNotNull().isNotBlank();
        assertThat(art.getId()).isNotEqualTo(vehicle.getId());
    }
}