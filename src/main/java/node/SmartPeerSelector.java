package node;

import java.util.Map;
import java.util.Random;

import common.RetResult;
import peers.Peer;

/**
 * Selection based on FlagTable of a randomly chosen undermined event
 */
public class SmartPeerSelector implements PeerSelector {
	peers.Peers peers;
	String localAddr;
	String last;

	FlagtableContainer flagTable;

	private Random rand;

	public SmartPeerSelector(peers.Peers participants,
		String localAddr, FlagtableContainer GetFlagTable) {

		this.localAddr = localAddr;
		this.peers =     participants;
		this.flagTable = GetFlagTable;

		this.rand = new Random();
		this.rand.setSeed(System.currentTimeMillis());
	}

	public peers.Peers peers() {
		return peers;
	}

	public void updateLast(String peer) {
		last = peer;
	}

	public peers.Peer next() {
		Peer[] selectablePeers = peers.ToPeerSlice();
		if (selectablePeers.length > 1) {
			selectablePeers = peers.excludePeer(selectablePeers, localAddr).peers;

			if (selectablePeers.length > 1) {
				selectablePeers = peers.excludePeer(selectablePeers, last).peers;
				if (selectablePeers.length > 1) {
					RetResult<Map<String, Long>> ftRes = flagTable.getFlagTable();

					if (ftRes.err == null) {
						Map<String, Long> ft = ftRes.result;
						if (ft != null) {
							for (String id : ft.keySet()) {
								long flag = ft.get(id);
								if (flag == 1 && selectablePeers.length > 1) {
									// TODO: check it
									selectablePeers = peers.excludePeer(selectablePeers, id).peers;
								}
							}
						}
					}
				}
			}
		}
		int i = rand.nextInt(selectablePeers.length);
		return selectablePeers[i];
	}
}