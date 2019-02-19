package node;

import autils.Utils;
import peers.Peer;

public class RandomPeerSelector implements PeerSelector {
	peers.Peers peers;
	String localAddr;
	String last;

	public RandomPeerSelector(peers.Peers participants, String localAddr) {
		this.localAddr = localAddr;
		this.peers = participants;
	}

	public peers.Peers peers() {
		return peers;
	}

	public void updateLast(String peer) {
		last = peer;
	}

	public peers.Peer next() {
		Peer[] selectablePeers = peers.toPeerSlice();

		if (selectablePeers.length > 1) {
			selectablePeers = peers.excludePeer(selectablePeers, localAddr).peers;
			if (selectablePeers.length > 1) {
				selectablePeers = peers.excludePeer(selectablePeers, last).peers;
			}
		}

		int i = Utils.random().nextInt(selectablePeers.length);
		return selectablePeers[i];
	}
}