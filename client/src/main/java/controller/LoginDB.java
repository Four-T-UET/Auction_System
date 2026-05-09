package controller;


import auction.logic.model.Clients;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LoginDB {
    private static final Map<String, String> USERS = new ConcurrentHashMap<>();

    private LoginDB() {
    }

    public static void ConnectToDatabase() {
        // In-memory fallback for the current project structure.
    }

    public static void insertUser(String username, String password) {
        if (username == null || password == null) {
            return;
        }
        USERS.put(username.trim(), password);
    }

    public static boolean isUserExists(String username) {
        return username != null && USERS.containsKey(username.trim());
    }

    public static boolean isPassword(String username, String password) {
        return username != null && password != null && password.equals(USERS.get(username.trim()));
    }

    /**
     * Lấy hoặc tạo mới Clients object từ username và password
     * (Mỗi lần login sẽ tạo mới instance)
     */
    public static Clients getOrCreateClient(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        username = username.trim();
        
        // Tạo mới Clients mỗi lần login
        Clients client = new Clients(username, password);
        return client;
    }
}



