package node;

import autils.Utils;
import peers.Peer;

public class RandomPeerSelector {
	peers.Peers peers;
	String localAddr;
	String last;

	public RandomPeerSelector(peers.Peers participants, String localAddr) {
		this.localAddr = localAddr;
		this.peers = participants;
	}
	
	public peers.Peers Peers() {
		return peers;
	}
	
	public void UpdateLast(String peer) {
		last = peer;
	}
	
	public peers.Peer Next() {
		Peer[] selectablePeers = peers.ToPeerSlice();
	
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