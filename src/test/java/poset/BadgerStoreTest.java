package poset;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import autils.Appender;
import autils.FileUtils;
import common.RResult2;
import common.RetResult;
import common.RetResult3;
import common.error;
import peers.Peer;
import peers.Peers;
import peers.Peers.PubKeyPeers;

/**
 * Blank test
 * @author qn
 *
 */
public class BadgerStoreTest {
	static File currentDirectory = new File(new File(".").getAbsolutePath());

	private String testDir = currentDirectory.getAbsolutePath() + "test_data";

	private String dbPath = Paths.get(testDir, "badger").toString();

	private RResult2<BadgerStore, pub[]> initBadgerStore(int cacheSize) {
		int n = 3;
		pub[] participantPubs = null;
		Peers participants = new Peers();
		for (int i = 0; i < n; i++) {
			KeyPair key = crypto.Utils.GenerateECDSAKeyPair().result;
			byte[] pubKey = crypto.Utils.FromECDSAPub(key.getPublic());
			Peer peer = new Peer(crypto.Utils.toHexString(pubKey), "");
			participants.AddPeer(peer);
			participantPubs = Appender.append(participantPubs,
				new pub(peer.GetID(), key, pubKey, peer.GetPubKeyHex()));
		}

		recreateTestDir();

		RetResult<BadgerStore> newBadgerStore = BadgerStore.NewBadgerStore(participants, cacheSize, dbPath);
		BadgerStore store = newBadgerStore.result;
		error err = newBadgerStore.err;
		assertNull("No error creating badger store", err);

		return new RResult2<>(store, participantPubs);
	}

	private void recreateTestDir() {
		error err = FileUtils.delete(testDir);
		assertNull("No error deleting folder", err);

		FileUtils.mkdirs(testDir, FileUtils.MOD_777);
		err = FileUtils.mkdirs(dbPath, FileUtils.MOD_755).err;
		assertNull("No error creating a file", err);
	}

	private void removeBadgerStore(BadgerStore store) {
		error err = store.Close();
		assertNull("No error", err);

		err = FileUtils.delete(testDir);
		assertNull("No error deleting folder", err);
	}

	private BadgerStore createTestDB(String dir) {
		Peers participants = Peers.NewPeersFromSlice(new peers.Peer[]{
			new Peer("0xaa", ""),
			new Peer("0xbb", ""),
			new Peer("0xcc", ""),
		});

		int cacheSize = 1;
		RetResult<BadgerStore> newStore = BadgerStore.NewBadgerStore(participants, cacheSize, dbPath);
		BadgerStore store = newStore.result;
		error err = newStore.err;
		assertNull("No error", err);
		return store;
	}

	@Test
	public void TestNewBadgerStore() {
		recreateTestDir();
		BadgerStore store = createTestDB(dbPath);

		assertEquals("Store path should mathc", store.path, dbPath);
		assertTrue("Path exists", FileUtils.fileExist(dbPath));

		//check roots
		Map<String, Root> inmemRoots = store.inmemStore.rootsByParticipant;
		assertEquals("DB root should have 3 items", 3, inmemRoots.size());

		error err;
		for (String participant : inmemRoots.keySet()) {
			Root root = inmemRoots.get(participant);
			RetResult<Root> dbGetRoot = store.dbGetRoot(participant);
			Root dbRoot = dbGetRoot.result;
			err = dbGetRoot.err;
			assertNull(String.format("No error when retrieving DB root for participant %s", participant), err);
			assertEquals(String.format("%s DB root should match", participant), root, dbRoot);
		}

		removeBadgerStore(store);
	}

