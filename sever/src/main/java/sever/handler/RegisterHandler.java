
package sever.handler;

import auction.logic.model.User;
import java.io.ObjectOutputStream;
import sever.dao.UserDAO;

public class RegisterHandler implements AuthHandler {
    private UserDAO userDAO = new UserDAO();

    @Override
    public void handle(String[] parts, ObjectOutputStream out) {
        try {
            // parts[0] = "REGISTER"
            // parts[1] = username
            // parts[2] = password

            String username = parts[1];
            String password = parts[2];

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