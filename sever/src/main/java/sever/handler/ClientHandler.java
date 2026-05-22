package sever.handler;

import auction.logic.RequestDTO.*;
import auction.logic.ResponseDTO.ServerTimeSnapshot;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import sever.service.AuctionService;
import sever.manager.ServerClientManager;

public class ClientHandler implements Runnable {
  private final Socket socket;
  private ObjectOutputStream out;

  public ClientHandler(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    // Lưu ý: ObjectOutputStream phải được khởi tạo TRƯỚC ObjectInputStream
    try (ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
         ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

      out = outStream;
      // ✅ REGISTER client vào ServerClientManager khi connect
      ServerClientManager.getInstance().registerClient(out);
      System.out.println("[ClientHandler] ✓ Client registered for broadcasting");

      while (true) {
        Object received = in.readObject();
        if (received == null) break;



        if (received instanceof LoginDTO loginDTO ){
          LoginHandler handler = new LoginHandler();
          handler.handle(loginDTO, out);
        }
        else if (received instanceof RegisterDTO registerDTO) {

          RegisterHandler handler = new RegisterHandler();
          handler.handle(registerDTO, out);
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
        }
        else {
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
      // ✅ UNREGISTER client khỏi ServerClientManager khi disconnect
      if (out != null) {
        ServerClientManager.getInstance().unregisterClient(out);
        System.out.println("[ClientHandler] ✓ Client unregistered from broadcasting");
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