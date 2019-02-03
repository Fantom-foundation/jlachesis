package node;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.HashMap;

import org.apache.log4j.Level;
import org.junit.Test;

import autils.Appender;
import common.Hash32;
import common.RetResult;
import common.error;
import crypto.Utils;
import crypto.hash;
import peers.Peer;
import peers.Peers;
import poset.Event;
import poset.InmemStore;
import poset.WireEvent;

/**
 * Test for Core
 * @author qn
 *
 */
public class CoreTest {
	Core[] cores;
	Map<String,String> index;
	Map<Long, KeyPair> participantKeys;

	private void initCores(int n) {

		cores = null;
		index = new HashMap<String,String>();
		participantKeys = new HashMap<Long, KeyPair>();

		int cacheSize = 1000;

		Peers participants = new Peers();
		for (int i = 0; i < n; i++) {
			KeyPair key = crypto.Utils.GenerateECDSAKeyPair().result;
//			pubHex := String.format("0x%X", crypto.FromECDSAPub(&key.PublicKey));
			String pubHex = Utils.keyToHexString(key.getPublic());
			Peer peer = new Peer(pubHex, "");
			participants.AddPeer(peer);
			participantKeys.put(peer.GetID(), key);
		}

		Peer[] peers = participants.ToPeerSlice();
		for (int i = 0; i < peers.length; ++i) {
			Peer peer = peers[i];
			Core core = new Core((long) i,
				participantKeys.get(peer.GetID()),
				participants,
				new InmemStore(participants, cacheSize),
				null,
				common.TestUtils.NewTestLogger(this.getClass()));

			String selfParent = String.format("Root%d", peer.GetID());

			HashMap<String, Long> flagTable = new HashMap<String,Long>();
			flagTable.put(selfParent, 1L);

			// Create and save the first Event
			Event initialEvent = new Event(null,
				new poset.InternalTransaction[]{},
				null,
				new String[]{selfParent, ""}, core.PubKey(), 0, flagTable);
			error err = core.SignAndInsertSelfEvent(initialEvent);

			assertNull("No error when SignAndInsertSelfEvent", err);

			core.RunConsensus();

			cores = Appender.append(cores, core);
			index.put(String.format("e%d", i), core.head);
		}
	}

	/*
	|  e12  |
	|   | \ |
	|   |   e20
	|   | / |
	|   /   |
	| / |   |
	e01 |   |
	| \ |   |
	e0  e1  e2
	0   1   2
	*/
	private void initPoset(Core[] cores, Map<Long, KeyPair> keys,
		Map<String,String> index, int participant) {
		error err;
		for (int i = 0; i < cores.length; i++) {
			if (i != participant) {
				RetResult<Event> getEvent = cores[i].GetEvent(index.get(String.format("e%d", i)));
				Event event = getEvent.result;
				err = getEvent.err;
				assertNull("No error getEvent", err);

				err = cores[participant].InsertEvent(event, true);
				assertNull("No error inserting " + getName(index, event.Hex()), err);
			}
		}

		// Get flag tables from parents
		RetResult<Event> getEvent = cores[0].poset.Store.GetEvent(index.get("e0"));
		Event event0 = getEvent.result;
		err = getEvent.err;
		assertNull("No error to get parent", err);

		RetResult<Event> getEventCall1 = cores[0].poset.Store.GetEvent(index.get("e1"));

		Event event1 = getEventCall1.result;
		err = getEventCall1.err;
		assertNull("No error when get parent", err);

		Map<String, Long> event1ft = event1.GetFlagTable().result;
		Map<String, Long> event01ft = event0.MergeFlagTable(event1ft).result;

		Event event01 = new Event(new byte[][]{},
			new poset.InternalTransaction[]{},
			null,
			new String[]{index.get("e0"), index.get("e1")}, // e0 and e1
			cores[0].PubKey(), 1, event01ft);

		err = insertEvent(cores, keys, index, event01, "e01", participant,
				Hash32.Hash32(cores[0].pubKey));
		assertNull("No error inserting e01", err);

		// Get flag tables from parents
		RetResult<Event> getEventCall2 = cores[2].poset.Store.GetEvent(index.get("e2"));
		Event event2 = getEventCall2.result;
		err = getEventCall2.err;
		assertNull("No error get parent", err);

		Map<String, Long> event20ft = event2.MergeFlagTable(event01ft).result;

		Event event20 = new Event(new byte[][]{},
			new poset.InternalTransaction[]{},
			null,
			new String[]{index.get("e2"), index.get("e01")}, // e2 and e01
			cores[2].PubKey(), 1, event20ft);

		err = insertEvent(cores, keys, index, event20, "e20", participant,
				Hash32.Hash32(cores[2].pubKey));
		assertNull("No error inserting e20", err);

		Map<String, Long> event12ft = event1.MergeFlagTable(event20ft).result;

		Event event12 = new Event(new byte[][]{},
			new poset.InternalTransaction[]{},
			null,
			new String[]{index.get("e1"), index.get("e20")}, // e1 and e20
			cores[1].PubKey(), 1, event12ft);
		err = insertEvent(cores, keys, index, event12, "e12", participant,
				Hash32.Hash32(cores[1].pubKey));
		assertNull("No error inserting e12", err);
	}

