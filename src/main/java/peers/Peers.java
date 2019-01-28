package peers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

public class Peers {
	ReadWriteLock lock;

	Peer[] Sorted;
	PubKeyPeers ByPubKey;
	IdPeers ById;
	List<Listener> Listeners;

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
		ByPubKey = new PubKeyPeers();
		ById = new IdPeers();
	}

	public static Peers NewPeersFromSlice(Peer[] source) {
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

		ByPubKey.put(peer.PubKeyHex, peer);
		ById.put(peer.ID, peer);
	}

	public void AddPeer(Peer peer) {
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
		Collection<Peer> res = ByPubKey.values();

		Peer[] array = res.toArray(new Peer[res.size()]);
		Arrays.sort(array, new PeerComparatorByID());

		Sorted = array;
	}

	/* Remove Methods */

	public void RemovePeer(Peer peer) {
		lock.writeLock().lock();
		try {
			if (ByPubKey.containsKey(peer.PubKeyHex)) {
				return;
			}
			ByPubKey.remove(peer.PubKeyHex);
			ById.remove(peer.ID);

			internalSort();
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void RemovePeerByPubKey(String pubKey) {
		RemovePeer(ByPubKey.get(pubKey));
	}

	public void RemovePeerById(long id) {
		RemovePeer(ById.get(id));
	}

	/* ToSlice Methods */

	public Peer[] ToPeerSlice() {
		return Sorted;
	}

	public String[] ToPubKeySlice() {
		lock.readLock().lock();
		try {
			String[] res = new String[Sorted.length];
			for (int i = 0; i< Sorted.length; ++i) {
				res[i] = Sorted[i].PubKeyHex;
			}

			return res;
		} finally {
			lock.readLock().unlock();
		}
	}

	public long[] ToIDSlice() {
		lock.readLock().lock();
		try {
			long[] res = new long[Sorted.length];
			for (int i = 0; i< Sorted.length; ++i) {
				res[i] = Sorted[i].ID;
			}
			return res;
		} finally {
			lock.readLock().unlock();
		}
	}

	/* EventListener */
	public void OnNewPeer(Listener cb) {
		Listeners.add(cb);
	}
	public void EmitNewPeer(Peer peer) {
		for (Listener l : Listeners) {
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
	public ExcludePeerResult ExcludePeer(Peer[] peers, String peer) {
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
	public int Len() {
		lock.readLock().lock();
		try {
			return ByPubKey.size();
		} finally {
			lock.readLock().unlock();
		}
	}

	public PubKeyPeers getByPubKey() {
		return ByPubKey;
	}

	public IdPeers getById() {
		return ById;
	}

	public Peer ById(long creatorID) {
		return ById.get(creatorID);
	}

	public Peer ByPubKey(String pub) {
		return ByPubKey.get(pub);
	}

	public ReadWriteLock getLock() {
		return lock;
	}

	public Peer[] getSorted() {
		return Sorted;
	}

	public List<Listener> getListeners() {
		return Listeners;
	}
}