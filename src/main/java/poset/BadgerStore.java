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
import common.RetResult;
import common.RetResult3;
import common.StoreErr;
import common.StoreErrType;
import common.error;
import peers.Peer;
import peers.Peers;

public class BadgerStore implements Store {
	peers.Peers participants;
	InmemStore inmemStore;
	DB db;
	String path;
	boolean needBoostrap;

	public static final String participantPrefix = "participant";
	public static final String rootSuffix        = "root";
	public static final String roundPrefix       = "round";
	public static final String topoPrefix        = "topo";
	public static final String blockPrefix       = "block";
	public static final String framePrefix       = "frame";

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

	//NewBadgerStore creates a brand new Store with a new database
	public static RetResult<BadgerStore> NewBadgerStore(peers.Peers participants, int cacheSize, String path) {

		InmemStore inmemStore = new InmemStore(participants, cacheSize);

	    DB db = DBMaker.fileDB(new File(path))
	            .transactionEnable().closeOnJvmShutdown().fileChannelEnable()
	            .make();

		error err;
		// TODO
//		DB handle, err := badger.Open(opts);
//		if (err != null) {
//			return new RetResult<BadgerStore>(null, err);
//		}

		BadgerStore store = new BadgerStore(participants, inmemStore, db, path);

		err = store.dbSetParticipants(participants);
		if  (err != null) {
			return new RetResult<BadgerStore>(null, err);
		}

		err = store.dbSetRoots(inmemStore.rootsByParticipant);
		if (err != null) {
			return new RetResult<BadgerStore>(null, err);
		}
		return new RetResult<>(store, null);
	}

	//LoadBadgerStore creates a Store from an existing database
	public static RetResult<BadgerStore> LoadBadgerStore(int cacheSize, String path) {
		error err = null;
		try {
			File file = new File(path);
		} catch (Exception e) {
			err = error.Errorf(e.getMessage());
		}
		if (err != null) {
			return new RetResult<BadgerStore>(null, err);
		}

		DB db = DBMaker.fileDB(new File(path))
	            .transactionEnable().closeOnJvmShutdown().fileChannelEnable()
	            .make();
//		if (err != null) {
//			return new RetResult<>(null, err);
//		}
		BadgerStore store = new BadgerStore(db, path, true);

		RetResult<peers.Peers> dbGetParticipantsCall = store.dbGetParticipants();
		peers.Peers participants = dbGetParticipantsCall.result;
		err = dbGetParticipantsCall.err;
		if (err != null) {
			return new RetResult<BadgerStore>(null, err);
		}

		InmemStore inmemStore = new InmemStore(participants, cacheSize);

		//read roots from db and put them in InmemStore
		Map<String, Root> roots = new HashMap<String,Root>();
		for (String p : store.participants.getByPubKey().keySet()) {
			RetResult<Root> dbGetRoot = store.dbGetRoot(p);
			Root root = dbGetRoot.result;
			err = dbGetRoot.err;
			if (err != null) {
				return new RetResult<BadgerStore>(null, err);
			}
			roots.put(p,  root);
		}
		err = inmemStore.Reset(roots);
		if (err != null) {
			return new RetResult<BadgerStore>(null, err);
		}

		store.participants = participants;
		store.inmemStore = inmemStore;

		return new RetResult<BadgerStore>(store, null);
	}

