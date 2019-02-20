package poset;

import java.util.HashMap;
import java.util.Map;

import common.LRUCache;
import common.RResult;
import common.RResult3;
import common.RollingIndex;
import common.StoreErr;
import common.StoreErrType;
import common.error;
import peers.Peer;
import peers.Peers.Listener;

public class InmemStore implements Store {
	int cacheSize;
	peers.Peers participants;
	LRUCache<String,Event> eventCache;
	LRUCache<Long,RoundInfo> roundCache;
	LRUCache<Long,Block> blockCache;
	LRUCache<Long,Frame> frameCache;
	common.RollingIndex consensusCache;
	long totConsensusEvents;
	ParticipantEventsCache participantEventsCache;
	Map<String,Root> rootsByParticipant; //[participant] => Root
	Map<String,Root> rootsBySelfParent; //[Root.SelfParent.Hash] => Root
	long lastRound;
	Map<String,String> lastConsensusEvents; //[participant] => hex() of last consensus event
	long lastBlock;

	public InmemStore(peers.Peers participants, int cacheSize) {
		HashMap<String,Root> rootsByParticipant = new HashMap<String, Root>();

		for (String pk : participants.getByPubKey().keySet()){
			Peer pid = participants.getByPubKey().get(pk);
			Root root = new Root(pid.getID());
			rootsByParticipant.put(pk,  root);
		}

		RResult<LRUCache<String,Event>> eventCacheCre = LRUCache.New(cacheSize);
		LRUCache<String,Event> eventCache = eventCacheCre.result;
		error err = eventCacheCre.err;
		if (err != null) {
			System.err.println( String.format("Unable to init InmemStore.eventCache:", err));
			System.exit(31);
		}

		RResult<LRUCache<Long,RoundInfo>> roundCacheCre = LRUCache.New(cacheSize);
		LRUCache<Long,RoundInfo> roundCache = roundCacheCre.result;
		err = roundCacheCre.err;
		if (err != null) {
			System.err.println( String.format("Unable to init InmemStore.roundCache:", err));
			System.exit(32);
		}

		RResult<LRUCache<Long,Block>> blockCacheCre = LRUCache.New(cacheSize);
		LRUCache<Long,Block> blockCache = blockCacheCre.result;
		err = blockCacheCre.err;
		if (err != null) {
			System.err.println( String.format("Unable to init InmemStore.blockCache:", err));
			System.exit(33);
		}

		RResult<LRUCache<Long,Frame>> frameCacheCre = LRUCache.New(cacheSize);
		LRUCache<Long,Frame> frameCache = frameCacheCre.result;
		err = frameCacheCre.err;
		if (err != null) {
			System.err.println( String.format("Unable to init InmemStore.frameCache:", err));
			System.exit(34);
		}

		this.cacheSize=              cacheSize;
		this.participants=           participants;
		this.eventCache=             eventCache;
		this.roundCache=             roundCache;
		this.blockCache=             blockCache;
		this.frameCache=             frameCache;
		this.consensusCache=         new RollingIndex("ConsensusCache", cacheSize);
		this.participantEventsCache= new ParticipantEventsCache(cacheSize, participants);
		this.rootsByParticipant=     rootsByParticipant;
		this.lastRound=              -1;
		this.lastBlock=              -1;
		this.lastConsensusEvents=   new HashMap<String,String>();

		participants.onNewPeer(
			new Listener() {
				@Override
				public void listen(Peer peer) {
					Root root = new Root(peer.getID());
					rootsByParticipant.put(peer.getPubKeyHex(), root);
					rootsBySelfParent = null;
					rootsBySelfParent();
			 		ParticipantEventsCache old = participantEventsCache;
					participantEventsCache = new ParticipantEventsCache(cacheSize, participants);
					participantEventsCache.Import(old);
				}})
		;
	}

	public int cacheSize() {
		return cacheSize;
	}

	public RResult<peers.Peers> participants() {
		return new RResult<peers.Peers>(participants, null);
	}

	public RResult<Map<String,Root>> rootsBySelfParent() {
		if (rootsBySelfParent == null) {
			rootsBySelfParent = new HashMap<String,Root>();
			for (Root root : rootsByParticipant.values()) {
				rootsBySelfParent.put(root.SelfParent.Hash, root);
			}
		}
		return new RResult<Map<String,Root>>(rootsBySelfParent, null);
	}

