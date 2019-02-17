package peers;

import java.util.Arrays;

/**
 * StaticPeers is used to provide a static list of peers.
 */
public class StaticPeers {
	Peer[] staticPeers = new Peer[0];

	/**
	 * Peers implements the PeerStore interface.
	 */
	public synchronized Peer[] peers() {
		Peer[] peers = Arrays.copyOf(staticPeers, staticPeers.length);
		return peers;
	}

	/**
	 * SetPeers implements the PeerStore interface.
	 */
	public synchronized void setPeers(Peer[] p)  {
		if (p == null) {
			staticPeers = null;
		} else  {
			staticPeers = Arrays.copyOf(p, p.length);
		}
	}
}