	public static RetResult<BadgerStore> LoadOrCreateBadgerStore(peers.Peers participants, int cacheSize, String path) {
		RetResult<BadgerStore> loadBadgerStore = LoadBadgerStore(cacheSize, path);

		BadgerStore store = loadBadgerStore.result;
		error err = loadBadgerStore.err;

		if (err != null) {
			System.err.println("Could not load store - creating new");
			RetResult<BadgerStore> newBadgerStore = NewBadgerStore(participants, cacheSize, path);
			store = newBadgerStore.result;
			err = newBadgerStore.err;

			if (err != null) {
				return new RetResult<BadgerStore>(null, err);
			}
		}

		return new RetResult<BadgerStore>(store, null);
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

	public int CacheSize() {
		return inmemStore.CacheSize();
	}

	public RetResult<peers.Peers> Participants() {
		return new RetResult<peers.Peers>(participants, null);
	}

	public RetResult<Map<String,Root>> RootsBySelfParent() {
		return inmemStore.RootsBySelfParent();
	}

	public RetResult<Event> GetEvent(String key) {
		//try to get it from cache
		RetResult<Event> getEvent = inmemStore.GetEvent(key);
		Event event = getEvent.result;
		error err = getEvent.err;
		//if not in cache, try to get it from db
		if (err != null) {
			RetResult<Event> dbGetEventCall = dbGetEvent(key);
			event = dbGetEventCall.result;
			err = dbGetEventCall.err;
		}
		return new RetResult<Event>(event, mapError(err, "Event", key));
	}

	public error SetEvent( Event event) {
		//try to add it to the cache
		error err = inmemStore.SetEvent(event);
		if (err != null) {
			return err;
		}
		//try to add it to the db
		return dbSetEvents(new Event[]{event});
	}

	public RetResult<String[]> ParticipantEvents(String participant, long skip) {
		RetResult<String[]> participantEventsCall = inmemStore.ParticipantEvents(participant, skip);
		String[] res = participantEventsCall.result;
		error err = participantEventsCall.err;
		if (err != null) {
			RetResult<String[]> dbParticipantEventsCall = dbParticipantEvents(participant, skip);
			res = dbParticipantEventsCall.result;
			err = dbParticipantEventsCall.err;
		}
		return new RetResult<String[]>(res, err);
	}

	public RetResult<String> ParticipantEvent(String participant, long index) {
		RetResult<String> participantEventCall = inmemStore.ParticipantEvent(participant, index);
		String result = participantEventCall.result;
		error err = participantEventCall.err;
		if (err != null) {
			RetResult<String> dbParticipantEventCall = dbParticipantEvent(participant, index);
			result = dbParticipantEventCall.result;
			err = dbParticipantEventCall.err;
		}
		return new RetResult<String>(result, mapError(err, "ParticipantEvent", Arrays.toString(participantEventKey(participant, index))));
	}

	public RetResult3<String,Boolean> LastEventFrom(String participant ) {
		return inmemStore.LastEventFrom(participant);
	}

	public RetResult3<String,Boolean> LastConsensusEventFrom(String participant ) {
		return inmemStore.LastConsensusEventFrom(participant);
	}

	public Map<Long,Long> KnownEvents() {
		Map<Long,Long> known = new HashMap<Long,Long>();
		for (String p : participants.getByPubKey().keySet()) {
			Peer pid = participants.getByPubKey().get(p);
			long index = (long) -1;
			RetResult3<String, Boolean> lastEventFromCall = LastEventFrom(p);
			String last = lastEventFromCall.result1;
			Boolean isRoot = lastEventFromCall.result2;
			error err = lastEventFromCall.err;
			if (err == null) {
				if (isRoot) {
					RetResult<Root> getRoot = GetRoot(p);
					Root root = getRoot.result;
					err = getRoot.err;
					if (err != null) {
						last = root.SelfParent.Hash;
						index = root.SelfParent.Index;
					}
				} else {
					RetResult<Event> getEventCall = GetEvent(last);
					Event lastEvent = getEventCall.result;
					err = getEventCall.err;
					if (err == null) {
						index = lastEvent.Index();
					}
				}

			}
			known.put(pid.GetID(), index);
		}
		return known;
	}

	public String[] ConsensusEvents() {
		return inmemStore.ConsensusEvents();
	}

	public long ConsensusEventsCount() {
		return inmemStore.ConsensusEventsCount();
	}

	public error AddConsensusEvent(Event event ) {
		return inmemStore.AddConsensusEvent(event);
	}

	public RetResult<RoundInfo> GetRound(long r) {
		RetResult<RoundInfo> getRound = inmemStore.GetRound(r);
		RoundInfo res = getRound.result;
		error err = getRound.err;
		if (err != null) {
			RetResult<RoundInfo> dbGetRoundCall = dbGetRound(r);
			res = dbGetRoundCall.result;
			err = dbGetRoundCall.err;
		}
		return new RetResult<RoundInfo>(res, mapError(err, "Round", Arrays.toString(roundKey(r))));
	}

	public error SetRound(long r , RoundInfo round ) {
		error err = inmemStore.SetRound(r, round);
		if (err != null) {
			return err;
		}
		return dbSetRound(r, round);
	}

	public long LastRound() {
		return inmemStore.LastRound();
	}

	public String[] RoundWitnesses(long r) {
		RetResult<RoundInfo> getRound = GetRound(r);
		RoundInfo round = getRound.result;
		error err = getRound.err;
		if (err != null) {
			return new String[] {};
		}
		return round.Witnesses();
	}

	public int RoundEvents(long r) {
		RetResult<RoundInfo> getRound = GetRound(r);
		RoundInfo round = getRound.result;
		error err = getRound.err;
		if (err != null) {
			return 0;
		}
		return round.Message.Events.size();
	}

	public RetResult<Root> GetRoot(String participant ) {
		RetResult<Root> getRoot = inmemStore.GetRoot(participant);
		Root root = getRoot.result;
		error err = getRoot.err;
		if (err != null) {
			RetResult<Root> dbGetRoot = dbGetRoot(participant);
			root = dbGetRoot.result;
			err = dbGetRoot.err;
		}
		return new  RetResult<Root>(root, mapError(err, "Root", Arrays.toString(participantRootKey(participant))));
	}

	public RetResult<Block> GetBlock(long rr) {
		RetResult<Block> getBlock = inmemStore.GetBlock(rr);
		Block res = getBlock.result;
		error err = getBlock.err;
		if (err != null) {
			RetResult<Block> dbGetBlock = dbGetBlock(rr);
			res = dbGetBlock.result;
			err = dbGetBlock.err;
		}
		return new RetResult<Block>(res, mapError(err, "Block", Arrays.toString(blockKey(rr))));
	}

	public error SetBlock(Block block ) {
		error err = inmemStore.SetBlock(block);
		if  (err != null) {
			return err;
		}
		return dbSetBlock(block);
	}

	public long LastBlockIndex() {
		return inmemStore.LastBlockIndex();
	}

	public RetResult<Frame> GetFrame(long rr ) {
		RetResult<Frame> getFrame = inmemStore.GetFrame(rr);
		Frame res = getFrame.result;
		error err = getFrame.err;
		if (err != null) {
			RetResult<Frame> dbGetFrame = dbGetFrame(rr);
			res = dbGetFrame.result;
			err = dbGetFrame.err;
		}
		return new RetResult<Frame>(res, mapError(err, "Frame", Arrays.toString(frameKey(rr))));
	}

	public error SetFrame(Frame frame) {
		error err = inmemStore.SetFrame(frame);
		if ( err != null) {
			return err;
		}
		return dbSetFrame(frame);
	}

	public error Reset(Map<String,Root> roots) {
		return inmemStore.Reset(roots);
	}

	public error Close() {
		error err = inmemStore.Close();
		if (err != null){
			return err;
		}
		db.close();
		return null;
	}

	public boolean NeedBoostrap() {
		return needBoostrap;
	}

	public String StorePath() {
		return path;
	}

	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//DB Methods

	private ConcurrentNavigableMap<byte[],byte[]> eventMap;
	private ConcurrentNavigableMap<byte[],byte[]> participantMap;
	private ConcurrentNavigableMap<byte[],byte[]> roundMap;
	private ConcurrentNavigableMap<byte[],byte[]> blockMap;
	private ConcurrentNavigableMap<byte[],byte[]> frameMap;

	private void initDBMaps() {
		eventMap = db.treeMap("events_map", Serializer.BYTE_ARRAY, Serializer.BYTE_ARRAY).createOrOpen();
		participantMap = db.treeMap("participants_map", Serializer.BYTE_ARRAY, Serializer.BYTE_ARRAY).createOrOpen();
		roundMap = db.treeMap("rounds_map", Serializer.BYTE_ARRAY, Serializer.BYTE_ARRAY).createOrOpen();
		blockMap = db.treeMap("blocks_map", Serializer.BYTE_ARRAY, Serializer.BYTE_ARRAY).createOrOpen();
		frameMap = db.treeMap("frames_map", Serializer.BYTE_ARRAY, Serializer.BYTE_ARRAY).createOrOpen();
	}

	private RetResult<String> getEvent(String eventKey) {
		byte[] v= eventMap.get(eventKey.getBytes());
		if (v == null) {
			return new RetResult<>(null, error.Errorf(String.format("Not found key : %s", eventKey)));
		}
		return new RetResult<>(new String(v), null);
	}

	public RetResult<Event> dbGetEvent(String key) {
		byte[] eventBytes;

		RetResult<String> getEvent = getEvent(key);
		error err = getEvent.err;
		eventBytes = getEvent.result.getBytes();

		if (err != null) {
			return new RetResult<Event>(new Event(), err);
		}

		Event event = new Event();
		err = event.ProtoUnmarshal(eventBytes);
		if  (err != null) {
			return new RetResult<Event>(new Event(), err);
		}

		return new RetResult<Event>(event, null);
	}

	public error dbSetEvents(Event[] events) {

		// TODO why tx.Discard? how to convert.
//		tx = db.NewTransaction(true);
//		defer tx.Discard();
		for (Event event : events) {
			String eventHex = event.Hex();
			RetResult<byte[]> eventProto = event.ProtoMarshal();
			byte[] val = eventProto.result;
			error err = eventProto.err;
			if (err != null) {
				return err;
			}
			//check if it already exists
			byte[] v = eventMap.get(eventHex.getBytes());
			boolean existent =  v != null;

			//insert [event hash] => [event bytes]
			eventMap.put(eventHex.getBytes(), val);

			if (existent) {
				//insert [topo_index] => [event hash]
				byte[] topoKey = topologicalEventKey(event.Message.TopologicalIndex);
				eventMap.put(topoKey, eventHex.getBytes());
				//insert [participant_index] => [event hash]
				byte[] peKey = participantEventKey(event.Creator(), event.Index());
				eventMap.put(peKey, eventHex.getBytes());
			}
		}

		db.commit();
		return null;
	}

	public RetResult<Event[]> dbTopologicalEvents() {
		Event[] res = null;
		long t = 0;

		error err = null;
		byte[] key = topologicalEventKey(t);
		byte[] item = eventMap.get(key);
		error errr = null;
		while (errr == null) {
			byte[] evKey = item;
			byte[] eventBytes = eventMap.get(evKey);

			if (eventBytes == null) {
				err = StoreErr.newStoreErr("Event", StoreErrType.KeyNotFound, Arrays.toString(evKey));
				break;
			}

			Event event = new Event();
			err = event.ProtoUnmarshal(eventBytes);
			if (err != null){
				break;
			}
			res = Appender.append(res, event);

			t++;
			key = topologicalEventKey(t);
			item = eventMap.get(key);
			if (item == null) {
				errr = StoreErr.newStoreErr("Event", StoreErrType.KeyNotFound, Arrays.toString(key));
			}
		}

		if (!isDBKeyNotFound(errr)) {
			err = errr;
		}

		return new RetResult<Event[]>(res, err);
	}

	public  RetResult<String[]> dbParticipantEvents(String participant, long skip) {
		String[] res = null;

		long i = skip + 1;
		byte[] key = participantEventKey(participant, i);
		byte[] item = participantMap.get(key);
		error errr = null;
		if (item == null) {
			errr = StoreErr.newStoreErr("Participant", StoreErrType.KeyNotFound, Arrays.toString(key));
		}
		else {
			while (errr == null) {
				res = Appender.append(res, new String(item));
				i++;
				key = participantEventKey(participant, i);
				item = participantMap.get(key);
				if (item == null) {
					errr = StoreErr.newStoreErr("Pariticipant", StoreErrType.KeyNotFound, Arrays.toString(key));
				}
			}
		}

		if (!isDBKeyNotFound(errr)) {
			return new RetResult<String[]>(res, errr);
		}

		return new RetResult<String[]>(res, null);
	}

	public RetResult<String> dbParticipantEvent(String participant, long index) {
		byte[] key = participantEventKey(participant, index);

		byte[] data = participantMap.get(key);
		error err = null;
		if (data == null) {
			err = StoreErr.newStoreErr("Pariticipant", StoreErrType.KeyNotFound, Arrays.toString(key));
			return new RetResult<>("", err);
		}

		return new RetResult<>( new String(data), null);
	}

	public error dbSetRoots(Map<String,Root> roots) {
//		tx = db.NewTransaction(true);
//		defer tx.Discard();

		for (String participant : roots.keySet()) {
			Root root = roots.get(participant);
			RetResult<byte[]> rootMarshal = root.ProtoMarshal();
			byte[] val = rootMarshal.result;
			error err = rootMarshal.err;
			if (err != null) {
				return err;
			}
			byte[] key = participantRootKey(participant);
//			fmt.Println("Setting root", participant, "->", key)
			//insert [participant_root] => [root bytes]
			participantMap.put(key, val);
		}

		db.commit();
		return null;
	}

	public RetResult<Root> dbGetRoot(String participant) {
		byte[] key = participantRootKey(participant);

		byte[] rootBytes = participantMap.get(key);
		error err = null;
		if (rootBytes == null) {
			err = StoreErr.newStoreErr("Pariticipant", StoreErrType.KeyNotFound, Arrays.toString(key));
			return new RetResult<>(new Root(), err);
		}

		Root root = new Root();
		err = root.ProtoUnmarshal(rootBytes);
		if (err != null) {
			return new RetResult<>(new Root(), err);
		}

		return new RetResult<>(root, null);
	}

	public RetResult<RoundInfo> dbGetRound(long index ) {
		byte[] key = roundKey(index);

		error err = null;
		byte[] roundBytes = roundMap.get(key);
		if (roundBytes == null) {
			err = StoreErr.newStoreErr("Round", StoreErrType.KeyNotFound, Arrays.toString(key));
		}

		if (err != null) {
			return new RetResult<>(new RoundInfo(), err);
		}

		RoundInfo roundInfo = new RoundInfo ();
		err = roundInfo.ProtoUnmarshal(roundBytes);
		if (err != null) {
			return new RetResult<>(new RoundInfo(), err);
		}

		return new RetResult<>(roundInfo, null);
	}

	public error dbSetRound(long index , RoundInfo round ) {
//		tx = db.NewTransaction(true);
//		defer tx.Discard();

		byte[] key = roundKey(index);
		RetResult<byte[]> protoMarshal = round.ProtoMarshal();
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

	public RetResult<peers.Peers> dbGetParticipants() {
		peers.Peers res = new peers.Peers();
		error err = null;

		byte[] prefix = participantPrefix.getBytes();
		for (byte[] key : participantMap.keySet()) {

			byte[] value = participantMap.get(key);
			if (value == null) {
				err = StoreErr.newStoreErr("Round", StoreErrType.KeyNotFound, Arrays.toString(key));
			}

			byte[] pubKey = Arrays.copyOfRange(value, prefix.length +1, value.length);

			res.AddPeer(new Peer(new String(pubKey), ""));
		}

		return new  RetResult<>(res, err);
	}

	public error dbSetParticipants(peers.Peers participants)  {
//		tx = db.NewTransaction(true);
//		defer tx.Discard();

		for (String participant : participants.getByPubKey().keySet()) {
			Peer id = participants.getByPubKey().get(participant);
			byte[] key = participantKey(participant);
//			val = byte[](strconv.FormatInt(id.GetID(), 10));
			byte[] val = String.valueOf(id.GetID()).getBytes();

			//insert [participant_participant] => [id]
			participantMap.put(key, val);
		}

		db.commit();
		return null;
	}

	public RetResult<Block> dbGetBlock(long index) {
		byte[] key = blockKey(index);

		byte[] blockBytes = blockMap.get(key);
		error  err = null;
		if (blockBytes == null) {
			err = StoreErr.newStoreErr("Block", StoreErrType.KeyNotFound, Arrays.toString(key));
			return new RetResult<Block>(new Block(), err);
		}

		Block block = new Block();
		RetResult<Block> protoUnmarshal = block.ProtoUnmarshal(blockBytes);
		block = protoUnmarshal.result;
		err = protoUnmarshal.err;
		if (err != null) {
			return new RetResult<>(new Block(), err);
		}

		return new RetResult<>(block, null);
	}

	public error dbSetBlock(Block block)  {
//		tx = db.NewTransaction(true);
//		defer tx.Discard();

		byte[] key = blockKey(block.Index());
		RetResult<byte[]> protoMarshal = block.ProtoMarshal();
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

	public RetResult<Frame> dbGetFrame(long index) {
		byte[] key = frameKey(index);

		byte[] frameBytes = frameMap.get(key);
		error  err = null;
		if (frameBytes == null) {
			err = StoreErr.newStoreErr("Frame", StoreErrType.KeyNotFound, Arrays.toString(key));
			return new RetResult<>(new Frame(), err);
		}

		Frame frame = new Frame();
		err = frame.ProtoUnmarshal(frameBytes);
		if (err != null) {
			return new RetResult<Frame>(new Frame(), err);
		}

		return new RetResult<Frame>(frame, null);
	}

	public error dbSetFrame(Frame frame)  {
//		tx = db.NewTransaction(true);
//		defer tx.Discard();

		byte[] key = frameKey(frame.Round);
		RetResult<byte[]> protoMarshal = frame.ProtoMarshal();
		byte[] val = protoMarshal.result;
		error err = protoMarshal.err;
		if (err != null) {
			return err;
		}

		//insert [index] => [block bytes]
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

	public RetResult<Event[]> TopologicalEvents() {
		return dbTopologicalEvents();
	}
}
