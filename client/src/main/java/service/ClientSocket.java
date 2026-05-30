package service;

import auction.logic.RequestDTO.TimeSyncRequest;
import auction.logic.ResponseDTO.BroadcastMessage;
import auction.logic.ResponseDTO.ServerTimeSnapshot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSocket {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientSocket.class);

  private static ClientSocket instance;
  private Socket socket;
  private ObjectOutputStream out;
  private ObjectInputStream in;

  private final LinkedBlockingQueue<Object> responseQueue = new LinkedBlockingQueue<>();
  private BroadcastListener broadcastListener;

  private ClientSocket() {
    try {
      socket = new Socket("localhost", 5000);
      out = new ObjectOutputStream(socket.getOutputStream());
      out.flush();
      in = new ObjectInputStream(socket.getInputStream());

      startBroadcastListener();
      syncInitialServerClock();
    } catch (IOException e) {
      LOGGER.error("Không thể kết nối tới Server! " + e.getMessage(), e);
    }
  }

  public static synchronized ClientSocket getInstance() {
    if (instance == null) instance = new ClientSocket();
    return instance;
  }

  public synchronized void send(Object data) {
    try {
      out.reset();
      out.writeObject(data);
      out.flush();
      out.reset();
    } catch (IOException e) {
      throw new RuntimeException("Send failed", e);
    }
  }

  public Object receive() {
    try {
      Object response = responseQueue.poll();
      if (response != null) return response;
      return responseQueue.poll(30, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException("Receive interrupted", e);
    }
  }

  private void syncInitialServerClock() {
    try {
      send(new TimeSyncRequest());
      Object response = receive();
      if (response instanceof ServerTimeSnapshot snapshot) {
        TimeSyncService.getInstance().updateClock(snapshot.getServerNowMillis(), snapshot.getServerZoneId());
      }
    } catch (Exception e) {
      LOGGER.warn("[ClientSocket]: Không thể đồng bộ thời gian: " + e.getMessage(), e);
    }
  }

  private void startBroadcastListener() {
    Thread listenerThread = new Thread(() -> {
      while (!socket.isClosed()) {
        try {
          Object obj = in.readObject();
          if (obj instanceof BroadcastMessage msg) {
            // đưa cho broadcast dispatcher xử lý
            BroadcastDispatcher.process(msg);
            if (broadcastListener != null) {
              broadcastListener.onBroadcastReceived(msg);
            }
          } else {
            responseQueue.put(obj);
          }
        } catch (Exception e) {
          LOGGER.warn("[ClientSocket] ✗ Listener error: " + e.getMessage(), e);
          break;
        }
      }
    }, "BroadcastListener");
    listenerThread.setDaemon(true);
    listenerThread.start();
  }

  public interface BroadcastListener {
    void onBroadcastReceived(BroadcastMessage msg);
  }

  public void setBroadcastListener(BroadcastListener listener) {
    this.broadcastListener = listener;
  }
}