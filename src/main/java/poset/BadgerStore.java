package poset;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import autils.Appender;
import autils.FileUtils;
import autils.Logger;
import common.RResult;
import common.RResult3;
import common.StoreErr;
import common.StoreErrType;
import common.error;
import peers.Peer;
import peers.Peers;

/**
 * BadgerStore
 *
 */
public class BadgerStore implements Store {
	public static final String participantPrefix = "participant";
	public static final String rootSuffix        = "root";
	public static final String roundPrefix       = "round";
	public static final String topoPrefix        = "topo";
	public static final String blockPrefix       = "block";
	public static final String framePrefix       = "frame";

	private static final Logger logger = Logger.getLogger(BadgerStore.class);

	peers.Peers participants;
	InmemStore inmemStore;
	DB db;
	String path;
	boolean needBoostrap;

	public BadgerStore() {
		super();
		this.participants = null;
		this.inmemStore = null;
		this.db = null;
		this.path = null;
		needBoostrap = false;
		initDBMaps();
	}

	public BadgerStore(Peers participants, InmemStore inmemStore, DB db, String path) {
		super();
		this.participants = participants;
		this.inmemStore = inmemStore;
		this.db = db;
		this.path = path;
		needBoostrap = false;
		initDBMaps();
	}

	public BadgerStore(DB db, String path, boolean needBoostrap) {
		super();
		this.participants = null;
		this.inmemStore = null;
		this.db = db;
		this.path = path;
		this.needBoostrap = needBoostrap;
		initDBMaps();
	}

	/**
	 * NewBadgerStore creates a brand new Store with a new database
	 * @param participants
	 * @param cacheSize
	 * @param path
	 * @return
	 */
	public static RResult<BadgerStore> NewBadgerStore(peers.Peers participants, int cacheSize, String path) {
		InmemStore inmemStore = new InmemStore(participants, cacheSize);
	    DB db = DBMaker.fileDB(new File(path))
	            .transactionEnable().closeOnJvmShutdown().fileChannelEnable()
	            .make();
		BadgerStore store = new BadgerStore(participants, inmemStore, db, path);

		error err = store.dbSetParticipants(participants);
		if  (err != null) {
			return new RResult<BadgerStore>(null, err);
		}

		err = store.dbSetRoots(inmemStore.rootsByParticipant);
		if (err != null) {
			return new RResult<BadgerStore>(null, err);
		}
		return new RResult<>(store, null);
	}

	/**
	 * LoadBadgerStore creates a Store from an existing database
	 * @param cacheSize
	 * @param path
	 * @return
	 */
	public static RResult<BadgerStore> LoadBadgerStore(int cacheSize, String path) {
		error err = null;
		if (! FileUtils.fileExist(path)) {
			return new RResult<BadgerStore>(null, error.Errorf("file path not exist"));
		}

		DB db = DBMaker.fileDB(new File(path))
	            .transactionEnable().closeOnJvmShutdown().fileChannelEnable()
	            .make();
		BadgerStore store = new BadgerStore(db, path, true);

		RResult<peers.Peers> dbGetParticipantsCall = store.dbGetParticipants();
		peers.Peers participants = dbGetParticipantsCall.result;
		err = dbGetParticipantsCall.err;
		if (err != null) {
			return new RResult<BadgerStore>(null, err);
		}

		InmemStore inmemStore = new InmemStore(participants, cacheSize);

		//read roots from db and put them in InmemStore
		Map<String, Root> roots = new HashMap<String,Root>();
		for (String p : participants.getByPubKey().keySet()) {
			RResult<Root> dbGetRoot = store.dbGetRoot(p);
			Root root = dbGetRoot.result;
			err = dbGetRoot.err;
			if (err != null) {
				return new RResult<BadgerStore>(null, err);
			}
			roots.put(p, root);
		}
		err = inmemStore.reset(roots);
		if (err != null) {
			return new RResult<BadgerStore>(null, err);
		}

		store.participants = participants;
		store.inmemStore = inmemStore;

		return new RResult<BadgerStore>(store, null);
	}

