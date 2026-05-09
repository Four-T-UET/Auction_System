package sever.handler;

import auction.logic.model.User;
import sever.dao.UserDAO;
import java.io.ObjectOutputStream;

public class LoginHandler implements AuthHandler {

  private UserDAO userDAO = new UserDAO(); // Khởi tạo DAO

  @Override
  public void handle(String[] parts, ObjectOutputStream out) {
    try {
      // parts[0] là "LOGIN", parts[1] là username, parts[2] là password
//      if (parts.length < 3) {
//        out.writeObject("FAILED: Thiếu thông tin đăng nhập");
//        out.flush();
//        return;
//      }


      String user = parts[1];
      String pass = parts[2];

      User userFromDB = userDAO.getUser(user);  // tìm USER từ dtb

      Object response;
      if (userFromDB != null && userFromDB.login(user, pass)) { // gọi hàm login trong User ( logic )
        response = userFromDB; // gán phản hồi là User đó ( là một Object ) vì đã implements Serializable
        System.out.println("Đăng nhập thành công: " + user);
      } else {
        response = "FAILED: Sai tài khoản hoặc mật khẩu";
      }

      // Ghi Object phản hồi
      out.writeObject(response);
      out.flush();
      // Không đóng stream ở đây vì ClientHandler còn dùng tiếp!

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}