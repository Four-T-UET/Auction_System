package service;

import auction.logic.model.User;
import java.io.IOException;


public class AuthService {
  public boolean verifyWithServer(String user, String pass) {
    try{
      ClientSocket clientSocket = ClientSocket.getInstance(); // tạo ClientSocket duy nhất 1 lần;

      Object response = clientSocket.sendAndReceive("LOGIN|" + user + "|" + pass);

      if(response instanceof User){
        return true;
      }
      return false;
//      out.println(user + "|" + pass);   // ghi dữ liệu vào buffer(ram) PrintWriter ở trên
//      out.flush(); // ép buffer ghi xuống socket và gửi sang sever
//      return in.readLine(); // Nhận "SUCCESS" hoặc "FAIL"  // phản hồi từ sever đây

    } catch (Exception e) {
      System.out.println("Can not find the user");
      return false;
    }
  }
}