	public static RResult<BadgerStore> LoadOrCreateBadgerStore(peers.Peers participants, int cacheSize, String path) {
		RResult<BadgerStore> loadBadgerStore = LoadBadgerStore(cacheSize, path);

		BadgerStore store = loadBadgerStore.result;
		error err = loadBadgerStore.err;

		if (err != null) {
			System.err.println("Could not load store - creating new");
			RResult<BadgerStore> newBadgerStore = NewBadgerStore(participants, cacheSize, path);
			store = newBadgerStore.result;
			err = newBadgerStore.err;

			if (err != null) {
				return new RResult<BadgerStore>(null, err);
			}
		}

		return new RResult<BadgerStore>(store, null);
	}

	//==============================================================================
	//Keys

	public byte[] topologicalEventKey(long index) {
		return String.format("%s_%09d", topoPrefix, index).getBytes();
	}

	public byte[] participantKey(String participant) {
		return String.format("%s_%s", participantPrefix, participant).getBytes();
	}

	public byte[] participantEventKey(String participant, long index) {
		return String.format("%s__event_%09d", participant, index).getBytes();
	}

	public byte[] participantRootKey(String participant) {
		return String.format("%s_%s", participant, rootSuffix).getBytes();
	}

	public byte[] roundKey(long index) {
		return String.format("%s_%09d", roundPrefix, index).getBytes();
	}

	public byte[] blockKey(long index) {
		return String.format("%s_%09d", blockPrefix, index).getBytes();
	}

	public byte[] frameKey(long index) {
		return String.format("%s_%09d", framePrefix, index).getBytes();
	}

	//==============================================================================
	//Implement the Store interface

	public int cacheSize() {
		return inmemStore.cacheSize();
	}

	public RResult<peers.Peers> participants() {
		return new RResult<peers.Peers>(participants, null);
	}

	public RResult<Map<String,Root>> rootsBySelfParent() {
		return inmemStore.rootsBySelfParent();
	}

	public RResult<Event> getEvent(String key) {
		//try to get it from cache
		RResult<Event> getEvent = inmemStore.getEvent(key);
		Event event = getEvent.result;
		error err = getEvent.err;
		//if not in cache, try to get it from db
		if (err != null) {
			RResult<Event> dbGetEventCall = dbGetEvent(key);
			event = dbGetEventCall.result;
			err = dbGetEventCall.err;
		}
		return new RResult<Event>(event, mapError(err, "Event", key));
	}

	public error setEvent( Event event) {
		//try to add it to the cache
		error err = inmemStore.setEvent(event);
		if (err != null) {
			return err;
		}
		//try to add it to the db
		return dbSetEvents(new Event[]{event});
	}

	public RResult<String[]> participantEvents(String participant, long skip) {
		RResult<String[]> participantEventsCall = inmemStore.participantEvents(participant, skip);
		String[] res = participantEventsCall.result;
		error err = participantEventsCall.err;
		if (err != null) {
			RResult<String[]> dbParticipantEventsCall = dbParticipantEvents(participant, skip);
			res = dbParticipantEventsCall.result;
			err = dbParticipantEventsCall.err;
		}
		return new RResult<String[]>(res, err);
	}

	public RResult<String> participantEvent(String participant, long index) {
		RResult<String> participantEventCall = inmemStore.participantEvent(participant, index);
		String result = participantEventCall.result;
		error err = participantEventCall.err;
		if (err != null) {
			RResult<String> dbParticipantEventCall = dbParticipantEvent(participant, index);
			result = dbParticipantEventCall.result;
			err = dbParticipantEventCall.err;
		}
		return new RResult<String>(result, mapError(err, "ParticipantEvent", Arrays.toString(participantEventKey(participant, index))));
	}

	public RResult3<String,Boolean> lastEventFrom(String participant ) {
		return inmemStore.lastEventFrom(participant);
	}

	public RResult3<String,Boolean> lastConsensusEventFrom(String participant ) {
		return inmemStore.lastConsensusEventFrom(participant);
	}

