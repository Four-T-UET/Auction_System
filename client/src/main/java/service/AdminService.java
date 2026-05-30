package service;

import auction.logic.RequestDTO.CancelAuctionDTO;
import auction.logic.RequestDTO.PullUserDTO;
import stateManager.UserManager;
import auction.logic.model.Auction;
import auction.logic.model.User;


import java.util.List;

public class AdminService {

    public static Object pullUsers() {
        try {
            ClientSocket clientSocket = ClientSocket.getInstance();
            clientSocket.send(new PullUserDTO());
            Object response = clientSocket.receive();
            if (response instanceof List<?> list) {
                @SuppressWarnings("unchecked")
                List<User> users = (List<User>) list;
                UserManager.getInstance().replaceAll(users);
            }
            return response;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

    /// /////////////////////////
    public static Object cancelAuctionService(Auction auction) {
        try {
            ClientSocket clientSocket = ClientSocket.getInstance();
            clientSocket.send(new CancelAuctionDTO(
                    auction.getItem().getName(),
                    auction.getId(),
                    auction.getCurrentWinner() != null ? auction.getCurrentWinner().getUsername() : null));
            Object response = clientSocket.receive();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /// ///////////////////////////////



}
