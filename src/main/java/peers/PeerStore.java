package peers;

import common.RResult;
import common.error;

/**
 * PeerStore provides an interface for persistent storage and
 * retrieval of peers.
 */
public interface PeerStore {
	/** Peers returns the list of known peers. */
	RResult<Peers> peers();

	/**
	 * SetPeers sets the list of known peers. This is invoked when a peer is
	 * added or removed.
	 * @param peers
	 */
	error setPeers(Peer[] peers);
}