	@Test
	public void TestLoadBadgerStore() {
		recreateTestDir();
		BadgerStore store = createTestDB(dbPath);
		store.Close();
		int cacheSize = 100;
		RetResult<BadgerStore> loadBadgerStore = BadgerStore.LoadBadgerStore(cacheSize, store.path);
		store = loadBadgerStore.result;
		error err = loadBadgerStore.err;
		assertNull("No error", err);

		RetResult<Peers> dbGetParticipants = store.dbGetParticipants();
		Peers dbParticipants = dbGetParticipants.result;
		err = dbGetParticipants.err;
		assertNull("No error", err);

		assertEquals("store.participants  length should be 3", 3, store.participants.Len());

		assertEquals("store.participants length should match", dbParticipants.Len(), store.participants.Len());

		PubKeyPeers byPubKey = dbParticipants.getByPubKey();
		for (String dbP : byPubKey.keySet()) {
			Peer dbPeer = byPubKey.get(dbP);
			Peer peer = store.participants.ByPubKey(dbP);
			assertNotNull(String.format("BadgerStore participants should contain %s", dbP), peer);
			assertEquals(String.format("participant %s ID should match", dbP), dbPeer.GetID(), peer.GetID());
		}

		removeBadgerStore(store);
	}

	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//Call DB methods directly
	//@Test
	public void TestDBEventMethods() {
		int cacheSize = 1; // Inmem_store's caches accept positive cacheSize only
		int testSize = 100;
		RResult2<BadgerStore, pub[]> initBadgerStore = initBadgerStore(cacheSize);
		BadgerStore store = initBadgerStore.result1;
		pub[] participants = initBadgerStore.result2;

		//insert events in db directly
		Map<String, Event[]> events = new HashMap<String, Event[]>();
		long topologicalIndex = 0L;
		Event[] topologicalEvents = null;
		for (pub p : participants) {
			Event[] items = null;
			for (int k =0; k < testSize; k++){
				Event event = new Event(
					new byte[][]{String.format("%s_%d", p.hex.substring(0,5), k).getBytes()},
					new InternalTransaction[]{},
					new BlockSignature[]{new BlockSignature("validator".getBytes(), 0, "r|s")},
					new String[]{"", ""},
					p.pubKey,
					k, null);

				event.Sign(p.privKey.getPrivate());
				event.Message.TopologicalIndex = topologicalIndex;
				topologicalIndex++;
				topologicalEvents = Appender.append(topologicalEvents, event);

				items = Appender.append(items, event);
				error err = store.dbSetEvents(new Event[]{event});
				assertNull("No error", err);
			}
			events.put(p.hex, items);
		}

		//check events where correctly inserted and can be retrieved
		error err;
		for (String p : events.keySet()) {
			Event[] evs = events.get(p);
			for (int k  = 0; k< evs.length; ++k) {
				Event ev = evs[k];
				RetResult<Event> dbGetEvent = store.dbGetEvent(ev.Hex());
				Event rev = dbGetEvent.result;
				err = dbGetEvent.err;
				assertNull("No error", err);

				assertEquals(String.format("events[%s][%d].Body should be %#v, not %#v", p, k), ev.Message.Body, rev.Message.Body);
				assertEquals(String.format("events[%s][%d].Signature should be %#v, not %#v", p, k), ev.Message.Signature, rev.Message.Signature);

				RetResult<Boolean> verify = rev.Verify();
				boolean ver = verify.result;
				err = verify.err;
				assertNull("No error", err);
				assertTrue("Verified signature returns true", ver);
			}
		}

		//check topological order of events was correctly created
		RetResult<Event[]> dbTopologicalEventsCall = store.dbTopologicalEvents();
		Event[] dbTopologicalEvents = dbTopologicalEventsCall.result;
		err = dbTopologicalEventsCall.err;
		assertNull("No error", err);

		assertEquals("Length of dbTopologicalEvents should match", topologicalEvents.length, dbTopologicalEvents.length);

		for (int i = 0; i< dbTopologicalEvents.length; ++i) {
			Event dte = dbTopologicalEvents[i];
			Event te = topologicalEvents[i];
			assertEquals(String.format("dbTopologicalEvents[%d].Hex should match", i), te.Hex(), dte.Hex());
			assertEquals(String.format("dbTopologicalEvents[%d].Body should match", i), te.Message.Body, dte.Message.Body);

			assertEquals(String.format("dbTopologicalEvents[%d].Signature should match", i),
						te.Message.Signature, dte.Message.Signature);


			RetResult<Boolean> verify = dte.Verify();
			boolean ver = verify.result;
			err = verify.err;
			assertNull("No error", err);
			assertTrue("Verified signature returns true", ver);
		}

		//check that participant events where correctly added
		int skipIndex = -1; //do not skip any indexes
		for (pub p : participants) {
			RetResult<String[]> dbParticipantEventsCall= store.dbParticipantEvents(p.hex, skipIndex);
			String[] pEvents = dbParticipantEventsCall.result;
			err = dbParticipantEventsCall.err;
			assertNull("No error", err);

			assertEquals(String.format("%s should have %d events, not %d", p.hex), testSize, pEvents.length);

			Event[] expectedEvents = Appender.sliceFromToEnd(events.get(p.hex), skipIndex+1);
			for (int k =0; k < expectedEvents.length; ++k) {
				Event e = expectedEvents[k];
				assertEquals(String.format("ParticipantEvents[%s][%d] should match", p.hex, k), e.Hex(), pEvents[k]);
			}
		}

		removeBadgerStore(store);
	}

