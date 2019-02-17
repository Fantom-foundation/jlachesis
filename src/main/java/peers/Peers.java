package peers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Peers {
	ReadWriteLock lock = new ReentrantReadWriteLock();

	Peer[] sorted;
	PubKeyPeers byPubKey;
	IdPeers byId;
	List<Listener> listeners;

	public class PubKeyPeers extends HashMap<String,Peer> {
		private static final long serialVersionUID = 9150920875342183234L;
	}

	public class IdPeers extends HashMap<Long,Peer> {
		private static final long serialVersionUID = 532584913722142262L;
	}

	public interface Listener {
		public void listen(Peer peer);
	}

	/* Constructors */
	public Peers() {
		sorted = new Peer[0];
		byPubKey = new PubKeyPeers();
		byId = new IdPeers();
		listeners = new ArrayList<>();
	}

	public static Peers newPeersFromSlice(Peer[] source) {
		Peers peers = new Peers();

		for (Peer peer : source) {
			peers.addPeerRaw(peer);
		}

		peers.internalSort();

		return peers;
	}

	/* Add Methods */

	// Add a peer without sorting the set.
	// Useful for adding a bunch of peers at the same time
	// This method is private and is not protected by mutex.
	// Handle with care
	public void addPeerRaw(Peer peer) {
		if (peer.ID == 0) {
			peer.computeID();
		}

		byPubKey.put(peer.PubKeyHex, peer);
		byId.put(peer.ID, peer);
	}

	public void addPeer(Peer peer) {
		lock.writeLock().lock();
		try {
			addPeerRaw(peer);
			internalSort();
		} finally {
			lock.writeLock().unlock();
		}
	 	EmitNewPeer(peer);
	}

	public void internalSort() {
		Collection<Peer> res = byPubKey.values();

		Peer[] array = res.toArray(new Peer[res.size()]);
		Arrays.sort(array, new PeerComparatorByID());

		sorted = array;
	}

	/* Remove Methods */

	public void removePeer(Peer peer) {
		lock.writeLock().lock();
		try {
			if (byPubKey.containsKey(peer.PubKeyHex)) {
				return;
			}
			byPubKey.remove(peer.PubKeyHex);
			byId.remove(peer.ID);

			internalSort();
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void removePeerByPubKey(String pubKey) {
		removePeer(byPubKey.get(pubKey));
	}

	public void removePeerById(long id) {
		removePeer(byId.get(id));
	}

	/* ToSlice Methods */

	public Peer[] ToPeerSlice() {
		return sorted;
	}

	public String[] toPubKeySlice() {
		lock.readLock().lock();
		try {
			String[] res = new String[sorted.length];
			for (int i = 0; i< sorted.length; ++i) {
				res[i] = sorted[i].PubKeyHex;
			}

			return res;
		} finally {
			lock.readLock().unlock();
		}
	}

	public long[] toIDSlice() {
		lock.readLock().lock();
		try {
			long[] res = new long[sorted.length];
			for (int i = 0; i< sorted.length; ++i) {
				res[i] = sorted[i].ID;
			}
			return res;
		} finally {
			lock.readLock().unlock();
		}
	}

	/* EventListener */
	public void onNewPeer(Listener cb) {
		listeners.add(cb);
	}
	public void EmitNewPeer(Peer peer) {
		for (Listener l : listeners) {
			l.listen(peer);
		}
	}


	public class ExcludePeerResult {
		public ExcludePeerResult(int index, Peer[] peers) {
			this.pIndex = index;
			this.peers = peers;
		}
		public int pIndex;
		public Peer[] peers;
	}

	// ExcludePeer is used to exclude a single peer from a list of peers.
	public ExcludePeerResult excludePeer(Peer[] peers, String peer) {
		int index = -1;
		List<Peer> otherPeers = new ArrayList<Peer>();
		for (int i = 0; i < peers.length; ++i) {
			Peer p = peers[i];

			if (p.NetAddr != peer && p.PubKeyHex != peer) {
				otherPeers.add(p);
			} else {
				index = i;
			}
		}
		return new ExcludePeerResult(index, otherPeers.toArray(new Peer[otherPeers.size()]));
	}


	/* Utilities */
	public int length() {
		lock.readLock().lock();
		try {
			return byPubKey.size();
		} finally {
			lock.readLock().unlock();
		}
	}

	public PubKeyPeers getByPubKey() {
		return byPubKey;
	}

	public IdPeers getById() {
		return byId;
	}

	public Peer byId(long creatorID) {
		return byId.get(creatorID);
	}

	public Peer byPubKey(String pub) {
		return byPubKey.get(pub);
	}

	public Peer[] getSorted() {
		return sorted;
	}

	public List<Listener> getListeners() {
		return listeners;
	}
}