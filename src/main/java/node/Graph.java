package node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import common.RResult;
import common.error;
import peers.Peer;
import poset.Block;
import poset.Event;
import poset.Root;
import poset.RoundInfo;
import poset.Store;

public class Graph {
	Node Node;

	public static class Infos {
		Map<String,Map<String,poset.Event>> ParticipantEvents;
		poset.RoundInfo[] Rounds;
		poset.Block[] Blocks;
		public Infos(Map<String, Map<String, Event>> participantEvents, RoundInfo[] rounds, Block[] blocks) {
			super();
			ParticipantEvents = participantEvents;
			Rounds = rounds;
			Blocks = blocks;
		}
	}


	public Graph(node.Node n) {
		this.Node = n;
	}

	public poset.Block[] GetBlocks() {
		poset.Block[] res;
		ArrayList<Block> blockList = new ArrayList<Block>();

		Store store = Node.core.poset.Store;

	 	for (long blockIdx = 0; blockIdx <= store.lastBlockIndex(); blockIdx++) {
	 		RResult<Block> getBlock = store.getBlock(blockIdx);
			Block r = getBlock.result;
			error err = getBlock.err;
	 		if (err != null) {
				break;
			}
	 		blockList.add(r);
		}

	 	res = blockList.toArray(new poset.Block[blockList.size()]);
	 	return res;
	}

	public Map<String,Map<String,Event>> GetParticipantEvents() {
		HashMap<String, Map<String, Event>> res = new HashMap<String,Map<String,Event>>();

		Store store = Node.core.poset.Store;
		peers.Peers peers = Node.core.poset.Participants;

		for (Peer p : peers.getByPubKey().values()) {
			RResult<Root> getRootCall = store.getRoot(p.getPubKeyHex());
			Root root = getRootCall.result;
			error err = getRootCall.err;

			if (err != null) {
				error.panic(err);
			}
			RResult<String[]> pEventsCall = store.participantEvents(p.getPubKeyHex(), root.GetSelfParent().GetIndex());
			String[] evs = pEventsCall.result;
			err = pEventsCall.err;
			if (err != null) {
				error.panic(err);
			}

			res.put(p.getPubKeyHex(), new HashMap<String,poset.Event>());

			String selfParent = String.format("Root%d", p.getID());

			HashMap<String, Long> flagTable = new HashMap<String,Long>();
			flagTable.put(selfParent, (long) 1);

			// Create and save the first Event
			Event initialEvent = new poset.Event(new byte[][]{},
				new poset.InternalTransaction[]{},
				new poset.BlockSignature[]{},
				new String[]{}, new byte[]{}, 0, flagTable);

			res.get(p.getPubKeyHex()).put(root.GetSelfParent().GetHash(), initialEvent);

			for (String e : evs) {
				 RResult<Event> getEvent = store.getEvent(e);
				Event event = getEvent.result;
				err = getEvent.err;

				if (err != null) {
					error.panic(err);
				}

				String hash = event.hex();
				res.get(p.getPubKeyHex()).put(hash, event);
			}
		}

		return res;
	}

	public RoundInfo[] GetRounds() {
		ArrayList<RoundInfo> roundList = new ArrayList<RoundInfo>();
		Store store = Node.core.poset.Store;

		for (long round = 0; round <= store.lastRound(); round++){
			RResult<RoundInfo> getRound = store.getRound(round);
			RoundInfo r = getRound.result;
			error err = getRound.err;

			if (err != null || !r.IsQueued()) {
				break;
			}
			roundList.add(r);
		}

		RoundInfo[] res = roundList.toArray(new RoundInfo[roundList.size()]);
		return res;
	}

	public Infos GetInfos()  {
		return new Infos( GetParticipantEvents(), GetRounds(), GetBlocks());
	}

	public Graph NewGraph(Node n) {
		return new Graph(n);
	}
}