package stateManager;

import auction.logic.model.User;

import java.util.List;

public interface UserUpdateListener {
    void onUserAdded(User user);
    void onUserUpdated(User user);
    void onUserReplaced(List<User> users);
    void onUserRemoved(String userId);
}
