package auction.logic.ResponseDTO;

import auction.logic.model.Auction;
import java.io.Serializable;
import java.time.ZoneId;

/**
 * Broadcast message từ Server gửi tới tất cả connected clients
 * Khi có auction mới được tạo
 */
public class BroadcastMessage implements Serializable {
    private static final long serialVersionUID = 2L; // Changed version due to new field

    public enum EventType {
        AUCTION_CREATED,     // Auction mới được tạo
        AUCTION_UPDATED,     // Auction được update (price, bid, v.v)
        AUCTION_REMOVED,     // Auction bị xóa
        AUCTION_FINISHED,    // Auction đã kết thúc
        WALLET_UPDATED       // Wallet balance/locked changed
    }

    private EventType eventType;
    private Auction auction;
    private String auctionId;  // dùng khi type = REMOVED
    private long serverNowMillis;
    private String serverZoneId;
    private Object data;       // Generic payload, e.g., WalletResponseDTO for WALLET_UPDATED

    public BroadcastMessage() {}

    public BroadcastMessage(EventType eventType, Auction auction) {
        this.eventType = eventType;
        this.auction = auction;
    }

    public BroadcastMessage(EventType eventType, String auctionId) {
        this.eventType = eventType;
        this.auctionId = auctionId;
    }

    public BroadcastMessage(EventType eventType, Object data) {
        this.eventType = eventType;
        this.data = data;
    }

    public long getServerNowMillis() {
        return serverNowMillis;
    }

    public void setServerNowMillis(long serverNowMillis) {
        this.serverNowMillis = serverNowMillis;
    }

    public String getServerZoneId() {
        return serverZoneId;
    }

    public void setServerZoneId(String serverZoneId) {
        this.serverZoneId = serverZoneId;
    }

    public ZoneId getServerZone() {
        if (serverZoneId == null || serverZoneId.isBlank()) {
            return ZoneId.systemDefault();
        }
        try {
            return ZoneId.of(serverZoneId);
        } catch (Exception ignored) {
            return ZoneId.systemDefault();
        }
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BroadcastMessage{" +
                "eventType=" + eventType +
                ", auction=" + (auction != null ? auction.getId() : "null") +
                ", auctionId='" + auctionId + '\'' +
                ", data=" + data +
                ", serverNowMillis=" + serverNowMillis +
                ", serverZoneId='" + serverZoneId + '\'' +
                '}';
    }
}
