package auction.logic.ResponseDTO;

import java.io.Serializable;

public class ServerTimeSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long serverNowMillis;
    private final String serverZoneId;

    public ServerTimeSnapshot(long serverNowMillis, String serverZoneId) {
        this.serverNowMillis = serverNowMillis;
        this.serverZoneId = serverZoneId;
    }

    public long getServerNowMillis() {
        return serverNowMillis;
    }

    public String getServerZoneId() {
        return serverZoneId;
    }
}
