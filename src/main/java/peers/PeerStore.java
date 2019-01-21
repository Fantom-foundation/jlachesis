package peers;

import common.RetResult;
import common.error;

// PeerStore provides an interface for persistent storage and
// retrieval of peers.
public interface PeerStore {
	// Peers returns the list of known peers.
	RetResult<Peers> Peers();

	// SetPeers sets the list of known peers. This is invoked when a peer is
	// added or removed.
	error SetPeers(Peer[] peers);
}