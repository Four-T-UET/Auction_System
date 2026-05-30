package service;

import auction.logic.ResponseDTO.BroadcastMessage;
import auction.logic.ResponseDTO.ServerTimeSnapshot;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeSyncService {
  private static TimeSyncService instance;
  private volatile long serverTimeOffsetMillis;
  private volatile String serverZoneId = ZoneId.systemDefault().getId();

  private TimeSyncService() {}

  public static synchronized TimeSyncService getInstance() {
    if (instance == null) {
      instance = new TimeSyncService();
    }
    return instance;
  }

  public void updateClock(long serverNowMillis, String zoneId) {
    if (zoneId != null && !zoneId.isBlank()) {
      this.serverZoneId = zoneId;
    }
    if (serverNowMillis > 0) {
      this.serverTimeOffsetMillis = serverNowMillis - System.currentTimeMillis();
    }
  }

  public long getServerTimeMillis() {
    return System.currentTimeMillis() + serverTimeOffsetMillis;
  }

  public ZoneId getServerZoneId() {
    try {
      return serverZoneId == null || serverZoneId.isBlank() ? ZoneId.systemDefault() : ZoneId.of(serverZoneId);
    } catch (Exception e) {
      return ZoneId.systemDefault();
    }
  }

  public long toServerEpochMillis(LocalDateTime dateTime) {
    if (dateTime == null) return -1L;
    return dateTime.atZone(getServerZoneId()).toInstant().toEpochMilli();
  }
}