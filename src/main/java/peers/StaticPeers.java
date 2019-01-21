package peers;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;

// StaticPeers is used to provide a static list of peers.
public class StaticPeers {
	Peer[] StaticPeers;
	Lock l; //sync.Mutex           

	// Peers implements the PeerStore interface.
	public synchronized Peer[] Peers() {
		Peer[] peers = Arrays.copyOf(StaticPeers, StaticPeers.length);
		return peers;
	}
	
	// SetPeers implements the PeerStore interface.
	public synchronized void SetPeers(Peer[] p)  {
		if (p == null) {
			StaticPeers = null;
		} else  {
			StaticPeers = Arrays.copyOf(p, p.length);
		}
	}
}
