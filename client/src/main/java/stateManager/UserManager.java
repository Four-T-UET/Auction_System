package stateManager;

import auction.logic.model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class UserManager {
    private static UserManager instance;
    private final ObservableList<User> masterUserList = FXCollections.observableArrayList();

    // listeners theo Observer pattern
    private final CopyOnWriteArrayList<UserUpdateListener> listeners = new CopyOnWriteArrayList<>();
    private UserManager() {}
    public static UserManager getInstance() {
        if (instance == null) { instance = new UserManager();}
        return instance;
    }

    public ObservableList<User> getMasterUserList() {return this.masterUserList;}

    // quản lý Listener
    public void registerListener(UserUpdateListener l) {
        if (l != null) listeners.addIfAbsent(l);
    }
    public void unregisterListener(UserUpdateListener l) {
        listeners.remove(l);
    }

    // Các hàm dispatch chạy trên luồng FX
    private void notifyAdded(User user) {
        if (Platform.isFxApplicationThread()) {
            for (UserUpdateListener l : listeners) {
                try { l.onUserAdded(user); } catch (Exception e) { e.printStackTrace(); }
            }
        } else {
            Platform.runLater(() -> notifyAdded(user));
        }
    }

    private void notifyUpdated(User user) {
        if (Platform.isFxApplicationThread()) {
            for (UserUpdateListener l : listeners) {
                try { l.onUserUpdated(user); } catch (Exception e) { e.printStackTrace(); }
            }
        } else {
            Platform.runLater(() -> notifyUpdated(user));
        }
    }

    private void notifyReplaced(List<User> users) {
        if (Platform.isFxApplicationThread()) {
            for (UserUpdateListener l : listeners) {
                try { l.onUserReplaced(users); } catch (Exception e) { e.printStackTrace(); }
            }
        } else {
            Platform.runLater(() -> notifyReplaced(users));
        }
    }

    private void notifyRemoved(String userId) {
        if (Platform.isFxApplicationThread()) {
            for (UserUpdateListener l : listeners) {
                try { l.onUserRemoved(userId); } catch (Exception e) { e.printStackTrace(); }
            }
        } else {
            Platform.runLater(() -> notifyRemoved(userId));
        }
    }
    // Thêm hoặc cập nhật user (an toàn gọi từ bất kỳ luồng nào)
    public void addOrUpdate(User user) {
        if (Platform.isFxApplicationThread()) {
            doAddOrUpdate(user);
        } else {
            Platform.runLater(() -> doAddOrUpdate(user));
        }
    }

    private void doAddOrUpdate(User user) {
        Optional<User> existing = masterUserList.stream()
                .filter(u -> u.getId().equals(user.getId()))
                .findFirst();
        if (existing.isPresent()) {
            int idx = masterUserList.indexOf(existing.get());
            masterUserList.set(idx, user);
            notifyUpdated(user);
        } else {
            masterUserList.add(user);
            notifyAdded(user);
        }
    }

    public void replaceAll(List<User> users) {
        if (Platform.isFxApplicationThread()) {
            masterUserList.setAll(users);
            notifyReplaced(users);
        } else {
            Platform.runLater(() -> {
                masterUserList.setAll(users);
                notifyReplaced(users);
            });
        }
    }
}
