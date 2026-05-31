package sever.manager;

import auction.logic.ResponseDTO.BroadcastMessage;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Phía Server: Quản lý tất cả client đã kết nối
 * Gửi thông báo broadcast tới tất cả clients
 */
public class ServerClientManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerClientManager.class);
    private static ServerClientManager instance;
    private final CopyOnWriteArrayList<ClientInfo> connectedClients = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<String, ObjectOutputStream> userIdToStream = new ConcurrentHashMap<>();

    private ServerClientManager() {}

    public static ServerClientManager getInstance() {
        if (instance == null) {
            instance = new ServerClientManager();
        }
        return instance;
    }

    /**
     * Đăng ký client khi kết nối
     */
    public synchronized void registerClient(ObjectOutputStream out) {
        if (out != null) {
            ClientInfo info = new ClientInfo(out);
            connectedClients.add(info);
            LOGGER.info("[ServerClientManager] : Đăng ký Client. Tổng Clients: " + connectedClients.size());
        }
    }

    /**
     * Đăng ký mapping userId -> output stream sau khi login thành công
     */
    public void registerUserId(String userId, ObjectOutputStream out) {
        if (userId != null && out != null) {
            userIdToStream.put(userId, out);
            LOGGER.info("[ServerClientManager] : Đăng ký User: " + userId);
        }
    }

    /**
     * Hủy đăng ký client khi ngắt kết nối
     */
    public synchronized void unregisterClient(ObjectOutputStream out) {
        // Xóa khỏi connectedClients
        boolean removed = connectedClients.removeIf(client -> client.getOut() == out);
        // Xóa khỏi userIdToStream (tìm theo giá trị stream)
        userIdToStream.entrySet().removeIf(entry -> entry.getValue() == out);
        if (removed) {
            LOGGER.info("[ServerClientManager] : Huỷ đăng ky Client. Tổng clients: " + connectedClients.size());
        }
    }

    /**
     * Gửi message đến một user cụ thể
     */
    public void sendToUser(String userId, Object message) {
        ObjectOutputStream out = userIdToStream.get(userId);
        if (out == null) {
            LOGGER.warn("[ServerClientManager] : Không thể kết nối / không tìm thấy User: " + userId);
            return;
        }
        try {
            synchronized (out) {
                out.reset();
                out.writeObject(message);
                out.flush();
                out.reset();
            }
            LOGGER.info("[ServerClientManager] : Gửi cho user " + userId);
        } catch (Exception e) {
            LOGGER.error("[ServerClientManager] : Gửi cho user thất bại " + userId, e);
            unregisterClient(out);
        }
    }
    /**
     * Broadcast tới TẤT CẢ clients (bao gồm cả người gửi)
     */
    public void broadcastToAll(BroadcastMessage message) {
        stampServerTime(message);
        LOGGER.info("[ServerClientManager] : Thông báo tới tất cả: " + message);
        for (ClientInfo client : connectedClients) {
            try {
                synchronized (client.getOut()) {
                    client.getOut().reset();
                    client.getOut().writeObject(message);
                    client.getOut().flush();
                    client.getOut().reset();
                }
                LOGGER.info("[ServerClientManager] : Gửi cho client");
            } catch (Exception e) {
                LOGGER.error("[ServerClientManager] : thông báo cho cac client thất bại", e);
                unregisterClient(client.getOut());
            }
        }
    }

    /**
     * Lớp nội bộ để đóng gói kết nối client
     */
    private static class ClientInfo {
        private final ObjectOutputStream out;

        ClientInfo(ObjectOutputStream out) {
            this.out = out;
        }

        ObjectOutputStream getOut() {
            return out;
        }
    }

    private void stampServerTime(BroadcastMessage message) {
        if (message == null) {
            return;
        }
        message.setServerNowMillis(System.currentTimeMillis());
        message.setServerZoneId(ZoneId.systemDefault().getId());
    }
}