	private error insertEvent(Core[] cores, Map<Long,KeyPair> keys,
		Map<String,String> index, Event event, String name, int participant,
		long creator)  {
		error err;
		if (participant == creator) {
			err = cores[participant].SignAndInsertSelfEvent(event);
			if (err != null) {
				return err;
			}
			// event is not signed because passed by value
			index.put(name, cores[participant].head);
		} else {
			event.Sign(keys.get(creator).getPrivate());
			err = cores[participant].InsertEvent(event, true);
			if (err != null) {
				return err;
			}
			index.put(name, event.Hex());
		}
		return null;
	}

	private void checkHeights(Core[] cores, Map<String,Long>[] expectedHeights) {
		for (int i = 0; i< cores.length; ++i) {
			Core core = cores[i];
			Map<String, Long> heights = core.Heights();
			assertEquals(String.format("Cores[%d].Heights() should match",
					i), expectedHeights[i], heights);
		}
	}

	//@Test
	public void TestEventDiff() {
		initCores(3);

		initPoset(cores, participantKeys, index, 0);

		/*
		  P0 knows

		  |  e12  |
		  |   | \ |
		  |   |   e20
		  |   | / |
		  |   /   |
		  | / |   |
		  e01 |   |        P1 knows
		  | \ |   |
		  e0  e1  e2       |   e1  |
		  0   1   2        0   1   2
		*/

		Map<Long,Long> knownBy1 = cores[1].KnownEvents();
		RetResult<poset.Event[]> eventDiff = cores[0].EventDiff(knownBy1);
		Event[] unknownBy1 = eventDiff.result;
		error err = eventDiff.err;
		assertNull("No error when event diff", err);

		assertEquals("length of unknown should be 5", 5, unknownBy1.length);

		String[] expectedOrder = new String[]{"e0", "e2", "e01", "e20", "e12"};
		for (int i =0; i< unknownBy1.length; ++i) {
			Event e = unknownBy1[i];
			String name = getName(index, e.Hex());
			assertEquals(String.format("element %d should match",
				i), expectedOrder[i], name);
		}

	}

//	@Test
	public void TestSync() {
		initCores(3);

		/*
		   core 0           core 1          core 2

		   e0  |   |        |   e1  |       |   |   e2
		   0   1   2        0   1   2       0   1   2
		*/

		HashMap<String,Long>[] expectedHeights = new HashMap[3];

		HashMap<String, Long> temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 1L);
		temp.put(cores[1].hexID, 0L);
		temp.put(cores[2].hexID, 0L);
		expectedHeights[0] = temp;

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 0L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 0L);
		expectedHeights[1] = temp;


		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 0L);
		temp.put(cores[1].hexID, 0L);
		temp.put(cores[2].hexID, 1L);
		expectedHeights[2] = temp;

		checkHeights(cores, expectedHeights);

		// core 1 is going to tell core 0 everything it knows
		error err = synchronizeCores(cores, 1, 0, new byte[][]{});
		assertNull("no error synchronize core", err);

		/*
		   core 0           core 1          core 2

		   e01 |   |
		   | \ |   |
		   e0  e1  |        |   e1  |       |   |   e2
		   0   1   2        0   1   2       0   1   2
		*/

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 2L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 0L);
		expectedHeights[0] = temp;


		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 0L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 0L);
		expectedHeights[1] = temp;


		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 0L);
		temp.put(cores[1].hexID, 0L);
		temp.put(cores[2].hexID, 1L);
		expectedHeights[2] = temp;

		checkHeights(cores, expectedHeights);

		Map<Long,Long> knownBy0 = cores[0].KnownEvents();
		long k = knownBy0.get(Hash32.Hash32(cores[0].pubKey));
		assertEquals("core 0 should have last-index 1 for core 0, not %d", 1, k);

		k = knownBy0.get(Hash32.Hash32(cores[1].pubKey));
		assertEquals("core 0 should have last-index 0 for core 1, not %d", 0, k);

		k = knownBy0.get(Hash32.Hash32(cores[2].pubKey));
		assertEquals("core 0 should have last-index -1 for core 2, not %d", -1, k);

		Event core0Head = cores[0].GetHead().result;
		assertEquals("core 0 head self-parent should be e0", core0Head.SelfParent(), cores[0].GetHead());

		assertEquals("core 0 head other-parent should be e1", core0Head.OtherParent(), index.get("e1"));

		assertNotNull("flag table is not null", core0Head.getMessage().GetFlagTable());

		index.put("e01", core0Head.Hex());

		// core 0 is going to tell core 2 everything it knows
		err = synchronizeCores(cores, 0, 2, new byte[][]{});
		assertNull("No error when core 0 synchronize to core 2", err);

		/*

		   core 0           core 1          core 2

		                                    |   |  e20
		                                    |   | / |
		                                    |   /   |
		                                    | / |   |
		   e01 |   |                        e01 |   |
		   | \ |   |                        | \ |   |
		   e0  e1  |        |   e1  |       e0  e1  e2
		   0   1   2        0   1   2       0   1   2
		*/

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 2L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 0L);
		expectedHeights[0] = temp;

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 0L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 0L);
		expectedHeights[1] = temp;

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 2L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 1L);
		expectedHeights[2] = temp;

		checkHeights(cores, expectedHeights);

		Map<Long, Long> knownBy2 = cores[2].KnownEvents();

		k = knownBy2.get(Hash32.Hash32(cores[0].pubKey));
		assertEquals("core 2 should have last-index 1 for core 0, not %d", 1, k);

		k = knownBy2.get(Hash32.Hash32(cores[1].pubKey));
		assertEquals("core 2 should have last-index 0 core 1, not %d", 0, k);

		k = knownBy2.get(Hash32.Hash32(cores[2].pubKey));
		assertEquals("core 2 should have last-index 1 for core 2, not %d", 1, k);

		Event core2Head = cores[2].GetHead().result;
		assertEquals("core 2 head self-parent should be e2", core2Head.SelfParent(), index.get("e2"));

		assertEquals("core 2 head other-parent should be e01", core2Head.OtherParent(),index.get("e01"));


		index.put("e20", core2Head.Hex());

		// core 2 is going to tell core 1 everything it knows
		err = synchronizeCores(cores, 2, 1, new byte[][]{});
		assertNull("No error when core 2 synchronize to core 1", err);

		/*
		   core 0           core 1          core 2

		                    |  e12  |
		                    |   | \ |
		                    |   |  e20      |   |  e20
		                    |   | / |       |   | / |
		                    |   /   |       |   /   |
		                    | / |   |       | / |   |
		   e01 |   |        e01 |   |       e01 |   |
		   | \ |   |        | \ |   |       | \ |   |
		   e0  e1  |        e0  e1  e2      e0  e1  e2
		   0   1   2        0   1   2       0   1   2
		*/

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 2L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 0L);
		expectedHeights[0] = temp;

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 2L);
		temp.put(cores[1].hexID, 2L);
		temp.put(cores[2].hexID, 2L);
		expectedHeights[1] = temp;

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 2L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 2L);
		expectedHeights[2] = temp;

		checkHeights(cores, expectedHeights);

		Map<Long, Long> knownBy1 = cores[1].KnownEvents();
		k = knownBy1.get(Hash32.Hash32(cores[0].pubKey));
		assertEquals("core 1 should have last-index 1 for core 0, not %d", 1, k);

		k = knownBy1.get(Hash32.Hash32(cores[1].pubKey));
		assertEquals("core 1 should have last-index 1 for core 1, not %d", 1, k);

		k = knownBy1.get(Hash32.Hash32(cores[2].pubKey));
		assertEquals("core 1 should have last-index 1 for core 2, not %d", 1, k);

		Event core1Head = cores[1].GetHead().result;
		assertEquals("core 1 head self-parent should be e1", core1Head.SelfParent(), index.get("e1"));

		assertEquals("core 1 head other-parent should be e20", core1Head.OtherParent(), index.get("e20"));

		index.put("e12", core1Head.Hex());
	}

	private void checkInDegree(Core[] cores, Map<String,Long>[] expectedInDegree) {
		for (int i= 0; i< cores.length; ++i) {
			Core core = cores[i];
			Map<String, Long> inDegrees = core.InDegrees();
			assertEquals(String.format("Cores[%d].InDegrees() should match",
				i), expectedInDegree[i], inDegrees);
		}
	}

	//@Test
	public void TestInDegrees() {
		initCores(3);

		/*
		   core 0           core 1          core 2

		   e0  |   |        |   e1  |       |   |   e2
		   0   1   2        0   1   2       0   1   2
		*/

		// core 1 is going to tell core 0 everything it knows
		error err = synchronizeCores(cores, 1, 0, new byte[][]{});
		assertNull("No error synchronizing core 1 to core 0", err);

		/*
		   core 0           core 1          core 2

		   e01 |   |
		   | \ |   |
		   e0  e1  |        |   e1  |       |   |   e2
		   0   1   2        0   1   2       0   1   2
		*/

		HashMap<String,Long>[] expectedHeights = new HashMap[3];

		HashMap<String,Long> temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 2L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 0L);
		expectedHeights[0] = temp;

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 0L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 0L);
		expectedHeights[1] = temp;

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 0L);
		temp.put(cores[1].hexID, 0L);
		temp.put(cores[2].hexID, 1L);
		expectedHeights[2] = temp;

		checkHeights(cores, expectedHeights);

		HashMap<String,Long>[] expectedInDegree = new HashMap[3];

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 0L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 0L);
		expectedInDegree[0] = temp;

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 0L);
		temp.put(cores[1].hexID, 0L);
		temp.put(cores[2].hexID, 0L);
		expectedInDegree[1] = temp;

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 0L);
		temp.put(cores[1].hexID, 0L);
		temp.put(cores[2].hexID, 0L);
		expectedInDegree[2] = temp;

		checkInDegree(cores, expectedInDegree);

		// core 1 is going to tell core 2 everything it knows
		err = synchronizeCores(cores, 1, 2, new byte[][]{});
		assertNull("No error synchronizing core 1 to core 2", err);

		/*
		   core 0           core 1          core 2

		   e01 |   |                        |   |  e21
		   | \ |   |                        |   | / |
		   e0  e1  |        |   e1  |       e0  e1  e2
		   0   1   2        0   1   2       0   1   2
		*/

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 2L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 0L);
		expectedHeights[0] = temp;

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 0L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 0L);
		expectedHeights[1] = temp;

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 0L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 2L);
		expectedHeights[2] = temp;

		checkHeights(cores, expectedHeights);

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 0L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 0L);
		expectedInDegree[0] = temp;

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 0L);
		temp.put(cores[1].hexID, 0L);
		temp.put(cores[2].hexID, 0L);
		expectedInDegree[1] = temp;


		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 0L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 0L);
		expectedInDegree[2] = temp;

		checkInDegree(cores, expectedInDegree);

		// core 0 is going to tell core 2 everything it knows
		err = synchronizeCores(cores, 0, 2, new byte[][]{});
		assertNull("No error synchronizing core 0 to core 2", err);

		/*

		   core 0           core 1          core 2

		                                    |   |  e20
		                                    |   | / |
		                                    |   /   |
		                                    | / |   |
		   e01 |   |                        e01 |  e21
		   | \ |   |                        | \ | / |
		   e0  e1  |        |   e1  |       e0  e1  e2
		   0   1   2        0   1   2       0   1   2
		*/
		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 2L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 0L);
		expectedHeights[0] = temp;

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 0L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 0L);
		expectedHeights[1] = temp;

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 2L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 3L);
		expectedHeights[2] = temp;

		checkHeights(cores, expectedHeights);

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 0L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 0L);
		expectedInDegree[0] = temp;

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 0L);
		temp.put(cores[1].hexID, 0L);
		temp.put(cores[2].hexID, 0L);
		expectedInDegree[1] = temp;


		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 1L);
		temp.put(cores[1].hexID, 2L);
		temp.put(cores[2].hexID, 0L);
		expectedInDegree[2] = temp;

		checkInDegree(cores, expectedInDegree);

		// core 2 is going to tell core 1 everything it knows
		err = synchronizeCores(cores, 2, 1, new byte[][]{});
		assertNull("No error synchronizing core 2 to core 1", err);

		/*

		   core 0           core 1          core 2

		                    |  e12  |
		                    |   | \ |
		                    |   |  e20      |   |  e20
		                    |   | / |       |   | / |
		                    |   /   |       |   /   |
		                    | / |   |       | / |   |
		   e01 |   |        e01 |  e21      e01 |  e21
		   | \ |   |        | \ | / |       | \ | / |
		   e0  e1  |        e0  e1  e2      e0  e1  e2
		   0   1   2        0   1   2       0   1   2
		*/
		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 2L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 0L);
		expectedHeights[0] = temp;

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 2L);
		temp.put(cores[1].hexID, 2L);
		temp.put(cores[2].hexID, 3L);
		expectedHeights[1] = temp;

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 2L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 3L);
		expectedHeights[2] = temp;

		checkHeights(cores, expectedHeights);

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 0L);
		temp.put(cores[1].hexID, 1L);
		temp.put(cores[2].hexID, 0L);
		expectedHeights[0] = temp;

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 1L);
		temp.put(cores[1].hexID, 0L);
		temp.put(cores[2].hexID, 1L);
		expectedHeights[1] = temp;

		temp = new HashMap<String,Long>();
		temp.put(cores[0].hexID, 1L);
		temp.put(cores[1].hexID, 2L);
		temp.put(cores[2].hexID, 0L);
		expectedHeights[2] = temp;

		checkInDegree(cores, expectedInDegree);
	}

	/*
	h0  |   h2
	| \ | / |
	|   h1  |
	|  /|   |--------------------
	g02 |   | R2
	| \ |   |
	|   \   |
	|   | \ |
	|   |  g21
	|   | / |
	|  g10  |
	| / |   |
	g0  |   g2
	| \ | / |
	|   g1  |
	|  /|   |--------------------
	f02 |   | R1
	| \ |   |
	|   \   |
	|   | \ |
	|   |  f21
	|   | / |
	|  f10  |
	| / |   |
	f0  |   f2
	| \ | / |
	|   f1  |
	|  /|   |--------------------
	e02 |   | R0 Consensus
	| \ |   |
	|   \   |
	|   | \ |
	|   |  e21
	|   | / |
	|  e10  |
	| / |   |
	e0  e1  e2
	0   1    2
	*/
	class play {
		int from;
		int to;
		byte[][] payload;
		public play(int from, int to, byte[][] payload) {
			super();
			this.from = from;
			this.to = to;
			this.payload = payload;
		}
	}

	public Core[] initConsensusPoset() {
		initCores(3);
		play[] playbook = new play[]{
			new play(0, 1, new  byte[][]{"e10".getBytes()}),
			new play(1, 2, new  byte[][]{"e21".getBytes()}),
			new play(2, 0, new  byte[][]{"e02".getBytes()}),
			new play(0, 1, new  byte[][]{"f1".getBytes()}),
			new play(1, 0, new  byte[][]{"f0".getBytes()}),
			new play(1, 2, new  byte[][]{"f2".getBytes()}),
			new play(0, 1, new  byte[][]{"f10".getBytes()}),
			new play(1, 2, new  byte[][]{"f21".getBytes()}),
			new play(2, 0, new  byte[][]{"f02".getBytes()}),
			new play(0, 1, new  byte[][]{"g1".getBytes()}),
			new play(1, 0, new  byte[][]{"g0".getBytes()}),
			new play(1, 2, new  byte[][]{"g2".getBytes()}),
			new play(0, 1, new  byte[][]{"g10".getBytes()}),
			new play(1, 2, new  byte[][]{"g21".getBytes()}),
			new play(2, 0, new  byte[][]{"g02".getBytes()}),
			new play(0, 1, new  byte[][]{"h1".getBytes()}),
			new play(1, 0, new  byte[][]{"h0".getBytes()}),
			new play(1, 2, new  byte[][]{"h2".getBytes()})
		};

		for (play play : playbook) {
			error err = syncAndRunConsensus(
					cores, play.from, play.to, play.payload);
			assertNull("No error synAndRunConsensus", err);
		}

		return cores;
	}

