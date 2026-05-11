package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientSocket {
  private static ClientSocket instance;
  private Socket socket;
  private ObjectOutputStream out;
  private ObjectInputStream in;

  // Singleton: Đảm bảo cả ứng dụng chỉ có 1 kết nối duy nhất
  private ClientSocket() {
    try {
      socket = new Socket("10.11.1.146", 5000);
      out = new ObjectOutputStream(socket.getOutputStream()); // lấy dữ liệu output stream của socket localhost
      in = new ObjectInputStream(socket.getInputStream()); // đọc dữ liệu sẽ được gửi lại từ sever: byte --> text
    } catch (IOException e) {
      System.err.println("Không thể kết nối tới Server! " + e.getMessage());
    }
  }

  public static ClientSocket getInstance() {
    if (instance == null) {
      instance = new ClientSocket();
    }
    return instance;
  }

  // Phương thức dùng chung cho mọi tính năng
  public synchronized Object sendAndReceive(Object data) {
    try {
      out.reset();
      out.writeObject(data);
      out.flush();
      out.reset();
      return in.readObject();
    } catch (IOException e) {
      return "ERROR";
    } catch (ClassNotFoundException e) {
      return "ERROR";
    }
  }
}