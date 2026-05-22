package sever.handler;

import auction.logic.RequestDTO.LoginDTO;
import auction.logic.model.User;
import sever.dao.UserDAO;
import java.io.ObjectOutputStream;

public class LoginHandler {

  private UserDAO userDAO = new UserDAO(); // Khởi tạo DAO

  public void handle(LoginDTO loginDTO, ObjectOutputStream out) {
    try {
      String user = loginDTO.getUsername();
      String pass = loginDTO.getPassword();


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