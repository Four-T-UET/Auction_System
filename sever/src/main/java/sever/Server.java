package sever;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import sever.handler.ClientHandler;
import sever.scheduler.AuctionStatusScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
  private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
  private static final int PORT = 5000;

  private static final ExecutorService clientThreadPool = Executors.newFixedThreadPool(20);

  public static void main(String[] args) {
    // Khởi động scheduler kiểm tra phiên đấu giá hết hạn
    AuctionStatusScheduler scheduler = AuctionStatusScheduler.getInstance();
    scheduler.start();

    // Thêm Shutdown Hook để đóng thread pool an toàn khi tắt Server
    Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdownServer(scheduler)));

    try (ServerSocket serverSocket = new ServerSocket(PORT)) {
      LOGGER.info("Server UET đang chạy tại port {}...", PORT);

      while (!Thread.currentThread().isInterrupted()) {
        Socket clientSocket = serverSocket.accept();
        LOGGER.info("Đã có khách kết nối từ: {}", clientSocket.getRemoteSocketAddress());
        // Giao việc xử lý client cho Thread Pool quản lý
        clientThreadPool.submit(new ClientHandler(clientSocket));
      }

    } catch (IOException e) {
      LOGGER.error("Server error", e);
    }
  }

  // Hàm dọn dẹp tài nguyên khi tắt Server
  private static void shutdownServer(AuctionStatusScheduler scheduler) {
    LOGGER.info("Đang dừng Server an toàn...");
    if (scheduler != null) {
      scheduler.stop();
    }

    clientThreadPool.shutdown();
    try {
      if (!clientThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
        clientThreadPool.shutdownNow();
      }
      LOGGER.info("Thread pool đã đóng.");
    } catch (InterruptedException e) {
      clientThreadPool.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}