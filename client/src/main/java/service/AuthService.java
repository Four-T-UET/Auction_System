package service;

import auction.logic.model.User;
import java.io.IOException;



public class AuthService {
  // test
  public static Object verifyWithServer(String user, String pass) {
    try{
      ClientSocket clientSocket = ClientSocket.getInstance(); // tạo ClientSocket duy nhất 1 lần;
//
      Object response = clientSocket.sendAndReceive("LOGIN|" + user + "|" + pass);

      return response;
//      out.println(user + "|" + pass);   // ghi dữ liệu vào buffer(ram) PrintWriter ở trên
//      out.flush(); // ép buffer ghi xuống socket và gửi sang sever
//      return in.readLine(); // Nhận "SUCCESS" hoặc "FAIL"  // phản hồi từ sever đây

    } catch (Exception e) {
      System.out.println("Can not find the user");
      return null;
    }
  }
  //  public static boolean registerRequest(String user, String pass) {
//
//    try{
//      ClientSocket clientSocket = ClientSocket.getInstance();
//      Object response = clientSocket.sendAndReceive("REGISTER|" + user + "|" + pass);
//      if(response instanceof User){
//        return true;
//      }
//      return false;
//    } catch (Exception e) {
//      return false;
//    }
//  }
  public static boolean registerRequest(String user, String pass) {
    try {
      ClientSocket clientSocket = ClientSocket.getInstance();
      Object response = clientSocket.sendAndReceive("REGISTER|" + user + "|" + pass);

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