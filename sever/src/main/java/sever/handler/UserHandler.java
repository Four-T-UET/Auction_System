package sever.handler;

import auction.logic.RequestDTO.LoginDTO;
import auction.logic.RequestDTO.RegisterDTO;
import auction.logic.ResponseDTO.UserResponseDTO;
import auction.logic.model.Clients;
import auction.logic.model.User;
import sever.dao.UserDAO;
import sever.manager.ClientRuntimeManager;
import sever.manager.ServerClientManager;
import sever.service.UserService;

import java.io.ObjectOutputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserHandler.class);

    private final UserDAO userDAO = new UserDAO();

    public void handleLogin(LoginDTO loginDTO, ObjectOutputStream out) {
        try {
            String username = loginDTO.getUsername();
            String password = loginDTO.getPassword();

            // Xác thực sử dụng phương thức DAO hiện có
            Object userFromDB = userDAO.getUser(username); // Trả về User hoặc CLients
            boolean valid = false;
            if (userFromDB instanceof Clients client) {
                valid = client.login(username, password); // xác thực thông tin
                if (valid) {
                    // Đăng ký userId -> output stream để gửi thông báo có đích (cập nhật ví)
                    ServerClientManager.getInstance().registerUserId(client.getId(), out);
                    // Cập nhật thêm client runtime cache
                    ClientRuntimeManager.getInstance().addOrUpdate(client);
                }
            } else if (userFromDB instanceof User user) {
                valid = user.login(username, password);
                // Nếu chỉ có User chung chung, không lấy được ID; bỏ qua đăng ký
            }

            Object response = valid ? userFromDB : "FAILED: Sai tài khoản hoặc mật khẩu";
            out.writeObject(response);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                out.writeObject("[User Handler]: Lỗi server - " + e.getMessage());
                out.flush();
            } catch (Exception ignored) {}
        }
    }

    public void handleRegister(RegisterDTO registerDTO, ObjectOutputStream out) {
        try {
            String username =  registerDTO.getUsername();
            String password = registerDTO.getPassword();
            // Kiểm tra user đã tồn tại chưa
            if (userDAO.getUser(username) != null) {
                out.writeObject("[User Handler] : Username đã tồn tại");
                out.flush();
                return;
            }
            // Thêm user vào database
            User success = userDAO.insertUser(username, password, "USER");

            if (success instanceof User) {
                out.writeObject(success);
                out.flush();
                LOGGER.info("[User Handler]: Đăng ký thành công: " + username);
            } else {
                out.writeObject("[User Handler]: Lỗi khi đăng ký");
            }
            out.flush();

        } catch (Exception e) {
            LOGGER.error("[User Handler]: Lỗi khi xử lý đăng ký", e);
        }
    }
    public void pull(ObjectOutputStream out){
        try {
            UserService userService = new UserService();
            List<User> list = userService.getAllUsers();
            out.writeObject(list);
            out.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