	public Map<Long,Long> knownEvents() {
		Map<Long,Long> known = new HashMap<Long,Long>();
		for (String p : participants.getByPubKey().keySet()) {
			Peer pid = participants.getByPubKey().get(p);
			long index = (long) -1;
			RResult3<String, Boolean> lastEventFromCall = lastEventFrom(p);
			String last = lastEventFromCall.result1;
			Boolean isRoot = lastEventFromCall.result2;
			error err = lastEventFromCall.err;
			if (err == null) {
				if (isRoot) {
					RResult<Root> getRoot = getRoot(p);
					Root root = getRoot.result;
					err = getRoot.err;
					if (err != null) {
						last = root.SelfParent.Hash;
						index = root.SelfParent.Index;
					}
				} else {
					RResult<Event> getEventCall = getEvent(last);
					Event lastEvent = getEventCall.result;
					err = getEventCall.err;
					if (err == null) {
						index = lastEvent.index();
					}
				}

			}
			known.put(pid.getID(), index);
		}
		return known;
	}

	public String[] consensusEvents() {
		return inmemStore.consensusEvents();
	}

	public long consensusEventsCount() {
		return inmemStore.consensusEventsCount();
	}

	public error addConsensusEvent(Event event ) {
		return inmemStore.addConsensusEvent(event);
	}

	public RResult<RoundInfo> getRound(long r) {
		RResult<RoundInfo> getRound = inmemStore.getRound(r);
		RoundInfo res = getRound.result;
		error err = getRound.err;
		if (err != null) {
			RResult<RoundInfo> dbGetRoundCall = dbGetRound(r);
			res = dbGetRoundCall.result;
			err = dbGetRoundCall.err;
		}
		return new RResult<RoundInfo>(res, mapError(err, "Round", Arrays.toString(roundKey(r))));
	}

	public error setRound(long r , RoundInfo round ) {
		error err = inmemStore.setRound(r, round);
		if (err != null) {
			return err;
		}
		return dbSetRound(r, round);
	}

