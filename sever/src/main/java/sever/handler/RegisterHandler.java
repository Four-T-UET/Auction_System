
package sever.handler;

import auction.logic.RequestDTO.RegisterDTO;
import auction.logic.model.User;
import java.io.ObjectOutputStream;
import sever.dao.UserDAO;

public class RegisterHandler {
    private UserDAO userDAO = new UserDAO();

    public void handle(RegisterDTO registerDTO, ObjectOutputStream out) {
        try {
            String username =  registerDTO.getUsername();
            String password = registerDTO.getPassword();

            // Kiểm tra user đã tồn tại chưa
            if (userDAO.getUser(username) != null) {
                out.writeObject("FAILED: Username đã tồn tại");
                out.flush();
                return;
            }

            // Insert user vào database
            User success = userDAO.insertUser(username, password);

            if (success instanceof User) {
                out.writeObject(success);
                out.flush();
                System.out.println(" Đăng ký thành công: " + username);
            } else {
                out.writeObject("FAILED: Lỗi khi đăng ký");
            }
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}