////	@Test
//	public void TestConsensus() {
//		cores = initConsensusPoset();
//
//		int l = cores[0].GetConsensusEvents().length;
//		assertEquals("length of consensus should match", 4, l);
//
//		core0Consensus = cores[0].GetConsensusEvents();
//		core1Consensus = cores[1].GetConsensusEvents();
//		core2Consensus = cores[2].GetConsensusEvents();
//
//		for i, e := range core0Consensus {
//			if core1Consensus[i] != e {
//				t.Fatalf("core 1 consensus[%d] does not match core 0's", i);
//			}
//			if core2Consensus[i] != e {
//				t.Fatalf("core 2 consensus[%d] does not match core 0's", i);
//			}
//		}
//	}

////	@Test
//	public void TestOverSyncLimit() {
//		cores = initConsensusPoset();
//
//		// positive
//		known := map[int64]int64{
//			int64(Hash32.Hash32(cores[0].pubKey)): 1,
//			int64(Hash32.Hash32(cores[1].pubKey)): 1,
//			int64(Hash32.Hash32(cores[2].pubKey)): 1,
//		}
//
//		syncLimit := int64(10)
//
//		if !cores[0].OverSyncLimit(known, syncLimit) {
//			t.Fatalf("OverSyncLimit(%v, %v) should return true", known, syncLimit)
//		}
//
//		// negative
//		known = map[int64]int64{
//			int64(Hash32.Hash32(cores[0].pubKey)): 6,
//			int64(Hash32.Hash32(cores[1].pubKey)): 6,
//			int64(Hash32.Hash32(cores[2].pubKey)): 6,
//		}
//
//		if cores[0].OverSyncLimit(known, syncLimit) {
//			t.Fatalf("OverSyncLimit(%v, %v) should return false", known, syncLimit)
//		}
//
//		// edge
//		known = map[int64]int64{
//			int64(Hash32.Hash32(cores[0].pubKey)): 2,
//			int64(Hash32.Hash32(cores[1].pubKey)): 3,
//			int64(Hash32.Hash32(cores[2].pubKey)): 3,
//		}
//		if cores[0].OverSyncLimit(known, syncLimit) {
//			t.Fatalf("OverSyncLimit(%v, %v) should return false", known, syncLimit)
//		}
//
//	}

	/*
	    |   |   |   |-----------------
		|   w31 |   | R3
		|	| \ |   |
	    |   |  w32  |
	    |   |   | \ |
	    |   |   |  w33
	    |   |   | / |-----------------
	    |   |  g21  | R2
		|   | / |   |
		|  w21  |   |
		|	| \ |   |
	    |   |  w22  |
	    |   |   | \ |
	    |   |   |  w23
	    |   |   | / |-----------------
	    |   |  f21  | R1
		|   | / |   | LastConsensusRound
		|  w11  |   |
		|	| \ |   |
	    |   |   \   |
	    |   |   | \ |
		|   |   |  w13
		|   |   | / |
	   FSE  |  w12  | FSE is only added after FastForward
	    |\  | / |   | -----------------
	    |  e13  |   | R0
		|	| \ |   |
	    |   |   \   |
	    |   |   | \ |
	    |   |   |  e32
	    |   |   | / |
	    |   |  e21  | All Events in Round 0 are Consensus Events.
	    |   | / |   |
	    |   e1  e2  e3
	    0	1	2	3
	*/
	private void initFFPoset(Core[] cores) {
		play[] playbook = new play[]{
			new play(1, 2, new byte[][]{"e21".getBytes()}),
			new play(2, 3, new byte[][]{"e32".getBytes()}),
			new play(3, 1, new byte[][]{"e13".getBytes()}),
			new play(1, 2, new byte[][]{"w12".getBytes()}),
			new play(2, 3, new byte[][]{"w13".getBytes()}),
			new play(3, 1, new byte[][]{"w11".getBytes()}),
			new play(1, 2, new byte[][]{"f21".getBytes()}),
			new play(2, 3, new byte[][]{"w23".getBytes()}),
			new play(3, 2, new byte[][]{"w22".getBytes()}),
			new play(2, 1, new byte[][]{"w21".getBytes()}),
			new play(1, 2, new byte[][]{"g21".getBytes()}),
			new play(2, 3, new byte[][]{"w33".getBytes()}),
			new play(3, 2, new byte[][]{"w32".getBytes()}),
			new play(2, 1, new byte[][]{"w31".getBytes()}),
		};

		for (int k = 0; k< playbook.length; ++k) {
			play play = playbook[k];
			error err = syncAndRunConsensus(cores, play.from, play.to, play.payload);
			assertNull(String.format("No error when syncAndRunConsensus play %d", k), err);
		}
	}

