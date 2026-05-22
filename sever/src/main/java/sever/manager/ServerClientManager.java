package sever.manager;

import auction.logic.ResponseDTO.BroadcastMessage;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.time.ZoneId;

/**
 * Server-side: Quản lý tất cả connected clients
 * Chịu trách nhiệm broadcast message tới tất cả clients
 */
public class ServerClientManager {
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
            System.out.println("[SERVER MANAGER] Client registered. Total clients: " + connectedClients.size());
        }
    }

    /**
     * Đăng ký mapping userId -> output stream sau khi login thành công
     */
    public void registerUserId(String userId, ObjectOutputStream out) {
        if (userId != null && out != null) {
            userIdToStream.put(userId, out);
            System.out.println("[SERVER MANAGER] Registered user: " + userId);
        }
    }

    /**
     * Hủy đăng ký client khi ngắt kết nối
     */
    public synchronized void unregisterClient(ObjectOutputStream out) {
        // Remove from connectedClients
        boolean removed = connectedClients.removeIf(client -> client.getOut() == out);
        // Remove from userIdToStream (find by stream value)
        userIdToStream.entrySet().removeIf(entry -> entry.getValue() == out);
        if (removed) {
            System.out.println("[SERVER MANAGER] Client unregistered. Total clients: " + connectedClients.size());
        }
    }

    /**
     * Gửi message đến một user cụ thể
     */
    public void sendToUser(String userId, Object message) {
        ObjectOutputStream out = userIdToStream.get(userId);
        if (out == null) {
            System.err.println("[SERVER MANAGER] User not found or not connected: " + userId);
            return;
        }
        try {
            synchronized (out) {
                out.reset();
                out.writeObject(message);
                out.flush();
                out.reset();
            }
            System.out.println("[SERVER MANAGER] Sent to user " + userId);
        } catch (Exception e) {
            System.err.println("[SERVER MANAGER] Failed to send to user " + userId + ": " + e.getMessage());
            unregisterClient(out);
        }
    }

    /**
     * Broadcast message tới TẤT CẢ connected clients NGOẠI TRỪ sender
     */
    public void broadcastToAllExcept(BroadcastMessage message, ObjectOutputStream senderOut) {
        stampServerTime(message);
        System.out.println("[SERVER MANAGER] Broadcasting: " + message);
        for (ClientInfo client : connectedClients) {
            // Không gửi lại cho người gửi
            if (client.getOut() == senderOut) {
                continue;
            }
            try {
                synchronized (client.getOut()) {
                    client.getOut().reset();
                    client.getOut().writeObject(message);
                    client.getOut().flush();
                    client.getOut().reset();
                }
                System.out.println("[SERVER MANAGER] ✓ Sent to client");
            } catch (Exception e) {
                System.err.println("[SERVER MANAGER] ✗ Failed to broadcast to client: " + e.getMessage());
                unregisterClient(client.getOut());
            }
        }
    }

    /**
     * Broadcast tới TẤT CẢ clients (including sender)
     */
    public void broadcastToAll(BroadcastMessage message) {
        stampServerTime(message);
        System.out.println("[SERVER MANAGER] Broadcasting to ALL: " + message);
        for (ClientInfo client : connectedClients) {
            try {
                synchronized (client.getOut()) {
                    client.getOut().reset();
                    client.getOut().writeObject(message);
                    client.getOut().flush();
                    client.getOut().reset();
                }
                System.out.println("[SERVER MANAGER] ✓ Sent to client");
            } catch (Exception e) {
                System.err.println("[SERVER MANAGER] ✗ Failed to broadcast to client: " + e.getMessage());
                unregisterClient(client.getOut());
            }
        }
    }

    /**
     * Inner class để wrap client connection
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
