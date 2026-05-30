package sever.service;

import auction.logic.ResponseDTO.UserResponseDTO;
import auction.logic.model.Clients;
import auction.logic.model.User;
import sever.dao.UserDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    // Khởi tạo DAO duy nhất thông qua Singleton giống như AuctionService
    private final UserDAO userDAO = new UserDAO();

    /**
     * Phương thức Lấy toàn bộ danh sách người dùng
     */
    public List<User> getAllUsers() {
        List<UserResponseDTO> processedList = new ArrayList<>();
        try {
            // Nhận danh sách thô (RawData) từ DB lên để tối ưu bộ nhớ
            List<UserResponseDTO> rawDataList = userDAO.pullAllUsers();

            // Duyệt qua dữ liệu thô và chuyển sang đối tượng common
            for (UserResponseDTO raw : rawDataList) {
                if (raw != null) {
                    processedList.add(raw);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("[User Service] : Lỗi khi lấy danh sách người dùng", e);
        }
        return convertToUserList(processedList);
    }
    /**
     * Chuyển danh sách UserResponseDTO thành danh sách User
     * UserReponseDTO chỉ chứa id và username --> cần tạo ra đúng clients
     */
    private static List<User> convertToUserList(List<UserResponseDTO> dtos) {
        List<User> users = new ArrayList<>();
        if (dtos != null) {
            for (UserResponseDTO dto : dtos) {
                if (dto != null) {
                    // Tạo đối tượng Clients với mật khẩu và vai trò placeholder
                    Clients client = new Clients(dto.username, "", "USER");
                    client.setId(dto.userId);
                    users.add(client);
                }
            }
        }
        return users;
    }
}