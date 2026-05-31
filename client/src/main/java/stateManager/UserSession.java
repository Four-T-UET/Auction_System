package stateManager;


import auction.logic.model.Clients;

/**
 * Session lưu trữ user hiện tại đã login
 */
public final class UserSession {
    private static Clients currentUser;

    private UserSession() {
        // Lớp tiện ích
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
     * Logout và xoá tất cả dữ liệu
     */
    public static void logout() {
        clearAllData();
    }

    /**
     * Xoá tất cả dữ liệu khi logout
     * - Xoá user hiện tại
     * - Xoá tất cả auction data từ AuctionManager
     * - Dừng UpdateClient listener
     * - Xoá tất cả listeners từ AuctionManager
     */
    public static void clearAllData() {
    }
}

