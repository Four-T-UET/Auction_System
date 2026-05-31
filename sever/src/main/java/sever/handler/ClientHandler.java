package sever.handler;

import auction.logic.RequestDTO.*;
import auction.logic.ResponseDTO.ServerTimeSnapshot;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import sever.service.AuctionService;
import sever.manager.ServerClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);
  private final Socket socket;
  private ObjectOutputStream out;

  public ClientHandler(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    // CHÚ Ý: ObjectOutputStream phải được khởi tạo TRƯỚC ObjectInputStream
    try (ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
         ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

      out = outStream;
      //  ĐĂNG KÝ client vào ServerClientManager khi kết nối
      ServerClientManager.getInstance().registerClient(out);
      LOGGER.info("[ClientHandler] : Client đăng ký để được thông báo");

      while (true) {
        Object received = in.readObject();
        if (received == null) break;



        if (received instanceof LoginDTO loginDTO ){
          UserHandler handler = new UserHandler();
          handler.handleLogin(loginDTO, out);
        }
        else if (received instanceof RegisterDTO registerDTO) {

          UserHandler handler = new UserHandler();
          handler.handleRegister(registerDTO, out);
        }else if(received instanceof AuctionDTO auctionDTO){
          AuctionHandler handler = new AuctionHandler();
          handler.handle(auctionDTO, out);
        }else if(received instanceof TimeSyncRequest){
          out.writeObject(new ServerTimeSnapshot(System.currentTimeMillis(), java.time.ZoneId.systemDefault().getId()));
          out.flush();
        }else if(received instanceof ItemDTO itemDTO){
          ItemHandler handler = new ItemHandler();
          handler.handle(itemDTO, out);
        }else if(received instanceof WalletDTO walletDTO){
          WalletHandler handler = new WalletHandler();
          handler.handle(walletDTO, out);
        }else if(received instanceof BidDTO bidDTO){
          BidHandler handler = new BidHandler();
          handler.handle(bidDTO, out);
        }
        else if(received instanceof PullDTO ){
          AuctionHandler handler = new AuctionHandler();
          handler.pull(out);
        }else if(received instanceof  PullUserDTO){
            UserHandler userHandler = new UserHandler();
            userHandler.pull(out);
        }
        /// ////////////
        else if(received instanceof CancelAuctionDTO cancelAuctionDTO){
            AdminHandler adminHandler = new AdminHandler();
            adminHandler.handleCancel(cancelAuctionDTO, out);
        }
        else {
          out.writeObject("UNDEFINED");
          out.flush();
        }
      }
    }
    // ====================== PHẦN SỬA Ở ĐÂY ======================
    catch (EOFException e) {
      LOGGER.info("[Item Handler] : Client đã ngắt kết nối bình thường (EOF).");
    }
    catch (SocketException e) {
      String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
      if (msg.contains("connection reset") || msg.contains("reset by peer") || msg.contains("socket closed")) {
        LOGGER.warn(" Client đã ngắt kết nối đột ngột (Connection reset).");
      } else {
        LOGGER.warn("SocketException: " + e.getMessage(), e);
      }
    }
    catch (ClassNotFoundException e) {
      LOGGER.error("Lỗi ClassNotFound khi đọc object từ client.", e);
    }
    catch (Exception e) {
      // Các lỗi khác mới in stack trace
      LOGGER.error(" Lỗi xử lý client không mong muốn:", e);
    }
    // ===========================================================
    finally {
      //  HỦY ĐĂNG KÝ client khỏi ServerClientManager khi ngắt kết nối
      if (out != null) {
        ServerClientManager.getInstance().unregisterClient(out);
        LOGGER.info("[ClientHandler] ✓ Client huỷ đăng ký để được thông báo");
      }
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