	public RResult<Event> getEvent(String key) {
		Event res = eventCache.get(key);
		if (res == null) {
			return new RResult<Event>(new Event(), StoreErr.newStoreErr("EventCache", StoreErrType.KeyNotFound, key));
		}

		return new RResult<Event>( res, null);
	}

	public error setEvent(Event event) {
		String key = event.hex();
		error err = getEvent(key).err;
		if (err != null && !StoreErr.Is(err, StoreErrType.KeyNotFound)) {
			return err;
		}
		if (StoreErr.Is(err, StoreErrType.KeyNotFound)) {
			err = addParticpantEvent(event.creator(), key, event.index());
			if (err != null) {
				return err;
			}
		}

		// fmt.Println("Adding event to cache", event.Hex())
		eventCache.put(key, event);

		return null;
	}

	public error addParticpantEvent(String participant, String hash, long index) {
		return participantEventsCache.Set(participant, hash, index);
	}

	public RResult<String[]> participantEvents(String participant, long skip) {
		return participantEventsCache.Get(participant, skip);
	}

	public RResult<String> participantEvent(String participant, long index) {
		RResult<String> getItem = participantEventsCache.GetItem(participant, index);
		String ev = getItem.result;
		error err = getItem.err;
		if (err != null) {
			Root root = rootsByParticipant.get(participant);
			if (root == null) {
				return new RResult<String>("", StoreErr.newStoreErr("InmemStore.Roots", StoreErrType.NoRoot, participant));
			}
			if (root.SelfParent.Index == index) {
				ev = root.SelfParent.Hash;
				err = null;
			}
		}
		return new RResult<String>(ev, err);
	}

	public RResult3<String,Boolean> lastEventFrom(String participant ) {
		//try to get the last event from this participant
		RResult<String> getLast = participantEventsCache.GetLast(participant);
		String last = getLast.result;
		error err = getLast.err;
		boolean isRoot = false;
		//if there is none, grab the root
		if (err != null && StoreErr.Is(err, StoreErrType.Empty)) {
			Root root = rootsByParticipant.get(participant);
			if (root != null) {
				last = root.SelfParent.Hash;
				isRoot = true;
				err = null;
			} else {
				err = StoreErr.newStoreErr("InmemStore.Roots", StoreErrType.NoRoot, participant);
			}
		}
		return new RResult3<String,Boolean>(last, isRoot, err);
	}

	public RResult3<String,Boolean> lastConsensusEventFrom(String participant){
		//try to get the last consensus event from this participant
		String last = lastConsensusEvents.get(participant);
		boolean isRoot = false;
		error err = null;
		//if there is none, grab the root
		if (last == null) {
			Root root = rootsByParticipant.get(participant);
			if (root != null) {
				last = root.SelfParent.Hash;
				isRoot = true;
			} else {
				err = StoreErr.newStoreErr("InmemStore.Roots", StoreErrType.NoRoot, participant);
			}
		}
		return new RResult3<String,Boolean>(last, isRoot, err);
	}

	public Map<Long,Long> knownEvents() {
		Map<Long, Long> known = participantEventsCache.Known();
		for (String p : participants.getByPubKey().keySet()) {
			Peer pid = participants.getByPubKey().get(p);
			if (known.get(pid.getID()) == null) {
				Root root = rootsByParticipant.get(p);
				if (root != null) {
					known.put(pid.getID(), root.SelfParent.Index);
				}
			}
		}
		return known;
	}

	public String[] consensusEvents() {
		Object[] lastWindow = consensusCache.getLastWindow().result1;
		String[] res = new String[lastWindow.length];
		for (int i =0 ; i< lastWindow.length; ++i) {
			res[i] = lastWindow[i].toString();
		}
		return res;
	}

	public long consensusEventsCount() {
		return totConsensusEvents;
	}

	public error addConsensusEvent(Event event ) {
		consensusCache.set(event.hex(), totConsensusEvents);
		totConsensusEvents++;
		lastConsensusEvents.put(event.creator(), event.hex());
		return null;
	}

