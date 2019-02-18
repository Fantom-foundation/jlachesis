package node;

/**
 * PeerSelector provides an interface for the lachesis node to
 * update the last peer it gossiped with and select the next peer
 * to gossip with
 */
public interface PeerSelector {
	peers.Peers peers();
	void updateLast(String peer);
	peers.Peer next();
}