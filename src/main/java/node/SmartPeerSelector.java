package node;

import java.util.Map;
import java.util.Random;

import common.RetResult;
import peers.Peer;

//Selection based on FlagTable of a randomly chosen undermined event
public class SmartPeerSelector implements PeerSelector {
	peers.Peers peers;
	String localAddr;
	String last;

	FlagtableContainer GetFlagTable;

	private Random rand;

	public SmartPeerSelector(peers.Peers participants,
		String localAddr, FlagtableContainer GetFlagTable) {

		this.localAddr = localAddr;
		this.peers =     participants;
		this.GetFlagTable = GetFlagTable;

		this.rand = new Random();
		this.rand.setSeed(System.currentTimeMillis());
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
			selectablePeers = peers.ExcludePeer(selectablePeers, localAddr).peers;

			if (selectablePeers.length > 1) {
				selectablePeers = peers.ExcludePeer(selectablePeers, last).peers;
				if (selectablePeers.length > 1) {
					RetResult<Map<String, Long>> ftRes = GetFlagTable.GetFlagTable();

					if (ftRes.err == null) {
						Map<String, Long> ft = ftRes.result;
						for (String id : ft.keySet()) {
							long flag = ft.get(id);
							if (flag == 1 && selectablePeers.length > 1) {
								// TODO: check it
								selectablePeers = peers.ExcludePeer(selectablePeers, id).peers;
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