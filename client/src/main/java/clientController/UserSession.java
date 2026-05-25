package clientController;


import auction.logic.model.Clients;
import auction.logic.manager.AuctionManager;
import service.UpdateClient;
import java.util.ArrayList;

/**
 * Session lưu trữ user hiện tại đã login
 */
public final class UserSession {
    private static Clients currentUser;
    private static UpdateClient updateClient;

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
     * Set UpdateClient instance để có thể dừng nó khi logout
     */
    public static void setUpdateClient(UpdateClient client) {
        updateClient = client;
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
        // Xoá user hiện tại
        currentUser = null;

        // Dừng UpdateClient nếu đang chạy
        if (updateClient != null) {
            updateClient.stop();
            updateClient = null;
        }

        // Xoá tất cả auction data từ AuctionManager
        AuctionManager.getInstance().replaceAll(new ArrayList<>());

        // Xoá tất cả listeners từ AuctionManager
        AuctionManager.getInstance().clearAllListeners();

        System.out.println("Tất cả dữ liệu đã được xoá khi logout");
    }
}

