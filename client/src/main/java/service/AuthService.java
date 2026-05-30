package service;

import auction.logic.RequestDTO.LoginDTO;
import auction.logic.RequestDTO.RegisterDTO;
import auction.logic.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
  public static Object loginRequest(String user, String pass) {
    try{
      ClientSocket clientSocket = ClientSocket.getInstance(); // tạo ClientSocket duy nhất 1 lần;
      LoginDTO loginDTO = new LoginDTO(user, pass);
      clientSocket.send(loginDTO);
      Object response = clientSocket.receive();
      return response;


    } catch (Exception e) {
      LOGGER.warn("Không thể tìm thay user", e);
      return null;
    }
  }

  public static boolean registerRequest(String user, String pass) {
    try {
      ClientSocket clientSocket = ClientSocket.getInstance();
      RegisterDTO registerDTO = new RegisterDTO(user, pass);
      clientSocket.send(registerDTO);
      Object response = clientSocket.receive();

      LOGGER.info("Server Response: " + response); // Dòng debug quan trọng

      if (response instanceof User) {
        return true;
      } else if (response instanceof String) {
        // In ra lỗi: "FAILED: Username đã tồn tại" hoặc "FAILED: Lỗi khi đăng ký"
        LOGGER.warn("Register Error: " + response);
      }
      return false;
    } catch (Exception e) {
      return false;
    }
  }

}