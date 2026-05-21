package sever;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import sever.handler.ClientHandler;
import sever.handler.LoginHandler;
import sever.scheduler.AuctionStatusScheduler;

public class Server {
  public static void main(String[] args) {
    // Khởi động scheduler kiểm tra phiên đấu giá hết hạn
    AuctionStatusScheduler scheduler = new AuctionStatusScheduler();
    scheduler.start();

    try (ServerSocket serverSocket = new ServerSocket(5000)) {
      System.out.println("Server UET đang chạy tại port 5000...");
      while (true) {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Đã có khách kết nối");
        new Thread(new ClientHandler(clientSocket)).start();
      }
    } catch (IOException e) { e.printStackTrace(); }
  }
}
