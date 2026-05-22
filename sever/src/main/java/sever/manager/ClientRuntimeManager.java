package sever.manager;

import auction.logic.model.Clients;
import java.util.concurrent.ConcurrentHashMap;
import sever.dao.UserDAO;

public class ClientRuntimeManager {
    private static ClientRuntimeManager instance;
    private final ConcurrentHashMap<String, Clients> clientCache = new ConcurrentHashMap<>();
    private final UserDAO userDAO = new UserDAO();

    private ClientRuntimeManager() {}

    public static ClientRuntimeManager getInstance() {
        if (instance == null) {
            instance = new ClientRuntimeManager();
        }
        return instance;
    }

    public Clients getOrLoad(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return null;
        }
        return clientCache.computeIfAbsent(clientId, id -> {
            try {
                return userDAO.getUserById(id);
            } catch (Exception e) {
                return null;
            }
        });
    }

    public void addOrUpdate(Clients client) {
        if (client == null || client.getId() == null) {
            return;
        }
        clientCache.put(client.getId(), client);
    }
}

