package service;

import auction.logic.manager.AuctionManager;
import auction.logic.model.Auction;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;
/*
????????????????????????????????????????????????????????????????????????????????????????
 */
public class UpdateClient {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Thread readerThread;
    private volatile boolean running = false;

    public UpdateClient(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            startReader();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startReader() {
        running = true;
        readerThread = new Thread(() -> {
            try {
                while (running && !socket.isClosed()) {
                    Object msg = in.readObject();
                    if (msg == null) continue;

                    if (msg instanceof Auction) {
                        AuctionManager.getInstance().addOrUpdate((Auction) msg);
                    }
                     else {
                        // Unknown message type - you can extend handling here
                        System.out.println("UpdateClient received unknown message type: " + msg.getClass());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // optionally implement reconnect logic here
            }
        }, "UpdateClient-Reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    public void stop() {
        running = false;
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (Exception ignored) {}
    }

    public void subscribe(String topic) {
        try {
            // optional: send subscription request to server
            out.writeObject(topic);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

