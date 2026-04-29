package controller.etrade;

import model.Clients;

/**
 * Session lưu trữ user hiện tại đã login
 */
public final class UserSession {
    private static Clients currentUser;

    private UserSession() {
        // Utility class
    }

    /**
     * Set user hiện tại khi login thành công
     */
    public static void setCurrentUser(Clients user) {
        currentUser = user;
    }

    /**
     * Lấy user hiện tại
     */
    public static Clients getCurrentUser() {
        return currentUser;
    }

    /**
     * Kiểm tra có user login chưa
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Logout
     */
    public static void logout() {
        currentUser = null;
    }
}

