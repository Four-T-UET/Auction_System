package sever.service;

import auction.logic.enums.ItemCategory;
import auction.logic.factory.ArtFactory;
import auction.logic.factory.ElectronicsFactory;
import auction.logic.factory.RealEstateFactory;
import auction.logic.factory.VehicleFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemServiceTest {
  @Test
  void getFactoryReturnsExpectedFactory() {
    ItemService service = new ItemService();

    assertTrue(service.getFactory(ItemCategory.VEHICLE) instanceof VehicleFactory);
    assertTrue(service.getFactory(ItemCategory.ARTS) instanceof ArtFactory);
    assertTrue(service.getFactory(ItemCategory.ELECTRONICS) instanceof ElectronicsFactory);
    assertTrue(service.getFactory(ItemCategory.REAL_ESTATE) instanceof RealEstateFactory);
  }
}

