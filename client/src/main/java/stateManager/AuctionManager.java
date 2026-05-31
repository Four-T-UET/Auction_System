package stateManager;

import auction.logic.model.Auction;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionManager {
	private static AuctionManager instance;
	private final ObservableList<Auction> masterAuctionList = FXCollections.observableArrayList();

	// listeners theo Observer pattern
	private final CopyOnWriteArrayList<AuctionUpdateListener> listeners = new CopyOnWriteArrayList<>();

	private AuctionManager() {}


	public static AuctionManager getInstance() {
		if (instance == null) instance = new AuctionManager();
		return instance;
	}

	public ObservableList<Auction> getMasterAuctionList() {
		return masterAuctionList;
	}

	// quản lý listener
	public void registerListener(AuctionUpdateListener l) {
		if (l != null) listeners.addIfAbsent(l);
	}
	public void unregisterListener(AuctionUpdateListener l) {
		listeners.remove(l);
	}

	// dispatch helpers chạy trên luồng của FX
	private void notifyAdded(Auction auction) {
		if (Platform.isFxApplicationThread()) {
			for (AuctionUpdateListener l : listeners) {
				try { l.onAuctionAdded(auction); } catch (Exception e) { e.printStackTrace(); }
			}
		} else {
			Platform.runLater(() -> notifyAdded(auction));
		}
	}

	private void notifyUpdated(Auction auction) {
		if (Platform.isFxApplicationThread()) {
			for (AuctionUpdateListener l : listeners) {
				try { l.onAuctionUpdated(auction); } catch (Exception e) { e.printStackTrace(); }
			}
		} else {
			Platform.runLater(() -> notifyUpdated(auction));
		}
	}

	private void notifyReplaced(List<Auction> auctions) {
		if (Platform.isFxApplicationThread()) {
			for (AuctionUpdateListener l : listeners) {
				try { l.onAuctionsReplaced(auctions); } catch (Exception e) { e.printStackTrace(); }
			}
		} else {
			Platform.runLater(() -> notifyReplaced(auctions));
		}
	}

	private void notifyRemoved(String auctionId) {
		if (Platform.isFxApplicationThread()) {
			for (AuctionUpdateListener l : listeners) {
				try { l.onAuctionRemoved(auctionId); } catch (Exception e) { e.printStackTrace(); }
			}
		} else {
			Platform.runLater(() -> notifyRemoved(auctionId));
		}
	}

	// thêm hoặc cập nhật phiên đấu giá
	public void addOrUpdate(Auction auction) {
		if (Platform.isFxApplicationThread()) {
			doAddOrUpdate(auction);
		} else {
			Platform.runLater(() -> doAddOrUpdate(auction));
		}
	}

	private void doAddOrUpdate(Auction auction) {
		Optional<Auction> existing = masterAuctionList.stream()
				.filter(a -> a.getId().equals(auction.getId()))
				.findFirst();
		if (existing.isPresent()) {
			int idx = masterAuctionList.indexOf(existing.get());
			masterAuctionList.set(idx, auction);
			notifyUpdated(auction);
		} else {
			masterAuctionList.add(0,auction);
			notifyAdded(auction);
		}
	}

	public void removeById(String auctionId) {
		if (Platform.isFxApplicationThread()) {
			doRemoveById(auctionId);
		} else {
			Platform.runLater(() -> doRemoveById(auctionId));
		}
	}

	private void doRemoveById(String auctionId) {
		boolean removed = masterAuctionList.removeIf(a -> a.getId().equals(auctionId));
		if (removed) notifyRemoved(auctionId);
	}

	public void replaceAll(List<Auction> auctions) {
		if (Platform.isFxApplicationThread()) {
			masterAuctionList.setAll(auctions);
			notifyReplaced(auctions);
		} else {
			Platform.runLater(() -> {
				masterAuctionList.setAll(auctions);
				notifyReplaced(auctions);
			});
		}
	}
}