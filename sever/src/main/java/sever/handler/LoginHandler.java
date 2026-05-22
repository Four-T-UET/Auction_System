package sever.handler;

import auction.logic.RequestDTO.LoginDTO;
import auction.logic.model.Clients;
import java.io.ObjectOutputStream;
import sever.dao.UserDAO;
import sever.manager.ServerClientManager;

public class LoginHandler {
    private final UserDAO userDAO = new UserDAO();

    public void handle(LoginDTO loginDTO, ObjectOutputStream out) {
        try {
            String username = loginDTO.getUsername();
            String password = loginDTO.getPassword();

            // Authenticate using existing DAO method
            Object userFromDB = userDAO.getUser(username); // Returns User or Clients object
            boolean valid = false;
            if (userFromDB instanceof Clients client) {
                valid = client.login(username, password);
                if (valid) {
                    // Register userId -> output stream for targeted messages (wallet updates)
                    ServerClientManager.getInstance().registerUserId(client.getId(), out);
                    // Also update client runtime cache
                    sever.manager.ClientRuntimeManager.getInstance().addOrUpdate(client);
                }
            } else if (userFromDB instanceof auction.logic.model.User user) {
                valid = user.login(username, password);
                // If we only have a generic User, we cannot get an ID; skip registration
            }

            Object response = valid ? userFromDB : "FAILED: Sai tài khoản hoặc mật khẩu";
            out.writeObject(response);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                out.writeObject("FAILED: Server error - " + e.getMessage());
                out.flush();
            } catch (Exception ignored) {}
        }
    }
}