	@Test
	public void TestDBRoundMethods() {
		int cacheSize = 1; // Inmem_store's caches accept positive cacheSize only
		RResult2<BadgerStore, pub[]> initBadgerStore = initBadgerStore(cacheSize);
		BadgerStore store = initBadgerStore.result1;
		pub[] participants = initBadgerStore.result2;

		RoundInfo round = new RoundInfo();
		HashMap<String, Event> events = new HashMap<String,Event>();
		for (pub p: participants) {
			Event event = new Event(new byte[][]{},
				new InternalTransaction[]{},
				new BlockSignature[]{},
				new String[]{"", ""},
				p.pubKey,
				0, null);
			events.put(p.hex, event);
			round.AddEvent(event.Hex(), true);
		}

		error err = store.dbSetRound(0, round);
		assertNull("No error", err);

		RetResult<RoundInfo> dbGetRound = store.dbGetRound(0);
		RoundInfo storedRound = dbGetRound.result;
		err = dbGetRound.err;
		assertNull("No error", err);

		assertEquals("Round and StoredRound do not match", round, storedRound);

		String[] witnesses = store.RoundWitnesses(0);
		String[] expectedWitnesses = round.Witnesses();
		assertEquals("There should be match length of witnesses", expectedWitnesses.length, witnesses.length);

		for (String w : expectedWitnesses) {
			assertTrue(String.format("Witnesses should contain %s", w), Arrays.asList(witnesses).contains(w));
		}

		removeBadgerStore(store);
	}

	@Test
	public void TestDBParticipantMethods() {
		int cacheSize = 1; // Inmem_store's caches accept positive cacheSize only
		BadgerStore store = initBadgerStore(cacheSize).result1;

		error err = store.dbSetParticipants(store.participants);
		assertNull("No error", err);

		RetResult<Peers> dbGetParticipants = store.dbGetParticipants();
		Peers participantsFromDB = dbGetParticipants.result;
		err = dbGetParticipants.err;
		assertNull("No error", err);

		PubKeyPeers byPubKey = store.participants.getByPubKey();
		for (String p : byPubKey.keySet()) {
			Peer peer = byPubKey.get(p);
			Peer dbPeer = participantsFromDB.ByPubKey(p);
			assertNotNull(String.format("DB contains participant %s", p), dbPeer);
			assertEquals(String.format("DB participant %s should have matching ID", p), peer.GetID(), dbPeer.GetID());
		}

		removeBadgerStore(store);
	}

