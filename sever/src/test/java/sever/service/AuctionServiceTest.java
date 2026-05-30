package sever.service;

import auction.logic.ResponseDTO.AuctionResponseDTO;
import auction.logic.enums.AuctionStatus;
import auction.logic.enums.ItemCategory;
import auction.logic.model.Auction;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuctionServiceTest {
  @Test
  void convertDTOToAuction_defaultsInvalidFields() throws Exception {
    AuctionService service = new AuctionService();
    LocalDateTime finish = LocalDateTime.now().plusMinutes(10);

    AuctionResponseDTO dto = new AuctionResponseDTO(
        "a1",
        "i1",
        100.0,
        5.0,
        60,
        finish,
        "INVALID",
        null,
        null,
        "Laptop",
        "Desc",
        "invalid_category",
        new byte[0]
    );

    Auction auction = invokeConvert(service, dto);

    assertNotNull(auction);
    assertEquals("a1", auction.getId());
    assertEquals("i1", auction.getItem().getId());
    assertEquals(ItemCategory.REAL_ESTATE, auction.getItem().getCategory());
    assertEquals(AuctionStatus.PENDING, auction.getStatus());
    assertEquals(finish, auction.getFinishTime());
    assertEquals(100.0, auction.getCurrentPrice(), 0.0001);
    assertEquals(5.0, auction.getMiniumStep(), 0.0001);
  }

  @Test
  void convertDTOToAuction_acceptsValidCategoryAndStatus() throws Exception {
    AuctionService service = new AuctionService();
    LocalDateTime finish = LocalDateTime.now().plusMinutes(5);

    AuctionResponseDTO dto = new AuctionResponseDTO(
        "a2",
        "i2",
        50.0,
        2.0,
        30,
        finish,
        AuctionStatus.RUNNING.name(),
        "",
        "",
        "TV",
        "Desc",
        "electronics",
        new byte[0]
    );

    Auction auction = invokeConvert(service, dto);

    assertEquals(ItemCategory.ELECTRONICS, auction.getItem().getCategory());
    assertEquals(AuctionStatus.RUNNING, auction.getStatus());
  }

  private Auction invokeConvert(AuctionService service, AuctionResponseDTO dto) throws Exception {
    Method method = AuctionService.class.getDeclaredMethod("convertDTOToAuction", AuctionResponseDTO.class);
    method.setAccessible(true);
    return (Auction) method.invoke(service, dto);
  }
}