//	//@Test
//	public void TestConsensusFF() {
//		initCores(4);
//		initFFPoset(cores);
//
//		r = cores[1].GetLastConsensusRoundIndex();
//		if  r == null || *r != 2 {
//			disp := "null"
//			if r != null {
//				disp = strconv.FormatInt(*r, 10)
//			}
//			t.Fatalf("Cores[1] last consensus Round should be 1, not %s", disp)
//		}
//
//		if l := len(cores[1].GetConsensusEvents()); l != 7 {
//			t.Fatalf("Node 1 should have 7 consensus events, not %d", l)
//		}
//
//		core1Consensus := cores[1].GetConsensusEvents()
//		core2Consensus := cores[2].GetConsensusEvents()
//		core3Consensus := cores[3].GetConsensusEvents()
//
//		for i, e := range core1Consensus {
//			if core2Consensus[i] != e {
//				t.Fatalf("Node 2 consensus[%d] does not match Node 1's", i)
//			}
//			if core3Consensus[i] != e {
//				t.Fatalf("Node 3 consensus[%d] does not match Node 1's", i)
//			}
//		}
//	}
//
//	//@Test
//	public void TestCoreFastForward() {
//		cores, _, _ := initCores(4, t)
//		initFFPoset(cores, t)
//
//		t.Run("Test no Anchor", public() {
//			// Test no anchor block
//			_, _, err := cores[1].GetAnchorBlockWithFrame()
//			if err == null {
//				t.Fatal("GetAnchorBlockWithFrame should throw an error" +
//					" because there is no anchor block yet")
//			}
//		})
//
//		block0, err := cores[1].poset.Store.GetBlock(0)
//		if err != null {
//			t.Fatal(err)
//		}
//
//		// collect signatures
//		signatures := make([]poset.BlockSignature, 3)
//		for k, c := range cores[1:] {
//			b, err := c.poset.Store.GetBlock(0)
//			if err != null {
//				t.Fatal(err)
//			}
//			sig, err := c.SignBlock(b)
//			if err != null {
//				t.Fatal(err)
//			}
//			signatures[k] = sig
//		}
//
//		t.Run("Test not enough signatures", public() {
//			// Append only 1 signatures
//			if err := block0.SetSignature(signatures[0]); err != null {
//				t.Fatal(err)
//			}
//
//			// Save Block
//			if err := cores[1].poset.Store.SetBlock(block0); err != null {
//				t.Fatal(err)
//			}
//			// Assign AnchorBlock
//			cores[1].poset.AnchorBlock = new(int64)
//			*cores[1].poset.AnchorBlock = 0
//
//			// Now the publiction should find an AnchorBlock
//			block, frame, err := cores[1].GetAnchorBlockWithFrame()
//			if err != null {
//				t.Fatal(err)
//			}
//
//			err = cores[0].FastForward(cores[1].hexID, block, frame)
//			// We should get an error because AnchorBlock doesnt contain enough
//			// signatures
//			if err == null {
//				t.Fatal("FastForward should throw an error because the Block" +
//					" does not contain enough signatures")
//			}
//		})
//
//		t.Run("Test positive", public() {
//			// Append the 2nd and 3rd signatures
//			for i := 1; i < 3; i++ {
//				if err := block0.SetSignature(signatures[i]); err != null {
//					t.Fatal(err)
//				}
//			}
//
//			// Save Block
//			if err := cores[1].poset.Store.SetBlock(block0); err != null {
//				t.Fatal(err)
//			}
//
//			block, frame, err := cores[1].GetAnchorBlockWithFrame()
//			if err != null {
//				t.Fatal(err)
//			}
//
//			err = cores[0].FastForward(cores[1].hexID, block, frame)
//			if err != null {
//				t.Fatal(err)
//			}
//
//			knownBy0 := cores[0].KnownEvents()
//			if err != null {
//				t.Fatal(err)
//			}
//
//			expectedKnown := map[int64]int64{
//				int64(Hash32.Hash32(cores[0].pubKey)): -1,
//				int64(Hash32.Hash32(cores[1].pubKey)): 0,
//				int64(Hash32.Hash32(cores[2].pubKey)): 1,
//				int64(Hash32.Hash32(cores[3].pubKey)): 0,
//			}
//
//			if !reflect.DeepEqual(knownBy0, expectedKnown) {
//				t.Fatalf("Cores[0].Known should be %v, not %v",
//					expectedKnown, knownBy0)
//			}
//
//			if r := cores[0].GetLastConsensusRoundIndex(); r == null || *r != 1 {
//				t.Fatalf("Cores[0] last consensus Round should be 1, not %v", r)
//			}
//
//			if lbi := cores[0].poset.Store.LastBlockIndex(); lbi != 0 {
//				t.Fatalf("Cores[0].poset.LastBlockIndex should be 0, not %d", lbi)
//			}
//
//			sBlock, err := cores[0].poset.Store.GetBlock(block.Index())
//			if err != null {
//				t.Fatalf("Error retrieving latest Block from reset poset: %v", err)
//			}
//			if !reflect.DeepEqual(sBlock.Body, block.Body) {
//				t.Fatalf("Blocks defer")
//			}
//
//			lastEventFrom0, _, err := cores[0].poset.Store.LastEventFrom(
//				cores[0].hexID)
//			if err != null {
//				t.Fatal(err)
//			}
//			if c0h := cores[0].Head(); c0h != lastEventFrom0 {
//				t.Fatalf("Head should be %s, not %s", lastEventFrom0, c0h)
//			}
//
//			if c0s := cores[0].Seq; c0s != -1 {
//				t.Fatalf("Seq should be %d, not %d", -1, c0s)
//			}
//
//		})
//
//	}

	public error synchronizeCores(Core[] cores, int from, int to, byte[][] payload) {
		Map<Long, Long> knownByTo = cores[to].KnownEvents();
		RetResult<Event[]> eventDiff = cores[from].EventDiff(knownByTo);
		Event[] unknownByTo = eventDiff.result;
		error err = eventDiff.err;
		if (err != null) {
			return err;
		}

		RetResult<WireEvent[]> toWire = cores[from].ToWire(unknownByTo);
		WireEvent[] unknownWire = toWire.result;
		err = toWire.err;
		if (err != null) {
			return err;
		}

		cores[to].AddTransactions(payload);

		return cores[to].Sync(unknownWire);
	}

	public error syncAndRunConsensus(Core[] cores, int from, int to, byte[][] payload)  {
		error err = synchronizeCores(cores, from, to, payload);
		if (err != null) {
			return err;
		}
		cores[to].RunConsensus();
		return null;
	}

	public String getName(Map<String,String> index, String hash) {
		for (String name : index.keySet()) {
			String h = index.get(name);
			if (h.equals(hash)) {
				return name;
			}
		}
		return String.format("%s not found", hash);
	}
}