	public long lastRound() {
		return inmemStore.lastRound();
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

	public RResult<Root> getRoot(String participant ) {
		RResult<Root> getRoot = inmemStore.getRoot(participant);
		Root root = getRoot.result;
		error err = getRoot.err;
		if (err != null) {
			RResult<Root> dbGetRoot = dbGetRoot(participant);
			root = dbGetRoot.result;
			err = dbGetRoot.err;
		}
		return new  RResult<Root>(root, mapError(err, "Root", Arrays.toString(participantRootKey(participant))));
	}

	public RResult<Block> getBlock(long rr) {
		RResult<Block> getBlock = inmemStore.getBlock(rr);
		Block res = getBlock.result;
		error err = getBlock.err;
		if (err != null) {
			RResult<Block> dbGetBlock = dbGetBlock(rr);
			res = dbGetBlock.result;
			err = dbGetBlock.err;
		}
		return new RResult<Block>(res, mapError(err, "Block", Arrays.toString(blockKey(rr))));
	}

	public error setBlock(Block block ) {
		error err = inmemStore.setBlock(block);
		if  (err != null) {
			return err;
		}
		return dbSetBlock(block);
	}

	public long lastBlockIndex() {
		return inmemStore.lastBlockIndex();
	}

	public RResult<Frame> getFrame(long rr ) {
		RResult<Frame> getFrame = inmemStore.getFrame(rr);
		Frame res = getFrame.result;
		error err = getFrame.err;
		if (err != null) {
			RResult<Frame> dbGetFrame = dbGetFrame(rr);
			res = dbGetFrame.result;
			err = dbGetFrame.err;
		}
		return new RResult<Frame>(res, mapError(err, "Frame", Arrays.toString(frameKey(rr))));
	}

	public error setFrame(Frame frame) {
		error err = inmemStore.setFrame(frame);
		if ( err != null) {
			return err;
		}
		return dbSetFrame(frame);
	}

	public error reset(Map<String,Root> roots) {
		return inmemStore.reset(roots);
	}

	public error close() {
		error err = inmemStore.close();
		if (err != null){
			return err;
		}
		db.close();
		return null;
	}

	public boolean needBoostrap() {
		return needBoostrap;
	}

	public String storePath() {
		return path;
	}

	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//DB Methods

	private ConcurrentNavigableMap<byte[],byte[]> eventMap;
	private ConcurrentNavigableMap<byte[],byte[]> participantMap;
	private ConcurrentNavigableMap<byte[],byte[]> participantEventMap;
	private ConcurrentNavigableMap<byte[],byte[]> participantRootMap;
	private ConcurrentNavigableMap<byte[],byte[]> roundMap;
	private ConcurrentNavigableMap<byte[],byte[]> blockMap;
	private ConcurrentNavigableMap<byte[],byte[]> frameMap;

	private void initDBMaps() {
		eventMap = db.treeMap("events_map", Serializer.BYTE_ARRAY, Serializer.BYTE_ARRAY).createOrOpen();
		participantMap = db.treeMap("participants_map", Serializer.BYTE_ARRAY, Serializer.BYTE_ARRAY).createOrOpen();
		participantEventMap = db.treeMap("participants_event", Serializer.BYTE_ARRAY, Serializer.BYTE_ARRAY).createOrOpen();
		participantRootMap = db.treeMap("participants_root", Serializer.BYTE_ARRAY, Serializer.BYTE_ARRAY).createOrOpen();
		roundMap = db.treeMap("rounds_map", Serializer.BYTE_ARRAY, Serializer.BYTE_ARRAY).createOrOpen();
		blockMap = db.treeMap("blocks_map", Serializer.BYTE_ARRAY, Serializer.BYTE_ARRAY).createOrOpen();
		frameMap = db.treeMap("frames_map", Serializer.BYTE_ARRAY, Serializer.BYTE_ARRAY).createOrOpen();
	}

	private RResult<byte[]> lookupEvent(String eventKey) {
		byte[] v= eventMap.get(eventKey.getBytes());
		//logger.field("eventKey", eventKey).field("v.length", v.length).debug("getEvent()");
		if (v == null) {
			return new RResult<>(null, error.Errorf(String.format("Not found key : %s", eventKey)));
		}
		return new RResult<>(v, null);
	}

	public RResult<Event> dbGetEvent(String key) {
		RResult<byte[]> getEvent = lookupEvent(key);
		error err = getEvent.err;
		byte[] eventBytes = getEvent.result;
		//logger.field("key", key).field("v", new String(eventBytes)).debug("dbGetEvent()");

		if (err != null) {
			return new RResult<Event>(null, err);
		}

		Event event = new Event();
		err = event.marshaller().protoUnmarshal(eventBytes);
		if  (err != null) {
			return new RResult<Event>(null, err);
		}

		return new RResult<Event>(event, null);
	}

	public error dbSetEvents(Event[] events) {
		// TODO why tx.Discard? how to convert.
//		tx = db.NewTransaction(true);
//		defer tx.Discard();
		for (Event event : events) {
			String eventHex = event.hex();
			//logger.field("eventHex", eventHex).debug("dbSetEvents()");
			byte[] eventBytes = eventHex.getBytes();

			RResult<byte[]> eventProto = event.marshaller().protoMarshal();
			byte[] val = eventProto.result;
			error err = eventProto.err;
			if (err != null) {
				return err;
			}

			//insert [event hash] => [event bytes]
			eventMap.put(eventBytes, val);
			//insert [topo_index] => [event hash]
			byte[] topoKey = topologicalEventKey(event.message.TopologicalIndex);
			//logger.field("topoKey", new String(topoKey)).debug("dbSetEvents()");
			eventMap.put(topoKey, eventBytes);
			//insert [participant_index] => [event hash]
			byte[] peKey = participantEventKey(event.creator(), event.index());
			//logger.field("topoKey", new String(peKey)).debug("dbSetEvents()");
			eventMap.put(peKey, eventBytes);
		}

		db.commit();
		return null;
	}

	public RResult<Event[]> dbTopologicalEvents() {
		Event[] res = null;
		long t = 0;

		error err = null;
		byte[] key;
		byte[] item;
		while (err == null) {
			key = topologicalEventKey(t);
			item = eventMap.get(key);

			//logger.field("key", new String(key)).field("item", Arrays.toString(item)).field("t",t).debug("dbTopologicalEvents");
			if (item == null) {
				err = StoreErr.newStoreErr("Event", StoreErrType.KeyNotFound, Arrays.toString(item));
				break;
			}
			byte[] eventBytes = eventMap.get(item);
			//logger.field("eventBytes", eventBytes).debug("dbTopologicalEvents");
			if (eventBytes == null) {
				err = StoreErr.newStoreErr("Event", StoreErrType.KeyNotFound, Arrays.toString(item));
				break;
			}

			Event event = new Event();
			err = event.marshaller().protoUnmarshal(eventBytes);
			if (err != null){
				break;
			}
			res = Appender.append(res, event);

			t++;
		}

		if (isDBKeyNotFound(err)) {
			//logger.field("err", err).debug("DBKeyNotFound");
			return new RResult<Event[]>(res, null);
		}

		//logger.field("res", res).field("err", err).debug("dbTopologicalEvents()");
		return new RResult<Event[]>(res, err);
	}

	public  RResult<String[]> dbParticipantEvents(String participant, long skip) {
		String[] res = null;

		long i = skip + 1;
		error err = null;
		byte[] key;
		byte[] item;

		while (err == null) {
			key = participantEventKey(participant, i);
			item = eventMap.get(key);

			//logger.field("i",  i).field("key", new String(key)).field("item", item).debug("dbParticipantEvents");
			if (item == null) {
				err = StoreErr.newStoreErr("Participant", StoreErrType.KeyNotFound, Arrays.toString(key));
				break;
			}
			res = Appender.append(res, new String(item));
			i++;
		}

		if (!isDBKeyNotFound(err)) {
			return new RResult<String[]>(res, err);
		}

		return new RResult<String[]>(res, null);
	}

	public RResult<String> dbParticipantEvent(String participant, long index) {
		//logger.field("participant", participant).field("index", index).debug("dbParticipantEvent()");
		byte[] key = participantEventKey(participant, index);
		byte[] data = participantMap.get(key);
		//logger.field("key", new String(key)).field("data", new String(data)).debug("dbParticipantEvent()");

		error err = null;
		if (data == null) {
			err = StoreErr.newStoreErr("Participant", StoreErrType.KeyNotFound, Arrays.toString(key));
			return new RResult<>("", err);
		}

		return new RResult<>( new String(data), null);
	}

	public error dbSetRoots(Map<String,Root> roots) {
		for (String participant : roots.keySet()) {
			Root root = roots.get(participant);
			RResult<byte[]> rootMarshal = root.marshaller().protoMarshal();
			byte[] val = rootMarshal.result;
			error err = rootMarshal.err;
			if (err != null) {
				return err;
			}
			byte[] key = participantRootKey(participant);
//			fmt.Println("Setting root", participant, "->", key)
			//insert [participant_root] => [root bytes]
			participantRootMap.put(key, val);
		}

		db.commit();
		return null;
	}

	public RResult<Root> dbGetRoot(String participant) {
		//logger.field("participant", participant).debug("dbGetRoot()");
		byte[] key = participantRootKey(participant);
		byte[] rootBytes = participantRootMap.get(key);
		//logger.field("key", new String(key))
		//	.field("rootBytes", new String(rootBytes)).debug("dbGetRoot()");

		error err = null;
		if (rootBytes == null) {
			err = StoreErr.newStoreErr("Participant", StoreErrType.KeyNotFound, Arrays.toString(key));
			return new RResult<>(new Root(), err);
		}

		Root root = new Root();
		err = root.marshaller().protoUnmarshal(rootBytes);
		if (err != null) {
			return new RResult<>(new Root(), err);
		}

		return new RResult<>(root, null);
	}

	public RResult<RoundInfo> dbGetRound(long index ) {
		byte[] key = roundKey(index);

		error err = null;
		byte[] roundBytes = roundMap.get(key);
		if (roundBytes == null) {
			err = StoreErr.newStoreErr("Round", StoreErrType.KeyNotFound, Arrays.toString(key));
		}

		if (err != null) {
			return new RResult<>(new RoundInfo(), err);
		}

		RoundInfo roundInfo = new RoundInfo ();
		err = roundInfo.marshaller().protoUnmarshal(roundBytes);
		if (err != null) {
			return new RResult<>(new RoundInfo(), err);
		}

		return new RResult<>(roundInfo, null);
	}

	public error dbSetRound(long index , RoundInfo round ) {
		byte[] key = roundKey(index);
		RResult<byte[]> protoMarshal = round.marshaller().protoMarshal();
		byte[] val = protoMarshal.result;
		error err = protoMarshal.err;
		if (err != null) {
			return err;
		}

		//insert [round_index] => [round bytes]
		roundMap.put(key, val);
		db.commit();
		return null;
	}

	public RResult<peers.Peers> dbGetParticipants() {
		peers.Peers res = new peers.Peers();
		error err = null;

		byte[] prefix = participantPrefix.getBytes();
		for (byte[] key : participantMap.keySet()) {
			byte[] value = participantMap.get(key);
			//logger.field("key", new String(key)).field("value", new String(value)).debug("dbGetParticipants()");

			if (value == null) {
				err = StoreErr.newStoreErr("Round", StoreErrType.KeyNotFound, Arrays.toString(key));
			}

			byte[] pubKey = Appender.sliceFromToEnd(key, prefix.length +1);

			res.addPeer(new Peer(new String(pubKey), ""));
		}

		return new  RResult<>(res, err);
	}

	public error dbSetParticipants(peers.Peers participants)  {
		for (String participant : participants.getByPubKey().keySet()) {
			Peer id = participants.getByPubKey().get(participant);
			byte[] key = participantKey(participant);
			byte[] value = String.valueOf(id.getID()).getBytes();
			//logger.field("key", new String(key)).field("value", new String(value)).debug("dbSetParticipants()");

			//insert [participant_participant] => [id]
			participantMap.put(key, value);
		}

//		db.commit();
		return null;
	}

	public RResult<Block> dbGetBlock(long index) {
		byte[] key = blockKey(index);

		byte[] blockBytes = blockMap.get(key);
		error  err = null;
		if (blockBytes == null) {
			err = StoreErr.newStoreErr("Block", StoreErrType.KeyNotFound, Arrays.toString(key));
			return new RResult<Block>(null, err);
		}

		Block block = new Block();
		err = block.marshaller().protoUnmarshal(blockBytes);
		if (err != null) {
			return new RResult<>(null, err);
		}

		return new RResult<>(block, null);
	}

	public error dbSetBlock(Block block)  {
		byte[] key = blockKey(block.Index());
		RResult<byte[]> protoMarshal = block.marshaller().protoMarshal();
		byte[] val = protoMarshal.result;
		error err = protoMarshal.err;
		if (err != null) {
			return err;
		}

		//insert [index] => [block bytes]
		blockMap.put(key, val);

		db.commit();
		return null;
	}

	public RResult<Frame> dbGetFrame(long index) {
		byte[] key = frameKey(index);

		byte[] frameBytes = frameMap.get(key);
		error  err = null;
		if (frameBytes == null) {
			err = StoreErr.newStoreErr("Frame", StoreErrType.KeyNotFound, Arrays.toString(key));
			return new RResult<>(new Frame(), err);
		}

		Frame frame = new Frame();
		err = frame.marshaller().protoUnmarshal(frameBytes);
		if (err != null) {
			return new RResult<Frame>(new Frame(), err);
		}

		return new RResult<Frame>(frame, null);
	}

	public error dbSetFrame(Frame frame)  {
		byte[] key = frameKey(frame.Round);
		RResult<byte[]> protoMarshal = frame.marshaller().protoMarshal();
		byte[] val = protoMarshal.result;
		error err = protoMarshal.err;
		if (err != null) {
			return err;
		}

		//insert [index] => [frame bytes]
		frameMap.put(key, val);
		db.commit();
		return null;
	}

	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	public boolean isDBKeyNotFound(error err) {
		return StoreErr.Is(err, StoreErrType.KeyNotFound);
	}

	public error mapError(error err, String name,  String key) {
		if (err != null) {
			if (isDBKeyNotFound(err)) {
				return StoreErr.newStoreErr(name, StoreErrType.KeyNotFound, key);
			}
		}
		return err;
	}

	public RResult<Event[]> topologicalEvents() {
		return dbTopologicalEvents();
	}
}