	@Test
	public void TestDBBlockMethods() {
		int cacheSize = 1; // Inmem_store's caches accept positive cacheSize only
		RResult2<BadgerStore, pub[]> initBadgerStore = initBadgerStore(cacheSize);
		BadgerStore store = initBadgerStore.result1;
		pub[] participants = initBadgerStore.result2;

		int index = 0;
		int roundReceived = 5;
		byte[][] transactions = new byte[][]{
			"tx1".getBytes(),
			"tx2".getBytes(),
			"tx3".getBytes(),
			"tx4".getBytes(),
			"tx5".getBytes(),
		};
		byte[] frameHash = "this is the frame hash".getBytes();

		Block block = new Block(index, roundReceived, frameHash, transactions);

		RetResult<BlockSignature> signCall = block.Sign(participants[0].privKey);
		BlockSignature sig1 = signCall.result;
		error err = signCall.err;
		assertNull("No error", err);

		signCall = block.Sign(participants[1].privKey);
		BlockSignature sig2 = signCall.result;
		err = signCall.err;
		assertNull("No error", err);

		block.SetSignature(sig1);
		block.SetSignature(sig2);

		// "Store Block"
		err = store.dbSetBlock(block);
		assertNull("No error", err);

		RetResult<Block> dbGetBlock = store.dbGetBlock(index);
		Block storedBlock = dbGetBlock.result;
		err = dbGetBlock.err;
		assertNull("No error", err);
		assertEquals("Block and StoredBlock do not match", storedBlock, block);

		// "Check signatures in stored Block"
		dbGetBlock = store.dbGetBlock(index);
		storedBlock = dbGetBlock.result;
		err = dbGetBlock.err;
		assertNull("No error", err);

		String val1Sig = storedBlock.GetSignatures().get(participants[0].hex);
		assertNotNull("Validator1 signature is stored in block", val1Sig);
		assertEquals("Validator1 block signatures matches", val1Sig, sig1.Signature);

		String val2Sig = storedBlock.GetSignatures().get(participants[1].hex);
		assertNotNull("Validator2 signature not stored in block", val2Sig);
		assertEquals("Validator2 block signatures matches", val2Sig, sig2.Signature);

		removeBadgerStore(store);
	}