	public RResult<RoundInfo> getRound(long r) {
		RoundInfo res = roundCache.get(r);
		if (res == null) {
			return new RResult<RoundInfo>(new RoundInfo(), StoreErr.newStoreErr("RoundCache", StoreErrType.KeyNotFound, Long.toString(r, 10)));
		}
		return new RResult<RoundInfo>(res, null);
	}

	public error setRound(long r , RoundInfo round )  {
		roundCache.put(r, round);
		if (r > lastRound) {
			lastRound = r;
		}
		return null;
	}

	public long lastRound() {
		return lastRound;
	}

	public String[] roundWitnesses(long r) {
		RResult<RoundInfo> getRound = getRound(r);
		RoundInfo round = getRound.result;
		error err = getRound.err;
		if (err != null) {
			return new String[] {};
		}
		return round.Witnesses();
	}

	public int roundEvents(long r) {
		RResult<RoundInfo> getRound = getRound(r);
		RoundInfo round = getRound.result;
		error err = getRound.err;
		if (err != null) {
			return 0;
		}
		return round.Message.Events.size();
	}

	public RResult<Root> getRoot(String participant)  {
		Root res = rootsByParticipant.get(participant);
		if (res == null) {
			return new RResult<Root>(null, StoreErr.newStoreErr("RootCache", StoreErrType.KeyNotFound, participant));
		}
		return new RResult<Root>(res, null);
	}

	public RResult<Block> getBlock(long index) {
		Block res = blockCache.get(index);
		if ( res == null) {
			return new RResult<Block>( null, StoreErr.newStoreErr("BlockCache", StoreErrType.KeyNotFound, Long.toString(index, 10)));
		}
		return new RResult<Block>( res, null);
	}

	public error setBlock(Block block) {
		long index = block.Index();
		RResult<Block> getBlock = getBlock(index);
		error err = getBlock.err;
		if (err != null && !StoreErr.Is(err, StoreErrType.KeyNotFound)) {
			return err;
		}
		blockCache.put(index, block);
		if (index > lastBlock) {
			lastBlock = index;
		}
		return null;
	}

	public long lastBlockIndex()  {
		return lastBlock;
	}

	public RResult<Frame> getFrame(long index) {
		Frame res = frameCache.get(index);
		if (res == null) {
			return new RResult<Frame>( new Frame(), StoreErr.newStoreErr("FrameCache", StoreErrType.KeyNotFound, Long.toString(index, 10)));
		}
		return new RResult<Frame>(res , null);
	}

	public error setFrame(Frame frame)  {
		long index = frame.Round;
		RResult<Frame> getFrame = getFrame(index);
		error err = getFrame.err;
		if (err != null && !StoreErr.Is(err, StoreErrType.KeyNotFound)) {
			return err;
		}
		frameCache.put(index, frame);
		return null;
	}

	public error reset(Map<String,Root> roots)  {
		RResult<LRUCache<String,Event>> newCache = LRUCache.New(cacheSize);
		LRUCache<String,Event> eventCache = newCache.result;
		error err = newCache.err;
		if (err != null) {
			System.err.println( String.format("Unable to reset InmemStore.eventCache:", err));
			System.exit(41);
		}

		RResult<LRUCache<Long,RoundInfo>> newCache1 = LRUCache.New(cacheSize);
		LRUCache<Long,RoundInfo> roundCache = newCache1.result;
		err = newCache1.err;
		if (err != null) {
			System.err.println( String.format("Unable to reset InmemStore.roundCache:", err));
			System.exit(42);
		}
		// FIXIT: Should we recreate blockCache, frameCache and participantEventsCache here as well
		//        and reset lastConsensusEvents ?
		this.rootsByParticipant = roots;
		this.rootsBySelfParent = null;
		this.eventCache = eventCache;
		this.roundCache = roundCache;
		this.consensusCache = new RollingIndex("ConsensusCache", cacheSize);
		err = participantEventsCache.Reset();
		this.lastRound = -1;
		this.lastBlock = -1;

		rootsBySelfParent();
		return err;
	}

	public error close()  {
		return null;
	}

	public boolean needBoostrap() {
		return false;
	}

	public String storePath() {
		return "";
	}

	// This is just a stub
	public RResult<Event[]> topologicalEvents() {
		return new RResult<Event[]>(new Event[] {}, null);
	}
}