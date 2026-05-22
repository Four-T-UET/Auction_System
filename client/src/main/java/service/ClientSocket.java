package service;

import auction.logic.RequestDTO.TimeSyncRequest;
import auction.logic.ResponseDTO.BroadcastMessage;
import auction.logic.ResponseDTO.ServerTimeSnapshot;
import auction.logic.manager.AuctionManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientSocket {
  private static ClientSocket instance;
  private Socket socket;
  private ObjectOutputStream out;
  private ObjectInputStream in;
  private volatile long serverTimeOffsetMillis;
  private volatile String serverZoneId = ZoneId.systemDefault().getId();

  //  Queue để lưu non-broadcast responses (login, register, auction, etc)
  private final LinkedBlockingQueue<Object> responseQueue = new LinkedBlockingQueue<>();

  //  Listener callback nếu có broadcast
  private BroadcastListener broadcastListener;

  // Singleton: Đảm bảo cả ứng dụng chỉ có 1 kết nối duy nhất
  private ClientSocket() {
    try {
//      socket = new Socket("10.11.217.159", 5000);
      socket = new Socket("localhost", 5000);
      out = new ObjectOutputStream(socket.getOutputStream()); // lấy dữ liệu output stream của socket localhost
      out.flush();
      in = new ObjectInputStream(socket.getInputStream()); // đọc dữ liệu sẽ được gửi lại từ sever: byte --> text

      //  START broadcast listener thread (non-blocking)
      startBroadcastListener();
      syncServerClock();
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
  public synchronized void send(Object data) {
    try {
      out.reset();
      out.writeObject(data);
      out.flush();
      out.reset();           // reset lần 2 như code cũ của bạn
    } catch (IOException e) {
      throw new RuntimeException("Send failed", e); // tốt hơn là throw thay vì return "ERROR"
    }
  }

  //  FIX: receive() sẽ lấy từ queue thay vì đọc trực tiếp
  public synchronized Object receive() {
    try {
      //  Wait for response from queue (put there by BroadcastListener)
      Object response = responseQueue.poll();
      if (response != null) {
        return response;
      }
      //  If queue is empty, wait timeout 30 seconds
      System.out.println("[ClientSocket] Waiting for response...");
      response = responseQueue.poll(30, java.util.concurrent.TimeUnit.SECONDS);
      if (response == null) {
        throw new RuntimeException("Response timeout - no answer from server");
      }
      return response;
    } catch (InterruptedException e) {
      throw new RuntimeException("Receive interrupted", e);
    }
  }

  public long getServerTimeMillis() {
    return System.currentTimeMillis() + serverTimeOffsetMillis;
  }

  public ZoneId getServerZoneId() {
    try {
      return serverZoneId == null || serverZoneId.isBlank() ? ZoneId.systemDefault() : ZoneId.of(serverZoneId);
    } catch (Exception e) {
      return ZoneId.systemDefault();
    }
  }

  public long toServerEpochMillis(LocalDateTime dateTime) {
    if (dateTime == null) {
      return -1L;
    }
    return dateTime.atZone(getServerZoneId()).toInstant().toEpochMilli();
  }

  //  Listener thread chạy background để lắng nghe từ server
  private void startBroadcastListener() {
    Thread listenerThread = new Thread(() -> {
      System.out.println("[ClientSocket] ✓ BroadcastListener started");
      while (true) {
        try {
          //  BroadcastListener là THE ONLY READER từ ObjectInputStream
          Object obj = in.readObject();

          if (obj instanceof BroadcastMessage msg) {
            //  Đây là broadcast event → handle ngay
            System.out.println("[ClientSocket] ✓ Received broadcast: " + msg);
            updateServerClock(msg);
            handleBroadcast(msg);
          } else {
            //  Đây là response từ request (login, register, auction, etc)
            // Put vào queue để receive() xử lý
            System.out.println("[ClientSocket] ✓ Received response from server: " + (obj != null ? obj.getClass().getSimpleName() : "null"));
            responseQueue.put(obj);
          }
        } catch (IOException e) {
          // Connection closed or error
          System.err.println("[ClientSocket] ✗ BroadcastListener stopped: " + e.getMessage());
          break;
        } catch (ClassNotFoundException e) {
          System.err.println("[ClientSocket] ✗ ClassNotFoundException: " + e.getMessage());
        } catch (InterruptedException e) {
          System.err.println("[ClientSocket] ✗ BroadcastListener interrupted");
          break;
        } catch (Exception e) {
          System.err.println("[ClientSocket] ✗ Error in broadcast listener: " + e.getMessage());
          break;
        }
      }
    }, "BroadcastListener");
    
    listenerThread.setDaemon(true);  // Thread sẽ kết thúc khi main app closes
    listenerThread.start();
  }

  /**
   * Xử lý broadcast message từ server
   */
  private void handleBroadcast(BroadcastMessage msg) {
    switch (msg.getEventType()) {
      case AUCTION_CREATED:
        System.out.println("[ClientSocket] AUCTION_CREATED broadcast received");
        if (msg.getAuction() != null) {
          //  Add/Update auction vào AuctionManager
          // AuctionManager.addOrUpdate() sẽ trigger listener → UI auto update
          AuctionManager.getInstance().addOrUpdate(msg.getAuction());
          System.out.println("[ClientSocket] ✓ Added auction to AuctionManager: " + msg.getAuction().getId());
        }
        break;

      case AUCTION_UPDATED:
        System.out.println("[ClientSocket] AUCTION_UPDATED broadcast received");
        if (msg.getAuction() != null) {
          AuctionManager.getInstance().addOrUpdate(msg.getAuction());
          System.out.println("[ClientSocket] ✓ Updated auction in AuctionManager");
        }
        break;

      case AUCTION_REMOVED:
        System.out.println("[ClientSocket] AUCTION_REMOVED broadcast received");
        if (msg.getAuctionId() != null) {
          AuctionManager.getInstance().removeById(msg.getAuctionId());
          System.out.println("[ClientSocket] ✓ Removed auction from AuctionManager");
        }
        break;

      case AUCTION_FINISHED:
        System.out.println("[ClientSocket] AUCTION_FINISHED broadcast received");
        if (msg.getAuction() != null) {
          // Update auction status to FINISHED in local cache
          AuctionManager.getInstance().addOrUpdate(msg.getAuction());
          System.out.println("[ClientSocket] ✓ Updated auction to FINISHED: " + msg.getAuction().getId());
        }
        break;

      case WALLET_UPDATED:
        System.out.println("[ClientSocket] WALLET_UPDATED broadcast received");
        if (msg.getData() instanceof auction.logic.ResponseDTO.WalletResponseDTO walletUpdate) {
          // Update current user's wallet if this client is the one
          auction.logic.model.Clients currentUser = controller.UserSession.getCurrentUser();
          if (currentUser != null) {
            currentUser.getWallet().setBalance(walletUpdate.getBalance());
            currentUser.getWallet().settotalLockBalance(walletUpdate.getLockedBalance());
            System.out.println("[ClientSocket] ✓ Updated wallet balance=" + walletUpdate.getBalance() + 
                               ", locked=" + walletUpdate.getLockedBalance());
          }
        }
        break;

      default:
        System.out.println("[ClientSocket] Unknown event type: " + msg.getEventType());
    }

    // Trigger custom callback nếu có
    if (broadcastListener != null) {
      broadcastListener.onBroadcastReceived(msg);
    }
  }

  /**
   * Callback interface để notify khi có broadcast
   */
  public interface BroadcastListener {
    void onBroadcastReceived(BroadcastMessage msg);
  }

  public void setBroadcastListener(BroadcastListener listener) {
    this.broadcastListener = listener;
  }

  private void syncServerClock() {
    try {
      send(new TimeSyncRequest());
      Object response = receive();
      if (response instanceof ServerTimeSnapshot snapshot) {
        serverZoneId = snapshot.getServerZoneId();
        serverTimeOffsetMillis = snapshot.getServerNowMillis() - System.currentTimeMillis();
        System.out.println("[ClientSocket] ✓ Initial server clock sync offset=" + serverTimeOffsetMillis + "ms");
      } else if (response instanceof Long serverNowMillis) {
        serverTimeOffsetMillis = serverNowMillis - System.currentTimeMillis();
        System.out.println("[ClientSocket] ✓ Initial server clock sync offset=" + serverTimeOffsetMillis + "ms");
      } else {
        System.err.println("[ClientSocket] ✗ Time sync response invalid: " + response);
      }
    } catch (Exception e) {
      System.err.println("[ClientSocket] ✗ Initial time sync failed: " + e.getMessage());
    }
  }

  private void updateServerClock(BroadcastMessage msg) {
    if (msg == null) {
      return;
    }
    if (msg.getServerZoneId() != null && !msg.getServerZoneId().isBlank()) {
      serverZoneId = msg.getServerZoneId();
    }
    if (msg.getServerNowMillis() > 0) {
      serverTimeOffsetMillis = msg.getServerNowMillis() - System.currentTimeMillis();
    }
  }

}

