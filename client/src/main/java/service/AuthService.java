package service;

import auction.logic.RequestDTO.LoginDTO;
import auction.logic.RequestDTO.RegisterDTO;
import auction.logic.model.User;





public class AuthService {
  // test
  public static Object verifyWithServer(String user, String pass) {
    try{
      ClientSocket clientSocket = ClientSocket.getInstance(); // tạo ClientSocket duy nhất 1 lần;
      LoginDTO loginDTO = new LoginDTO(user, pass);
      clientSocket.send(loginDTO);
      Object response = clientSocket.receive();
      return response;


    } catch (Exception e) {
      System.out.println("Can not find the user");
      return null;
    }
  }

  public static boolean registerRequest(String user, String pass) {
    try {
      ClientSocket clientSocket = ClientSocket.getInstance();
      RegisterDTO registerDTO = new RegisterDTO(user, pass);
      clientSocket.send(registerDTO);
      Object response = clientSocket.receive();

      System.out.println("Server Response: " + response); // Dòng debug quan trọng

      if (response instanceof User) {
        return true;
      } else if (response instanceof String) {
        // In ra lỗi thực sự: "FAILED: Username đã tồn tại" hoặc "FAILED: Lỗi khi đăng ký"
        System.err.println("Register Error: " + response);
      }
      return false;
    } catch (Exception e) {
      return false;
    }
  }

}