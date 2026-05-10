package sever.handler;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {
  private final Socket socket;

  public ClientHandler(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    // Lưu ý: ObjectOutputStream phải được khởi tạo TRƯỚC ObjectInputStream
    try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
         ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

      while (true) {
        Object received = in.readObject();
        if (received == null) break;

        String line = received.toString();
        System.out.println("Server nhận được: [" + line + "]");

        String[] parts = line.split("\\|");
        String command = parts[0];

        if ("LOGIN".equals(command)) {
          AuthHandler handler = new LoginHandler();
          handler.handle(parts, out);
        } else if ("REGISTER".equals(command)) {

          AuthHandler handler = new RegisterHandler();
          handler.handle(parts, out);
        } else {
          out.writeObject("UNDEFINED");
          out.flush();
        }
      }

    }
    // ====================== PHẦN SỬA Ở ĐÂY ======================
    catch (EOFException e) {
      System.out.println("✅ Client đã ngắt kết nối bình thường (EOF).");
    }
    catch (SocketException e) {
      String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
      if (msg.contains("connection reset") || msg.contains("reset by peer") || msg.contains("socket closed")) {
        System.out.println("✅ Client đã ngắt kết nối đột ngột (Connection reset).");
      } else {
        System.err.println("SocketException: " + e.getMessage());
      }
    }
    catch (ClassNotFoundException e) {
      System.err.println("Lỗi ClassNotFound khi đọc object từ client.");
      e.printStackTrace();
    }
    catch (Exception e) {
      // Các lỗi khác mới in stack trace
      System.err.println("❌ Lỗi xử lý client không mong muốn:");
      e.printStackTrace();
    }
    // ===========================================================
    finally {
      try {
        if (socket != null && !socket.isClosed()) {
          socket.close();
        }
      } catch (IOException e) {
        // im lặng, không cần in lỗi close
      }
    }
  }
}