	@Test
	public void TestDBFrameMethods() {
		int cacheSize = 1; // Inmem_store's caches accept positive cacheSize only
		RResult2<BadgerStore, pub[]> initBadgerStoreCall = initBadgerStore(cacheSize);
		BadgerStore store = initBadgerStoreCall.result1;
		pub[] participants = initBadgerStoreCall.result2;

		EventMessage[] events = new EventMessage[participants.length];
		Root[] roots = new Root[participants.length];
		for (int id = 0; id < participants.length; ++id) {
			pub p = participants[id];
			Event event = new Event(
				new byte[][]{String.format("%s_%d", p.hex.substring(0,5), 0).getBytes()},
				new InternalTransaction[]{},
				new BlockSignature[]{new BlockSignature("validator".getBytes(), 0, "r|s")},
				new String[]{"", ""},
				p.pubKey,
				0, null);
			event.Sign(p.privKey.getPrivate());
			events[id] = event.Message;
			roots[id] = new Root(id);
		}
		Frame frame = new Frame(1L, roots, events);

		// "Store Frame"
		error err = store.dbSetFrame(frame);
		assertNull("No error", err);

		RetResult<Frame> dbGetFrame = store.dbGetFrame(frame.Round);
		Frame storedFrame = dbGetFrame.result;
		err = dbGetFrame.err;
		assertNull("No error", err);
		assertEquals("Frame and StoredFrame do not match", storedFrame, frame);

		removeBadgerStore(store);
	}

	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//Check that the wrapper methods work
	//These methods use the inmemStore as a cache on top of the DB
//	@Test
	public void TestBadgerEvents() {
		//Insert more events than can fit in cache to test retrieving from db.
		int cacheSize = 10;
		int testSize = 100;
		RResult2<BadgerStore, pub[]> initBadgerStore = initBadgerStore(cacheSize);
		BadgerStore store = initBadgerStore.result1;
		pub[] participants = initBadgerStore.result2;

		//insert event
		HashMap<String, Event[]> events = new HashMap<String,Event[]>();
		for (pub p : participants) {
			Event[] items = null;
			for (int k = 0; k < testSize; k++) {
				Event event = new Event(
						new byte[][]{String.format("%s_%d", p.hex.substring(0,5), k).getBytes()},
						new InternalTransaction[]{},
						new BlockSignature[]{new BlockSignature("validator".getBytes(), 0, "r|s")},
						new String[]{"", ""},
						p.pubKey,
						k, null);
				items = Appender.append(items, event);
				error err = store.SetEvent(event);
				assertNull("No error", err);
			}
			events.put(p.hex, items);
		}

		// check that events were correclty inserted
		for ( String p : events.keySet()) {
			Event[] evs = events.get(p);

			for (int k = 0; k < evs.length; ++k) {
				Event ev = evs[k];
				RetResult<Event> getEvent = store.GetEvent(ev.Hex());
				Event rev = getEvent.result;
				error err = getEvent.err;
				assertNull("No error", err);

				assertEquals(String.format("events[%s][%d].Body should be %#v, not %#v", p, k), ev.Message.Body, rev.Message.Body);
				assertEquals(String.format("events[%s][%d].Signature should be %#v, not %#v", p, k), ev.Message.Signature, rev.Message.Signature);
			}
		}

		//check retrieving events per participant
		int skipIndex = -1; //do not skip any indexes
		for (pub p : participants) {
			RetResult<String[]> pEventsCall = store.ParticipantEvents(p.hex, skipIndex);
			String[] pEvents = pEventsCall.result;
			error err = pEventsCall.err;
			assertNull("No error", err);
			assertEquals(String.format("%s should have %d events, not %d", p.hex), testSize, pEvents.length);

			Event[] expectedEvents = Appender.sliceFromToEnd(events.get(p.hex), skipIndex+1);
			for (int k = 0; k < expectedEvents.length; ++k) {
				Event e = expectedEvents[k];
				assertEquals(String.format("ParticipantEvents[%s][%d] should be %s, not %s",
						p.hex, k), e.Hex(), pEvents[k]);
			}
		}

		//check retrieving participant last
		for (pub p : participants) {
			RetResult3<String, Boolean> lastEventFrom = store.LastEventFrom(p.hex);
			String last = lastEventFrom.result1;
			error err = lastEventFrom.err;
			assertNull("No error", err);

			Event[] evs = events.get(p.hex);
			Event expectedLast = evs[evs.length-1];
			assertEquals(String.format("%s last should be %s, not %s", p.hex), expectedLast.Hex(), last);
		}

		HashMap<Long, Long> expectedKnown = new HashMap<Long,Long>();
		for (pub p : participants) {
			expectedKnown.put((long) p.id, (long) testSize - 1);
		}
		Map<Long, Long> known = store.KnownEvents();
		assertEquals("Known should match", known, expectedKnown);

		for (pub p : participants) {
			Event[] evs = events.get(p.hex);
			for (Event ev : evs) {
				error err = store.AddConsensusEvent(ev);
				assertNull("No error", err);
			}
		}

		removeBadgerStore(store);
	}

	@Test
	public void TestBadgerRounds() {
		int cacheSize = 1; // Inmem_store's caches accept positive cacheSize only
		RResult2<BadgerStore, pub[]> initBadgerStore = initBadgerStore(cacheSize);
		BadgerStore store = initBadgerStore.result1;
		pub[] participants = initBadgerStore.result2;

		RoundInfo round = new RoundInfo();
		HashMap<String, Event> events = new HashMap<String,Event>();
		for (pub p : participants) {
			Event event= new Event(new byte[][]{},
				new InternalTransaction[]{},
				new BlockSignature[]{},
				new String[]{"", ""},
				p.pubKey,
				0, null);
			events.put(p.hex,event);
			round.AddEvent(event.Hex(), true);
		}

		error err = store.SetRound(0, round);
		assertNull("No error", err);

		long c = store.LastRound();
		assertEquals("Store LastRound should be 0", 0, c);

		RetResult<RoundInfo> getRound = store.GetRound(0);
		RoundInfo storedRound = getRound.result;
		err = getRound.err;
		assertNull("No error", err);

		assertEquals("Round and StoredRound do not match", round, storedRound);

		String[] witnesses = store.RoundWitnesses(0);
		String[] expectedWitnesses = round.Witnesses();
		assertEquals("There should be %d witnesses, not %d", expectedWitnesses.length, witnesses.length);
		for (String w : expectedWitnesses) {
			assertTrue(String.format("Witnesses should contain %s", w), Arrays.asList(witnesses).contains(w));
		}

		removeBadgerStore(store);
	}

	@Test
	public void TestBadgerBlocks() {
		int cacheSize = 1; // Inmem_store's caches accept positive cacheSize only
		RResult2<BadgerStore, pub[]> initBadgerStore = initBadgerStore(cacheSize);
		BadgerStore store = initBadgerStore.result1;
		pub[] participants = initBadgerStore.result2;

		int index = 0;
		int roundReceived = 5;
		byte[][] transactions = new byte[][]{
			"tx1".getBytes(),
			"tx2".getBytes(),
			"tx3".getBytes(),
			"tx4".getBytes(),
			"tx5".getBytes(),
		};
		byte[] frameHash = "this is the frame hash".getBytes();
		Block block = new Block(index, roundReceived, frameHash, transactions);
		RetResult<BlockSignature> signCall = block.Sign(participants[0].privKey);
		BlockSignature sig1 = signCall.result;
		error err = signCall.err;
		assertNull("No error", err);

		signCall = block.Sign(participants[1].privKey);
		BlockSignature sig2 = signCall.result;
		err = signCall.err;
		assertNull("No error", err);

		block.SetSignature(sig1);
		block.SetSignature(sig2);

		//"Store Block"
		err = store.SetBlock(block);
		assertNull("No error", err);

		RetResult<Block> getBlock = store.GetBlock(index);
		Block storedBlock = getBlock.result;
		err = getBlock.err;
		assertNull("No error", err);
		assertEquals("Block and StoredBlock do not match", storedBlock, block);

		// "Check signatures in stored Block"
		RetResult<Block> getBlock2 = store.GetBlock(index);
		storedBlock = getBlock2.result;
		err = getBlock2.err;
		assertNull("No error", err);

		String val1Sig = storedBlock.GetSignatures().get(participants[0].hex);
		assertNotNull("Validator1 signature is stored in block", val1Sig);
		assertEquals("Validator1 block signatures should match", val1Sig, sig1.Signature);

		String val2Sig = storedBlock.GetSignatures().get(participants[1].hex);
		assertNotNull("Validator2 signature not stored in block", val2Sig);
		assertEquals("Validator2 block signatures should match", val2Sig, sig2.Signature);

		removeBadgerStore(store);
	}

	@Test
	public void TestBadgerFrames() {
		int cacheSize = 1; // Inmem_store's caches accept positive cacheSize only
		RResult2<BadgerStore, pub[]> initBadgerStore = initBadgerStore(cacheSize);
		BadgerStore store = initBadgerStore.result1;
		pub[] participants = initBadgerStore.result2;


		EventMessage[] events = new EventMessage[participants.length];
		Root[] roots = new Root[participants.length];
		for (int id = 0; id< participants.length; ++id) {
			pub p = participants[id];
			Event event = new Event(
					new byte[][]{String.format("%s_%d", p.hex.substring(0,5), 0).getBytes()},
					new InternalTransaction[]{},
					new BlockSignature[]{new BlockSignature("validator".getBytes(), 0, "r|s")},
					new String[]{"", ""},
					p.pubKey,
					0, null);
			event.Sign(p.privKey.getPrivate());
			events[id] = event.Message;
			roots[id] = new Root(id);
		}
		Frame frame = new Frame(1L, roots, events);

		// "Store Frame"
		error err = store.SetFrame(frame);
		assertNull("No error", err);
		RetResult<Frame> getFrame = store.GetFrame(frame.Round);
		Frame storedFrame = getFrame.result;
		err = getFrame.err;
		assertNull("No error", err);

		assertEquals("Frame and StoredFrame do not match", frame, storedFrame);

		removeBadgerStore(store);
	}
}