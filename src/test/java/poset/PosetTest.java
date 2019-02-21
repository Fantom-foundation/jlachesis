package poset;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static poset.Event.rootSelfParent;

import java.io.File;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import autils.Appender;
import autils.Logger;
import common.RResult;
import common.error;
import peers.Peer;
import peers.Peers;
import poset.proto.Trilean;

/**
 * Test for Poset
 * @author qn
 *
 */
public class PosetTest {

	private static Logger logger = Logger.getLogger(PosetTest.class);

	private static String
		e0  = "e0",
		e1  = "e1",
		e2  = "e2",
		e10 = "e10",
		e21 = "e21",
		e02 = "e02",
		f1  = "f1",
		f0  = "f0",
		f2  = "f2",
		g1  = "g1",
		g0  = "g0",
		g2  = "g2",
		g10 = "g10",
		h0  = "h0",
		h2  = "h2",
		h10 = "h10",
		h21 = "h21",
		i1  = "i1",
		i0  = "i0",
		i2  = "i2",
		e01 = "e01",
		s20 = "s20",
		s10 = "s10",
		s00 = "s00",
		e20 = "e20",
		e12 = "e12",
		a   = "a",
		s11 = "s11",
		w00 = "w00",
		w01 = "w01",
		w02 = "w02",
		w03 = "w03",
		a23 = "a23",
		a00 = "a00",
		a12 = "a12",
		a10 = "a10",
		a21 = "a21",
		w13 = "w13",
		w12 = "w12",
		w11 = "w11",
		w10 = "w10",
		b21 = "b21",
		w23 = "w23",
		b00 = "b00",
		w21 = "w21",
		c10 = "c10",
		w22 = "w22",
		w20 = "w20",
		w31 = "w31",
		w32 = "w32",
		w33 = "w33",
		w30 = "w30",
		d13 = "d13",
		w40 = "w40",
		w41 = "w41",
		w42 = "w42",
		w43 = "w43",
		e23 = "e23",
		w51 = "w51",
		e32 = "e32",
		g13 = "g13",
		f01 = "f01",
		i32 = "i32",
		r0  = "r0",
		r1  = "r1",
		r2  = "r2",
		f2b = "f2b",
		g0x = "g0x",
		h0b = "h0b",
		j2  = "j2",
		j0  = "j0",
		j1  = "j1",
		k0  = "k0",
		k2  = "k2",
		k10 = "k10",
		l2  = "l2",
		l0  = "l0",
		l1  = "l1",
		m0  = "m0",
		m2  = "m2";

	static File currentDirectory = new File(new File(".").getAbsolutePath());

	int cacheSize = 100;
	int	n         = 3;
	String	badgerDir = currentDirectory.getAbsolutePath() + "test_data/badger";

	class TestNode {
		int ID;
		byte[] Pub;
		String PubHex;
		KeyPair Key;
		Event[] Events;

		public TestNode(KeyPair key, int id)  {
			Pub = crypto.Utils.FromECDSAPub(key.getPublic());
			ID = common.Hash32.Hash32(Pub);
			Key = key;
			PubHex = crypto.Utils.toHexString(Pub);
			Events = new Event[] {};
		}

		public void signAndAddEvent(Event event, String name, HashMap<String,String> index) {
			event.sign(Key.getPrivate());
			Events = Appender.append(Events, event);
			index.put(name, event.hex());
			orderedEvents = Appender.append(orderedEvents, event);
		}
	}


	// [event] => {lamportTimestamp, round}
	class tr {
		long t, r;

		tr(long t, long r) {
			super();
			this.t = t;
			this.r = r;
		}
	}

	class ancestryItem {
		String descendant, ancestor;
		boolean val;
		boolean err;
		public ancestryItem(String descendant, String ancestor, boolean val, boolean err) {
			super();
			this.descendant = descendant;
			this.ancestor = ancestor;
			this.val = val;
			this.err = err;
		}
	}

	class roundItem {
		String event;
		long round;
		public roundItem(String event, long round) {
			super();
			this.event = event;
			this.round = round;
		}
	}

	class Hierarchy {
		String ev, selfAncestor, ancestor;

		public Hierarchy(String ev, String selfAncestor, String ancestor) {
			super();
			this.ev = ev;
			this.selfAncestor = selfAncestor;
			this.ancestor = ancestor;
		}
	}

	class play {
		int to;
		long index;
		String selfParent;
		String otherParent;
		String name;
		byte[][] txPayload;
		BlockSignature[] sigPayload;
		String[] knownRoots;
		public play(int to, long index, String selfParent, String otherParent, String name, byte[][] txPayload,
				BlockSignature[] sigPayload, String[] knownRoots) {
			super();
			this.to = to;
			this.index = index;
			this.selfParent = selfParent;
			this.otherParent = otherParent;
			this.name = name;
			this.txPayload = txPayload;
			this.sigPayload = sigPayload;
			this.knownRoots = knownRoots;
		}
	}

	/* Initialisation publictions */
	TestNode[] nodes;
	HashMap<String,String> index;
	Event[] orderedEvents;
	Peers participants;
	Poset poset;

	public void initPosetNodes(int n)  {
		participants  = new Peers();
		orderedEvents = new Event[]{};
		nodes         = null;
		index         = new HashMap<String,String>();
		HashMap<String, KeyPair> keys = new HashMap<String,KeyPair>();

		for (int i = 0; i < n; i++) {
			KeyPair key = crypto.Utils.GenerateECDSAKeyPair().result;
			byte[] pub = crypto.Utils.FromECDSAPub(key.getPublic());
//			pubHex := fmt.Sprintf("0x%X", pub)
			String pubHex = crypto.Utils.toHexString(pub);
			participants.addPeer(new Peer(pubHex, ""));
			keys.put(pubHex, key);
		}

		Peer[] toPeerSlice = participants.toPeerSlice();
		for (int i =0; i < toPeerSlice.length; ++i) {
			Peer peer = toPeerSlice[i];
			nodes = Appender.append(nodes, new TestNode(keys.get(peer.getPubKeyHex()), i));
		}
	}

	public void playEvents(play[] plays, TestNode[] nodes, HashMap<String,String> index) {
		for (play p : plays) {
			HashMap<String, Long> ft = new HashMap<String,Long>();
			for (String root : p.knownRoots) {
				ft.put(index.get(root), 1L);
			}

			Event e = new Event(p.txPayload, null,
				p.sigPayload,
				new String[]{index.get(p.selfParent), index.get(p.otherParent)},
				nodes[p.to].Pub, p.index, ft);

			nodes[p.to].signAndAddEvent(e, p.name, index);
		}
	}

	public Poset createPoset( boolean db, Event[] orderedEvents,
			peers.Peers participants, Logger logger) {
		Store store;
		error err;
		if (db) {
			RResult<BadgerStore> newBadgerStore = BadgerStore.NewBadgerStore(participants, cacheSize, badgerDir);
			store = newBadgerStore.result;
			err = newBadgerStore.err;
			assertNull("No ERROR creating badger store", err);
		} else {
			store = new InmemStore(participants, cacheSize);
		}

		Poset poset = new Poset(participants, store, null, logger);

		for (int i = 0; i < orderedEvents.length; ++i) {
			Event ev = orderedEvents[i];
			err = poset.InsertEvent(ev, true);
			assertNull(String.format("No error when inserting event %d", i), err);
		}

		return poset;
	}

	public void  initPosetFull(play[] plays, boolean db, int n) {
		initPosetNodes(n);

		// Needed to have sorted nodes based on participants hash32
		Peer[] peerSlice = participants.toPeerSlice();

		for (int i = 0; i< peerSlice.length; ++i) {
			Peer peer = peerSlice[i];

			HashMap<String,Long> map = new HashMap<String,Long>();
			map.put(rootSelfParent(peer.getID()), 1L);
			Event event = new Event(null, null, null, new String[]{rootSelfParent(peer.getID()), ""},
				nodes[i].Pub, 0, map);
			nodes[i].signAndAddEvent(event, String.format("e%d", i), index);
		}

		playEvents(plays, nodes, index);

		poset = createPoset(db, orderedEvents, participants, logger);

		peerSlice = participants.toPeerSlice();
		// Add reference to each participants' root event
		for (int i = 0; i< peerSlice.length; ++i) {
			Peer peer = peerSlice[i];
			RResult<Root> getRoot = poset.Store.getRoot(peer.getPubKeyHex());
			Root root = getRoot.result;
			error err  = getRoot.err;
			assertNull("No error", err);
			index.put("r"+i, root.SelfParent.Hash);
		}
	}

	/*  */

	/*
	|  e12  |
	|   | \ |
	|  s10 e20
	|   | / |
	|   /   |
	| / |   |
	s00 |  s20
	|   |   |
	e01 |   |
	| \ |   |
	e0  e1  e2
	|   |   |
	r0  r1  r2
	0   1   2
	*/
	public void initPoset() {
		play[] plays = new play[]{
			new play(0, 1, e0, e1, e01, null, null, new String[]{e0, e1}),
			new play(2, 1, e2, "", s20, null, null, new String[]{e2}),
			new play(1, 1, e1, "", s10, null, null, new String[]{e1}),
			new play(0, 2, e01, "", s00, null, null, new String[]{e0, e1}),
			new play(2, 2, s20, s00, e20, null, null, new String[]{e0, e1, e2}),
			new play(1, 2, s10, e20, e12, null, null, new String[]{e0, e1, e2}),
		};

		initPosetFull(plays, false, n);

		for (int i = 0; i< orderedEvents.length; ++i) {
			Event ev = orderedEvents[i];
			error err = poset.Store.setEvent(ev);
			assertNull(String.format("No error for setting event at %d", i), err);
		}
	}

	@Test
	public void testAncestor() {
		initPoset();

		ancestryItem[] expected = new  ancestryItem[]{
			// first generation
			new ancestryItem(e01, e0, true, false),
			new ancestryItem(e01, e1, true, false),
			new ancestryItem(s00, e01, true, false),
			new ancestryItem(s20, e2, true, false),
			new ancestryItem(e20, s00, true, false),
			new ancestryItem(e20, s20, true, false),
			new ancestryItem(e12, e20, true, false),
			new ancestryItem(e12, s10, true, false),
			// second generation
			new ancestryItem(s00, e0, true, false),
			new ancestryItem(s00, e1, true, false),
			new ancestryItem(e20, e01, true, false),
			new ancestryItem(e20, e2, true, false),
			new ancestryItem(e12, e1, true, false),
			new ancestryItem(e12, s20, true, false),
			// third generation
			new ancestryItem(e20, e0, true, false),
			new ancestryItem(e20, e1, true, false),
			new ancestryItem(e20, e2, true, false),
			new ancestryItem(e12, e01, true, false),
			new ancestryItem(e12, e0, true, false),
			new ancestryItem(e12, e1, true, false),
			new ancestryItem(e12, e2, true, false),
			// false positive
			new ancestryItem(e01, e2, false, false),
			new ancestryItem(s00, e2, false, false),
			new ancestryItem(e0, "", false, true),
			new ancestryItem(s00, "", false, true),
			new ancestryItem(e12, "", false, true),
			// root events
			new ancestryItem(e1, r1, true, false),
			new ancestryItem(e20, r1, true, false),
			new ancestryItem(e12, r0, true, false),
			new ancestryItem(s20, r1, false, false),
			new ancestryItem(r0, r1, false, false)
		};

		for (ancestryItem exp : expected) {
			RResult<Boolean> ancestorCall = poset.ancestor(index.get(exp.descendant), index.get(exp.ancestor));
			boolean a = ancestorCall.result;
			error err = ancestorCall.err;
			assertNull(String.format("No error when computing ancestor(%s, %s)",
				exp.descendant, exp.ancestor), err);
			assertEquals(String.format("ancestor(%s, %s) should match",
				exp.descendant, exp.ancestor), exp.val, a);
		}
	}

	@Test
	public void testSelfAncestor() {
		initPoset();

		ancestryItem[] expected = new ancestryItem[]{
			// 1 generation
			new ancestryItem(e01, e0, true, false),
			new ancestryItem(s00, e01, true, false),
			// 1 generation false negative
			new ancestryItem(e01, e1, false, false),
			new ancestryItem(e12, e20, false, false),
			new ancestryItem(s20, e1, false, false),
			new ancestryItem(s20, "", false, true),
			// 2 generations
			new ancestryItem(e20, e2, true, false),
			new ancestryItem(e12, e1, true, false),
			// 2 generations false negatives
			new ancestryItem(e20, e0, false, false),
			new ancestryItem(e12, e2, false, false),
			new ancestryItem(e20, e01, false, false),
			// roots
			new ancestryItem(e20, r2, true, false),
			new ancestryItem(e1, r1, true, false),
			new ancestryItem(e1, r0, false, false),
			new ancestryItem(r1, r0, false, false)
		};

		for (ancestryItem exp : expected) {
			RResult<Boolean> selfAncestorCall = poset.selfAncestor(index.get(exp.descendant), index.get(exp.ancestor));
			boolean a = selfAncestorCall.result;
			error err = selfAncestorCall.err;
			assertNull(String.format("No Error when computing selfAncestor(%s, %s)",
				exp.descendant, exp.ancestor), err);
			assertEquals(String.format("selfAncestor(%s, %s) should match",
				exp.descendant, exp.ancestor), exp.val, a);
		}
	}

	@Test
	public void testSee() {
		initPoset();

		ancestryItem[] expected = new ancestryItem[]{
			new ancestryItem(e01, e0, true, false),
			new ancestryItem(e01, e1, true, false),
			new ancestryItem(e20, e0, true, false),
			new ancestryItem(e20, e01, true, false),
			new ancestryItem(e12, e01, true, false),
			new ancestryItem(e12, e0, true, false),
			new ancestryItem(e12, e1, true, false),
			new ancestryItem(e12, s20, true, false)
		};

		for (ancestryItem exp : expected) {
			RResult<Boolean> see = poset.see(index.get(exp.descendant), index.get(exp.ancestor));
			boolean a = see.result;
			error err = see.err;
			assertNull(String.format("No Error computing see(%s, %s)",
				exp.descendant, exp.ancestor), err);
			assertEquals(String.format("see(%s, %s) should equal",
				exp.descendant, exp.ancestor), exp.val, a);
		}
	}

	@Test
	public void testLamportTimestamp() {
		initPoset();

		HashMap<String,Long> expectedTimestamps = new HashMap<String,Long>();
		expectedTimestamps.put(e0,  0L);
		expectedTimestamps.put(e1,  0L);
		expectedTimestamps.put(e2,  0L);
		expectedTimestamps.put(e01, 1L);
		expectedTimestamps.put(s10, 1L);
		expectedTimestamps.put(s20, 1L);
		expectedTimestamps.put(s00, 2L);
		expectedTimestamps.put(e20, 3L);
		expectedTimestamps.put(e12, 4L);

		for (String e : expectedTimestamps.keySet()) {
			long ets = expectedTimestamps.get(e);
			RResult<Long> lamportTimestampCall = poset.lamportTimestamp(index.get(e));
			long ts = lamportTimestampCall.result;
			error err = lamportTimestampCall.err;
			assertNull(String.format("No Error computing lamportTimestamp(%s)", e), err);
			assertEquals(String.format("%s LamportTimestamp should equal", e), ets, ts);
		}
	}

	/*
	|    |    e20
	|    |   / |
	|    | /   |
	|    /     |
	|  / |     |
	e01  |     |
	| \  |     |
	|   \|     |
	|    |\    |
	|    |  \  |
	e0   e1 (a)e2
	0    1     2

	Node 2 Forks; events a and e2 are both created by node2, they are not
	self-parent sand yet they are both ancestors of event e20
	*/

	@Test
	public void testFork() {
		this.index = new HashMap<String,String>();
		this.nodes = null;
		this.participants = new Peers();

		for (int i = 0; i < n; i++) {
			KeyPair key = crypto.Utils.GenerateECDSAKeyPair().result;
			TestNode node = new TestNode(key, i);
			nodes = Appender.append(nodes, node);
			participants.addPeer(new Peer(node.PubHex, ""));
		}

		InmemStore store = new InmemStore(participants, cacheSize);
		poset = new Poset(participants, store, null, logger);

		for (int i =0; i< nodes.length; ++i) {
			TestNode node = nodes[i];
			Event event = new Event(null, null, null, new String[]{"", ""}, node.Pub, 0, null);
			event.sign(node.Key.getPrivate());
			index.put(String.format("e%d", i), event.hex());
			poset.InsertEvent(event, true);
		}

		//a and e2 need to have different hashes
		Event eventA = new Event(new byte[][]{"yo".getBytes()}, null, null, new String[]{"", ""},
				nodes[2].Pub, 0, null);
		eventA.sign(nodes[2].Key.getPrivate());
		index.put("a", eventA.hex());
		error err = poset.InsertEvent(eventA, true);
		assertNotNull("InsertEvent should return error for 'a'", err);

		Event event01 = new Event(null, null, null,
			new String[]{index.get(e0), index.get(a)}, //e0 and a
			nodes[0].Pub, 1, null);
		event01.sign(nodes[0].Key.getPrivate());
		index.put(e01, event01.hex());
		err = poset.InsertEvent(event01, true);
		assertNotNull(String.format("InsertEvent should return error for %s", e01), err);

		Event event20 = new Event(null, null, null,
			new String[]{index.get(e2), index.get(e01)}, //e2 and e01
			nodes[2].Pub, 1, null);
		event20.sign(nodes[2].Key.getPrivate());
		index.put(e20, event20.hex());
		err = poset.InsertEvent(event20, true);
		assertNotNull(String.format("InsertEvent should return error for %s", e20), err);
	}

	/*
	|  s11  |
	|   |   |
	|   f1  |
	|  /|   |
	| / s10 |
	|/  |   |
	e02 |   |
	| \ |   |
	|   \   |
	|   | \ |
	s00 |  e21
	|   | / |
	|  e10  s20
	| / |   |
	e0  e1  e2
	0   1    2
	*/

	public void initRoundPoset() {
		play[] plays = new play[]{
			new play(1, 1, e1, e0, e10, null, null, new String[]{e0, e1}),
			new play(2, 1, e2, "", s20, null, null, new String[]{e2}),
			new play(0, 1, e0, "", s00, null, null, new String[]{e0}),
			new play(2, 2, s20, e10, e21, null, null, new String[]{e0, e1, e2}),
			new play(0, 2, s00, e21, e02, null, null, new String[]{e0, e21}),
			new play(1, 2, e10, "", s10, null, null, new String[]{e0, e1}),
			new play(1, 3, s10, e02, f1, null, null, new String[]{e21, e02, e1}),
			new play(1, 4, f1, "", s11, new byte[][]{"abc".getBytes()}, null,
					new String[]{e21, e02, f1}),
		};
		initPosetFull(plays, false, n);
	}

	@Test
	public void testInsertEvent() {
		initRoundPoset();

		// "Check Event Coordinates"
		RResult<Event> getEventCall = poset.Store.getEvent(index.get(e0));
		Event e0Event = getEventCall.result;
		error err = getEventCall.err;
		assertNull("No error", err);

		if (!(e0Event.message.SelfParentIndex == -1 &&
			e0Event.message.OtherParentCreatorID == -1 &&
			e0Event.message.OtherParentIndex == -1 &&
			e0Event.message.CreatorID == poset.Participants.byPubKey(e0Event.creator()).getID())) {
			fail(String.format("Invalid wire info on %s", e0));
		}

		RResult<Event> getEvent = poset.Store.getEvent(index.get(e21));
		Event e21Event = getEvent.result;
		err = getEvent.err;
		assertNull("No error", err);

		getEvent = poset.Store.getEvent(index.get(e10));
		Event e10Event = getEvent.result;
		err = getEvent.err;
		assertNull("No error", err);

		if (!(e21Event.message.SelfParentIndex == 1 &&
			e21Event.message.OtherParentCreatorID == poset.Participants.byPubKey(e10Event.creator()).getID() &&
			e21Event.message.OtherParentIndex == 1 &&
			e21Event.message.CreatorID == poset.Participants.byPubKey(e21Event.creator()).getID())) {
			fail(String.format("Invalid wire info on %s", e21));
		}

		getEvent = poset.Store.getEvent(index.get(f1));
		Event f1Event = getEvent.result;
		err = getEvent.err;
		assertNull("No error", err);

		if (!(f1Event.message.SelfParentIndex == 2 &&
			f1Event.message.OtherParentCreatorID == poset.Participants.byPubKey(e0Event.creator()).getID() &&
			f1Event.message.OtherParentIndex == 2 &&
			f1Event.message.CreatorID == poset.Participants.byPubKey(f1Event.creator()).getID())) {
			fail(String.format("Invalid wire info on %s", f1));
		}

		String e0CreatorID = "" + poset.Participants.byPubKey(e0Event.creator()).getID();

		Hierarchy[] toCheck = new Hierarchy[]{
			new Hierarchy(e0, "Root" + e0CreatorID, ""),
				new Hierarchy(e10, index.get(e1), index.get(e0)),
				new Hierarchy(e21, index.get(s20), index.get(e10)),
				new Hierarchy(e02, index.get(s00), index.get(e21)),
				new Hierarchy(f1, index.get(s10), index.get(e02))
		};

		for (Hierarchy v : toCheck) {
			if (!checkParents(v.ev, v.selfAncestor, v.ancestor)) {
				fail(v.ev + " selfParent not good");
			}
		}

		// "Check UndeterminedEvents"
		String[] expectedUndeterminedEvents = new String[]{
			index.get(e0),
			index.get(e1),
			index.get(e2),
			index.get(e10),
			index.get(s20),
			index.get(s00),
			index.get(e21),
			index.get(e02),
			index.get(s10),
			index.get(f1),
			index.get(s11)};

		for (int i = 0; i < expectedUndeterminedEvents.length; ++i) {
			String eue = expectedUndeterminedEvents[i];
			String ue = poset.UndeterminedEvents.get(i);
			assertEquals(String.format("UndeterminedEvents[%d] should match",
				i), eue, ue);
		}

		// Pending loaded Events
		// 3 Events with index 0,
		// 1 Event with non-empty Transactions
		// = 4 Loaded Events
		int ple = poset.PendingLoadedEvents;
		assertEquals("PendingLoadedEvents should be 4", 4, ple);
	}


	private boolean checkParents(String e, String selfAncestor, String ancestor) {
		RResult<Event> getEvent = poset.Store.getEvent(index.get(e));
		Event ev = getEvent.result;
		error err = getEvent.err;
		assertNull("No error", err);
		return ev.selfParent().equals(selfAncestor) && ev.otherParent().equals(ancestor);
	}


	//@Test
	public void testReadWireInfo() {
		initRoundPoset();
		System.out.println(index);
		index.forEach((k, evh) -> {
			if (k.charAt(0) == 'r') {
				return;
			}
			RResult<Event> getEvent = poset.Store.getEvent(evh);
			Event ev = getEvent.result;
			error err = getEvent.err;
			assertNull("No error", err);

			WireEvent evWire = ev.toWire();

			RResult<Event> readWireInfo = poset.ReadWireInfo(evWire);
			Event evFromWire = readWireInfo.result;
			err = readWireInfo.err;
			assertNull("No error", err);

			assertArrayEquals(String.format("Error converting %s.Body.BlockSignatures"+
					" from light wire", k), ev.message.Body.BlockSignatures,
						evFromWire.message.Body.BlockSignatures);

			assertEquals(String.format("Error converting %s.Body from light wire", k),
				ev.message.Body, evFromWire.message.Body);
			assertEquals(String.format("Error converting %s.Signature from light wire", k), ev.message.Signature,
				evFromWire.message.Signature);
			RResult<Boolean> verify = evFromWire.verify();
			boolean ok = verify.result;
			err = verify.err;
			assertTrue(String.format("Error verifying signature for %s from ligh wire: %s",
					k, err), ok);
		});
	}

	@Test
	public void testStronglySee() {
		initRoundPoset();

		ancestryItem[] expected = new ancestryItem[]{
			new ancestryItem(e21, e0, true, false),
			new ancestryItem(e02, e10, true, false),
			new ancestryItem(e02, e0, true, false),
			new ancestryItem(e02, e1, true, false),
			new ancestryItem(f1, e21, true, false),
			new ancestryItem(f1, e10, true, false),
			new ancestryItem(f1, e0, true, false),
			new ancestryItem(f1, e1, true, false),
			new ancestryItem(f1, e2, true, false),
			new ancestryItem(s11, e2, true, false),
			// false negatives
			new ancestryItem(e10, e0, false, false),
			new ancestryItem(e21, e1, false, false),
			new ancestryItem(e21, e2, false, false),
			new ancestryItem(e02, e2, false, false),
			new ancestryItem(s11, e02, false, false),
			new ancestryItem(s11, "", false, true),
			// root events
			new ancestryItem(s11, r1, true, false),
			new ancestryItem(e21, r0, true, false),
			new ancestryItem(e21, r1, false, false),
			new ancestryItem(e10, r0, false, false),
			new ancestryItem(s20, r2, false, false),
			new ancestryItem(e02, r2, false, false),
			new ancestryItem(e21, r2, false, false),
		};

		for (ancestryItem exp : expected) {
			RResult<Boolean> sSee = poset.stronglySee(index.get(exp.descendant), index.get(exp.ancestor));
			boolean s = sSee.result;
			error err = sSee.err;
			if (err != null && !exp.err) {
				fail(String.format("Error computing stronglySee(%s, %s)",
					exp.descendant, exp.ancestor, err));
			}
			assertEquals(String.format("stronglySee(%s, %s) should match",
				exp.descendant, exp.ancestor), exp.val, s);
		}
	}

	//@Test
	public void testWitness() {
		initRoundPoset();

		Map<String,RoundEvent> round0Witnesses = new HashMap<String,RoundEvent>();
		round0Witnesses.put(index.get(e0), new RoundEvent(true, Trilean.UNDEFINED));
		round0Witnesses.put(index.get(e1), new RoundEvent(true, Trilean.UNDEFINED));
		round0Witnesses.put(index.get(e2), new RoundEvent( true, Trilean.UNDEFINED));
		poset.Store.setRound(0, new RoundInfo(
				new RoundInfoMessage(round0Witnesses)));

		Map<String, RoundEvent> round1Witnesses = new HashMap<String,RoundEvent>();
		round1Witnesses.put(index.get(f1), new RoundEvent(true, Trilean.UNDEFINED));
		poset.Store.setRound(1, new RoundInfo(new RoundInfoMessage(round1Witnesses)));

		ancestryItem[] expected = new ancestryItem[]{
			new ancestryItem("", e0, true, false),
			new ancestryItem("", e1, true, false),
			new ancestryItem("", e2, true, false),
			new ancestryItem("", f1, true, false),
			new ancestryItem("", e10, false, false),
			new ancestryItem("", e21, true, false),
			new ancestryItem("", e02, true, false)
		};

		for (ancestryItem exp : expected) {
			RResult<Boolean> witnessCall = poset.witness(index.get(exp.ancestor));
			boolean s = witnessCall.result;
			error err = witnessCall.err;
			assertNull(String.format("No Error computing witness(%s)",
					exp.ancestor), err);
			assertEquals(String.format("witness(%s) should match",
				exp.ancestor), exp.val, s);
		}
	}
//
//	@Test
//	public void TestRound() {
//		p, index, _ := initRoundPoset();
//
//		round0Witnesses := make(map[string]*RoundEvent)
//		round0Witnesses[index.get(e0)] = &RoundEvent{
//			Witness: true, Famous: Trilean_UNDEFINED}
//		round0Witnesses[index.get(e1)] = &RoundEvent{
//			Witness: true, Famous: Trilean_UNDEFINED}
//		round0Witnesses[index.get(e2)] = &RoundEvent{
//			Witness: true, Famous: Trilean_UNDEFINED}
//		poset.Store.SetRound(0, RoundInfo{Message: RoundInfoMessage{
//			Events: round0Witnesses}})
//
//		round1Witnesses := make(map[string]*RoundEvent)
//		round1Witnesses[index.get(e21)] = &RoundEvent{
//			Witness: true, Famous: Trilean_UNDEFINED}
//		round1Witnesses[index.get(e02)] = &RoundEvent{
//			Witness: true, Famous: Trilean_UNDEFINED}
//		round1Witnesses[index.get(f1)] = &RoundEvent{
//			Witness: true, Famous: Trilean_UNDEFINED}
//		poset.Store.SetRound(1, RoundInfo{
//			Message: RoundInfoMessage{Events: round1Witnesses}})
//
//		expected := []roundItem{
//			{e0, 0),
//			{e1, 0),
//			{e2, 0),
//			{s00, 0),
//			{e10, 0),
//			{s20, 0),
//			{e21, 1),
//			{e02, 1),
//			{s10, 0),
//			{f1, 1),
//			{s11, 2),
//		}
//
//		for _, exp := range expected {
//			r, err := poset.round(index.get(exp.event))
//			if err != null {
//				t.Fatalf("Error computing round(%s)", exp.event, err)
//			}
//			if r != exp.round {
//				t.Fatalf("round(%s) should be %v, not %v", exp.event, exp.round, r)
//			}
//		}
//	}
//
//	//@Test
//	public void TestRoundDiff() {
//		p, index, _ := initRoundPoset();
//
//		round0Witnesses := make(map[string]*RoundEvent)
//		round0Witnesses[index.get(e0)] = &RoundEvent{
//			Witness: true, Famous: Trilean_UNDEFINED}
//		round0Witnesses[index.get(e1)] = &RoundEvent{
//			Witness: true, Famous: Trilean_UNDEFINED}
//		round0Witnesses[index.get(e2)] = &RoundEvent{
//			Witness: true, Famous: Trilean_UNDEFINED}
//		poset.Store.SetRound(0, RoundInfo{
//			Message: RoundInfoMessage{Events: round0Witnesses}})
//
//		round1Witnesses := make(map[string]*RoundEvent)
//		round1Witnesses[index.get(e21)] = &RoundEvent{
//			Witness: true, Famous: Trilean_UNDEFINED}
//		round1Witnesses[index.get(e02)] = &RoundEvent{
//			Witness: true, Famous: Trilean_UNDEFINED}
//		round1Witnesses[index.get(f1)] = &RoundEvent{
//			Witness: true, Famous: Trilean_UNDEFINED}
//		poset.Store.SetRound(1,
//			RoundInfo{Message: RoundInfoMessage{Events: round1Witnesses}})
//
//		if d, err := poset.roundDiff(index.get(s11), index.get(e21)); d != 1 {
//			if err != null {
//				t.Fatalf("RoundDiff(%s, %s) returned an error: %s", s11, e02, err)
//			}
//			t.Fatalf("RoundDiff(%s, %s) should be 1 not %d", s11, e02, d)
//		}
//
//		if d, err := poset.roundDiff(index.get(f1), index.get(s11)); d != -1 {
//			if err != null {
//				t.Fatalf("RoundDiff(%s, %s) returned an error: %s", s11, f1, err)
//			}
//			t.Fatalf("RoundDiff(%s, %s) should be -1 not %d", s11, f1, d)
//		}
//		if d, err := poset.roundDiff(index.get(e02), index.get(e21)); d != 0 {
//			if err != null {
//				t.Fatalf("RoundDiff(%s, %s) returned an error: %s", e20, e21, err)
//			}
//			t.Fatalf("RoundDiff(%s, %s) should be 0 not %d", e20, e21, d)
//		}
//	}
//
//  @Test
//	public void TestDivideRounds() {
//		p, index, _ := initRoundPoset(t)
//
//		if err := poset.DivideRounds(); err != null {
//			t.Fatal(err)
//		}
//
//		if l := poset.Store.LastRound(); l != 2 {
//			t.Fatalf("last round should be 2 not %d", l)
//		}
//
//		round0, err := poset.Store.GetRound(0)
//		if err != null {
//			t.Fatal(err)
//		}
//		if l := len(round0.Witnesses()); l != 3 {
//			t.Fatalf("round 0 should have 3 witnesses, not %d", l)
//		}
//		if !contains(round0.Witnesses(), index.get(e0)) {
//			t.Fatalf("round 0 witnesses should contain %s", e0)
//		}
//		if !contains(round0.Witnesses(), index.get(e1)) {
//			t.Fatalf("round 0 witnesses should contain %s", e1)
//		}
//		if !contains(round0.Witnesses(), index.get(e2)) {
//			t.Fatalf("round 0 witnesses should contain %s", e2)
//		}
//
//		round1, err := poset.Store.GetRound(1)
//		assertNull("No error", err);
//		if l := len(round1.Witnesses()); l != 3 {
//			t.Fatalf("round 1 should have 1 witness, not %d", l)
//		}
//		if !contains(round1.Witnesses(), index.get(f1)) {
//			t.Fatalf("round 1 witnesses should contain %s", f1)
//		}
//
//		round2, err := poset.Store.GetRound(2)
//		assertNull("No error", err);
//
//		if l := len(round2.Witnesses()); l != 1 {
//			t.Fatalf("round 1 should have 1 witness, not %d", l)
//		}
//
//		expectedPendingRounds := []pendingRound{
//			{
//				Index:   0,
//				Decided: false,
//			),
//			{
//				Index:   1,
//				Decided: false,
//			), {
//				Index:   2,
//				Decided: false,
//			),
//		}
//		for i, pd := range poset.PendingRounds {
//			if !reflect.DeepEqual(*pd, expectedPendingRounds[i]) {
//				t.Fatalf("pendingRounds[%d] should be %v, not %v",
//					i, expectedPendingRounds[i], *pd)
//			}
//		}
//
//
//		expectedTimestamps := map[string]tr{
//			e0, new tr(0, 0),
//			e1, new tr(0, 0),
//			e2, new tr(0, 0),
//			s00, new tr(1, 0),
//			e10, new tr(1, 0),
//			s20, new tr(1, 0),
//			e21, new tr(2, 1),
//			e02, new tr(3, 1),
//			s10, new tr(2, 0),
//			f1, new tr(4, 1),
//			s11, new tr(5, 2),
//		}
//
//		for e, et := range expectedTimestamps {
//			ev, err := poset.Store.GetEvent(index.get(e))
//			if err != null {
//				t.Fatal(err)
//			}
//			if r := ev.round; r == null || *r != et.r {
//				t.Fatalf("%s round should be %d, not %d", e, et.r, *r)
//			}
//			if ts := ev.lamportTimestamp; ts == null || *ts != et.t {
//				t.Fatalf("%s lamportTimestamp should be %d, not %d", e, et.t, *ts)
//			}
//		}
//
//	}
//
//	@Test
//	public void TestCreateRoot() {
//		p, index, _ := initRoundPoset(t)
//		poset.DivideRounds()
//
//		participants := poset.Participants.ToPeerSlice()
//
//		baseRoot := NewBaseRoot(participants[0).GetID())
//
//		expected := map[string]Root{
//			e0: baseRoot,
//			e02: {
//				NextRound: 1,
//				SelfParent: &RootEvent{Hash: index.get(s00),
//					CreatorID: participants[0).GetID(), Index: 1,
//					LamportTimestamp: 1, Round: 0},
//				Others: HashMap<String,RootEvent>{
//					index.get(e02): {Hash: index.get(e21), CreatorID: participants[2).GetID(),
//						Index: 2, LamportTimestamp: 2, Round: 1},
//				},
//			},
//			s10: {
//				NextRound: 0,
//				SelfParent: &RootEvent{Hash: index.get(e10),
//					CreatorID: participants[1).GetID(), Index: 1,
//					LamportTimestamp: 1, Round: 0},
//				Others: HashMap<String,RootEvent>{},
//			},
//			f1: {
//				NextRound: 1,
//				SelfParent: &RootEvent{Hash: index.get(s10),
//					CreatorID: participants[1).GetID(), Index: 2,
//					LamportTimestamp: 2, Round: 0},
//				Others: HashMap<String,RootEvent>{
//					index.get(f1): {Hash: index.get(e02), CreatorID: participants[0).GetID(),
//						Index: 2, LamportTimestamp: 3, Round: 1},
//				},
//			},
//		}
//
//		for evh, expRoot := range expected {
//			ev, err := poset.Store.GetEvent(index.get(evh))
//			if err != null {
//				t.Fatal(err)
//			}
//			root, err := poset.createRoot(ev)
//			if err != null {
//				t.Fatalf("Error creating %s Root: %v", evh, err)
//			}
//			if !reflect.DeepEqual(expRoot, root) {
//				t.Fatalf("%s Root should be %+v, not %+v", evh, expRoot, root)
//			}
//		}
//	}
//
//
//	/*
//	e01  e12
//	 |   |  \
//	 e0  R1  e2
//	 |       |
//	 R0      R2
//
//	*/
//	public initDentedPoset() {
//		initPosetNodes(n);
//
//		orderedPeers = participants.ToPeerSlice();
//
//		for _, peer : orderedPeers {
//			index.get(rootSelfParent(peer.ID)) = rootSelfParent(peer.ID)
//		}
//
//		plays := []play{
//			{0, 0, rootSelfParent(orderedPeers[0).GetID()), "", e0, null, null,
//				String[]{}},
//			{2, 0, rootSelfParent(orderedPeers[2).GetID()), "", e2, null, null,
//				String[]{}},
//			{0, 1, e0, "", e01, null, null, new String[]{}},
//			{1, 0, rootSelfParent(orderedPeers[1).GetID()), e2, e12, null, null,
//				String[]{}},
//		}
//
//		playEvents(plays, nodes, index, orderedEvents)
//
//		poset = createPoset(false, orderedEvents, participants, testLogger(t))
//	}
//
//	public TestCreateRootBis() {
//		p, index := initDentedPoset(t)
//
//		participants := poset.Participants.ToPeerSlice()
//
//		root := NewBaseRootEvent(participants[1).GetID())
//		expected := map[string]Root{
//			e12: {
//				NextRound:  0,
//				SelfParent: &root,
//				Others: HashMap<String,RootEvent>{
//					index.get(e12): {Hash: index.get(e2), CreatorID: participants[2).GetID(),
//						Index: 0, LamportTimestamp: 0, Round: 0},
//				},
//			},
//		}
//
//		for evh, expRoot := range expected {
//			ev, err := poset.Store.GetEvent(index.get(evh))
//			if err != null {
//				t.Fatal(err)
//			}
//			root, err := poset.createRoot(ev)
//			if err != null {
//				t.Fatalf("Error creating %s Root: %v", evh, err)
//			}
//			if !reflect.DeepEqual(expRoot, root) {
//				t.Fatalf("%s Root should be %v, not %v", evh, expRoot, root)
//			}
//		}
//	}
//
//	/*
//
//	e0  e1  e2    Block (0, 1)
//	0   1    2
//	*/
//	public void initBlockPoset() {
//		initPosetNodes(n);
//
//		Peer[] peers = participants.ToPeerSlice();
//		for (int i = 0; i < peers.length; ++i) {
//			Peer peer = peers[i];
//			Event event = new Event(null, null, null, new String[]{rootSelfParent(peer.ID), ""},
//				nodes[i].Pub, 0, null);
//			nodes[i].signAndAddEvent(event, fmt.Sprintf("e%d", i),
//				index, orderedEvents);
//		}
//
//		poset = new Poset(participants, new InmemStore(participants, cacheSize),
//			null, testLogger());
//
//		//create a block and signatures manually
//		block := NewBlock(0, 1, []byte("framehash"),
//			new byte[][]{[]byte("block tx")})
//		err := poset.Store.SetBlock(block)
//		assertNull("No error setting block. Err: %s", err);
//
//		for i, ev := range *orderedEvents {
//			if err := poset.InsertEvent(ev, true); err != null {
//				fmt.Printf("error inserting event %d: %s\n", i, err)
//			}
//		}
//
//		return poset, nodes, index
//	}
//
//	@Test
//	public void TestInsertEventsWithBlockSignatures() {
//		initBlockPoset();
//
//		RetResult<Block> getBlock = poset.Store.GetBlock(0);
//		block = getBlock.result;
//		err = getBlock.err;
//		assertNull("No error retrieving block 0. %s", err);
//
//		blockSigs = new BlockSignature[n];
//		for k, n := range nodes {
//			blockSigs[k], err = block.Sign(n.Key)
//			assertNull("No error", err);
//		}
//
//		t.Run("InsertingEventsWithValidSignatures", public() {
//
//			/*
//				s00 |   |
//				|   |   |
//				|  e10  s20
//				| / |   |
//				e0  e1  e2
//				0   1    2
//			*/
//			plays := []play{
//				{1, 1, e1, e0, e10, null, []BlockSignature{blockSigs[1]},
//					String[]{}},
//				{2, 1, e2, "", s20, null, []BlockSignature{blockSigs[2]},
//					String[]{}},
//				{0, 1, e0, "", s00, null, []BlockSignature{blockSigs[0]},
//					String[]{}},
//			}
//
//			for _, pl := range plays {
//				e := NewEvent(pl.txPayload,
//					null,
//					pl.sigPayload,
//					String[]{index.get(pl.selfParent), index.get(pl.otherParent)},
//					nodes[pl.to].Pub,
//					pl.index, null)
//				e.Sign(nodes[pl.to].Key)
//				index.get(pl.name) = e.Hex()
//				if err := poset.InsertEvent(e, true); err != null {
//					t.Fatalf("error inserting event %s: %s\n", pl.name, err)
//				}
//			}
//
//			// Check SigPool
//			if l := len(poset.SigPool); l != 3 {
//				t.Fatalf("block signature pool should contain 3 signatures,"+
//					" not %d", l)
//			}
//
//			// Process SigPool
//			poset.ProcessSigPool()
//
//			// Check that the block contains 3 signatures
//			block, _ := poset.Store.GetBlock(0)
//			if l := len(block.Signatures); l != 2 {
//				t.Fatalf("block 0 should contain 2 signatures, not %d", l)
//			}
//
//			// Check that SigPool was cleared
//			if l := len(poset.SigPool); l != 0 {
//				t.Fatalf("block signature pool should contain 0 signatures,"+
//					" not %d", l)
//			}
//		})
//
//		t.Run("InsertingEventsWithSignatureOfUnknownBlock",
//			public() {
//				// The Event should be inserted
//				// The block signature is simply ignored
//
//				block1 := NewBlock(1, 2, []byte("framehash"), new byte[][]{})
//				sig, _ := block1.Sign(nodes[2].Key)
//
//				// unknown block
//				unknownBlockSig := BlockSignature{
//					Validator: nodes[2].Pub,
//					Index:     1,
//					Signature: sig.Signature,
//				}
//				pl := play{2, 2, s20, e10, e21, null, []BlockSignature{unknownBlockSig},
//					String[]{}}
//
//				e := NewEvent(null,
//					null,
//					pl.sigPayload,
//					String[]{index.get(pl.selfParent), index.get(pl.otherParent)},
//					nodes[pl.to].Pub,
//					pl.index, null)
//				e.Sign(nodes[pl.to].Key);
//				index.put(pl.name, e.Hex());
//				if err := poset.InsertEvent(e, true); err != null {
//					t.Fatalf("ERROR inserting event %s: %s", pl.name, err)
//				}
//
//				// check that the event was recorded
//				_, err := poset.Store.GetEvent(index[e21])
//				if err != null {
//					t.Fatalf("ERROR fetching Event %s: %s", e21, err)
//				}
//
//			})
//
//		t.Run("InsertingEventsWithBlockSignatureNotFromCreator",
//			public() {
//				// The Event should be inserted
//				// The block signature is simply ignored
//
//				// wrong validator
//				// Validator should be same as Event creator (node 0)
//				key, _ := crypto.GenerateECDSAKey()
//				badNode := NewTestNode(key, 666)
//				badNodeSig, _ := block.Sign(badNode.Key)
//
//				pl := play{0, 2, s00, e21, e02, null, []BlockSignature{badNodeSig},
//					String[]{}}
//
//				e := NewEvent(null,
//					null,
//					pl.sigPayload,
//					String[]{index[pl.selfParent], index[pl.otherParent]},
//					nodes[pl.to].Pub,
//					pl.index, null)
//				e.Sign(nodes[pl.to].Key)
//				index[pl.name] = e.Hex()
//				if err := poset.InsertEvent(e, true); err != null {
//					t.Fatalf("ERROR inserting event %s: %s\n", pl.name, err)
//				}
//
//				// check that the signature was not appended to the block
//				block, _ := poset.Store.GetBlock(0)
//				if l := len(block.Signatures); l > 3 {
//					t.Fatalf("Block 0 should contain 3 signatures, not %d", l)
//				}
//			})
//
//	}
//
//	/*
//	                   Round 8
//	      [m0]  | [m2]-----------------------------
//			| \ | / |  Round 7
//			|  <l1> |
//			|  /|   |
//		  <l0>  |   |
//			| \ |   |
//			|   \   |
//			|   | \ |
//			|   | <l2>-----------------------------
//			|   | / |  Round 6
//			| [k10] |
//			| / |   |
//		  [k0]  | [k2]-----------------------------
//			| \ | / |  Round 5
//			| <j1>  |
//			|  /|   |
//		  <j0>  |   |
//			| \ |   |
//			|   \   |
//			|   | \ |
//		    |   | <j2>-----------------------------
//			|   | / |  Round 4
//			| [i1]  |
//			| / |   |
//		  [i0]  | [i2]-----------------------------
//			| \ | / |  Round 3
//			| <h10> |
//			|  /|   |
//		   h0b  |   |
//			|   |   |
//		  <h0>  |   |
//			| \ |   |
//			|   \   |
//			|   | \ |
//		---g0x  | <h2>----------------------------- //g0x's other-parent is f2. This situation can happen with concurrency.
//		|	|   | / |  Round 2
//		|	|  g10  |
//		|	| / |   |
//		|  [g0] | [g2]
//		|	| \ | / |
//		|	| [g1]  | ------------------------------
//		|	|   |   |  Round 1
//		|	| <f1>  |
//		|  	|  /|   |
//		| <f0>  |   |
//		|	| \ |   |
//		|	|   \   |
//		|	|   | \ |
//		|   |   |  f2b
//		|	|   |   |
//		----------<f2>------------------------------
//			|   | / |  Round 0
//			|  e10  |
//		    | / |   |
//		   [e0][e1][e2]
//			0   1    2
//	*/
//	public void initConsensusPoset(boolean db) {
//		play[] plays = new play[]{
//			new play(1, 1, e1, e0, e10, null, null, new String[]{e0, e1}),
//			new play(2, 1, e2, e10, f2, new byte[][]{[]byte(f2)}, null, new String[]{e0, e1, e2}),
//			new play(2, 2, f2, "", f2b, null, null, new String[]{f2}),
//			new play(0, 1, e0, f2b, f0, null, null, new String[]{e0, f2}),
//			new play(1, 2, e10, f0, f1, null, null, new String[]{f2, f0, e1}}),
//			new play(1, 3, f1, "", g1, new byte[][]{[]byte(g1)}, null, new String[]{f2, f0, f1}),
//			new play(0, 2, f0, g1, g0, null, null, new String[]{g1, f0}),
//			new play(2, 3, f2b, g1, g2, null, null, new String[]{g1, f2}),
//			new play(1, 4, g1, g0, g10, null, null, new String[]{g1, f0}),
//			new play(0, 3, g0, f2, g0x, null, null, new String[]{g0, g1, f2b}),
//			new play(2, 4, g2, g10, h2, null, null, new String[]{g1, g0, g2}),
//			new play(0, 4, g0x, h2, h0, null, null, new String[]{h2, g0, g1}),
//			new play(0, 5, h0, "", h0b, new byte[][]{[]byte(h0b)), null, new String[]{h0, h2}),
//			new play(1, 5, g10, h0b, h10, null, null, new String[]{h0, h2, g1}),
//			new play(0, 6, h0b, h10, i0, null, null, new String[]{h10, h0, h2}),
//			new play(2, 5, h2, h10, i2, null, null, new String[]{h10, h0, h2}),
//			new play(1, 6, h10, i0, i1, new byte[][]{[]byte(i1)), null, new String[]{i0, h10, h0, h2}),
//			new play(2, 6, i2, i1, j2, null, null, new String[]{i1, i0, i2}),
//			new play(0, 7, i0, j2, j0, new byte[][]{[]byte(j0)), null, new String[]{i0, j2}),
//			new play(1, 7, i1, j0, j1, null, null, new String[]{i1, i0, j0, j2}),
//			new play(0, 8, j0, j1, k0, null, null, new String[]{j1, j0, j2}),
//			new play(2, 7, j2, j1, k2, null, null, new String[]{j1, j0, j2}),
//			new play(1, 8, j1, k0, k10, null, null, new String[]{j1, j0, j2, k0}),
//			new play(2, 8, k2, k10, l2, null, null, new String[]{k0, k10, k2}),
//			new play(0, 9, k0, l2, l0, null, null, new String[]{k0, l2}),
//			new play(1, 9, k10, l0, l1, null, null, new String[]{l0, l2, k10, k0}),
//			new play(0, 10, l0, l1, m0, null, null, new String[]{l1, l0, l2}),
//			new play(2, 9, l2, l1, m2, null, null, new String[]{l1, l0, l2}),
//		};
//
//		initPosetFull(plays, db, n, testLogger());
//	}
//
//	@Test
//	public void TestDivideRoundsBis() {
//		initConsensusPoset(false);
//
//		error err = poset.DivideRounds();
//		assertNull("No error", err);
//
//		Map<String,tr> m = new HashMap<String,tr>();
//		m.put(e0,  new tr(0, 0)),
//		m.put(e1, new tr(0, 0)),
//		m.put(e2, new tr(0, 0)),
//		m.put(e10, new tr(1, 0)),
//		m.put(f2, new tr(2, 1)),
//		m.put(f2b, new tr(3, 1)),
//		m.put(f0, new tr(4, 1)),
//		m.put(f1, new tr(5, 1)),
//		m.put(g1, new tr(6, 2)),
//		m.put(g0, new tr(7, 2)),
//		m.put(g2, new tr(7, 2)),
//		m.put(g10, new tr(8, 2)),
//		m.put(g0x, new tr(8, 2)),
//		m.put(h2, new tr(9, 3)),
//		m.put(h0, new tr(10, 3)),
//		m.put(h0b, new tr(11, 3)),
//		m.put(h10, new tr(12, 3)),
//		m.put(i0, new tr(13, 4)),
//		m.put(i2, new tr(13, 4)),
//		m.put(i1, new tr(14, 4)),
//		m.put(j2, new tr(15, 5)),
//		m.put(j0, new tr(16, 5)),
//		m.put(j1, new tr(17, 5)),
//		m.put(k0, new tr(18, 6)),
//		m.put(k2, new tr(18, 6)),
//		m.put(k10, new tr(19, 6)),
//		m.put(l2, new tr(20, 7)),
//		m.put(l0, new tr(21, 7)),
//		m.put(l1, new tr(22, 7)),
//		m.put(m0, new tr(23, 8)),
//		m.put(m2, new tr(23, 8))
//		Map<String,tr> expectedTimestamps = m;
//
//		error err;
//		expectedTimestamps.forEach( (e, et) -> {
//			RetResult<Event> getEvent = poset.Store.GetEvent(index.get(e));
//			Event ev = getEvent.result;
//			err = getEvent.err;
//			assertNull("No error", err);
//			assertEquals(String.format("%s round should match", e), et.r, ev.round);
//			assertEquals(String.format("%s lamportTimestamp should match", e), et.t, ev.lamportTimestamp);
//		}
//	}
//
//	@Test
//	public void TestDecideFame() {
//		initConsensusPoset(false);
//
//		poset.DivideRounds();
//		error err = poset.DecideFame();
//		assertNull("No error", err);
//
//		RetResult<RoundInfo> getRound = poset.Store.GetRound(0);
//		RoundInfo round0 = getRound.result;
//		err = getRound.err;
//		assertNull("No error", err);
//
//		RoundEvent f = round0.Message.Events.get(index.get(e0));
//		assertTrue(String.format("%s should be famous; got %s", e0, f),
//				f.Witness && f.Famous == Trilean.Trilean_TRUE);
//
//		f = round0.Message.Events.get(index.get(e1));
//		assertTrue(String.format("%s should be famous; got %s", e1, f),
//			f.Witness && f.Famous == Trilean.Trilean_TRUE);
//
//		f = round0.Message.Events.get(index.get(e2));
//		assertTrue(String.format("%s should be famous; got %s", e2, f),
//			f.Witness && f.Famous == Trilean.Trilean_TRUE);
//
//		getRound = poset.Store.GetRound(1);
//		round1 = getRound.result;
//		err = getRound.err;
//		assertNull("No error", err);
//
//		f = round1.Message.Events.get(index.get(f2));
//		assertTrue(String.format("%s should be famous; got %v", f2, f),
//			f.Witness && f.Famous == Trilean.Trilean_TRUE);
//		f = round1.Message.Events.get(index.get(f0));
//		assertTrue(String.format("%s should be famous; got %v", f0, f),
//			f.Witness && f.Famous == Trilean.Trilean_TRUE);
//		f = round1.Message.Events.get(index.get(f1));
//		assertTrue(String.format("%s should be famous; got %v", f1, f),
//			f.Witness && f.Famous == Trilean.Trilean_TRUE);
//
//		getRound = poset.Store.GetRound(2);
//		round2 = getRound.result;
//		err = getRound.err;
//		assertNull("No error", err);
//
//
//		if f := round2.Message.Events[index[g1]]; !(f.Witness &&
//			f.Famous == Trilean_TRUE) {
//			t.Fatalf("%s should be famous; got %v", g1, f)
//		}
//		if f := round2.Message.Events[index[g0]]; !(f.Witness &&
//			f.Famous == Trilean_TRUE) {
//			t.Fatalf("%s should be famous; got %v", g0, f)
//		}
//		if f := round2.Message.Events[index[g2]]; !(f.Witness &&
//			f.Famous == Trilean_TRUE) {
//			t.Fatalf("%s should be famous; got %v", g2, f)
//		}
//
//		round3, err := poset.Store.GetRound(3)
//		assertNull("No error", err);
//
//		if f := round3.Message.Events[index[h2]]; !(f.Witness &&
//			f.Famous == Trilean_TRUE) {
//			t.Fatalf("%s should be famous; got %v", h2, f)
//		}
//		if f := round3.Message.Events[index[h0]]; !(f.Witness &&
//			f.Famous == Trilean_TRUE) {
//			t.Fatalf("%s should be famous; got %v", h0, f)
//		}
//		if f := round3.Message.Events[index[h10]]; !(f.Witness &&
//			f.Famous == Trilean_TRUE) {
//			t.Fatalf("%s should be famous; got %v", h10, f)
//		}
//
//		round4, err := poset.Store.GetRound(4)
//		assertNull("No error", err);
//
//		if f := round4.Message.Events[index[i0]]; !(f.Witness &&
//			f.Famous == Trilean_UNDEFINED) {
//			t.Fatalf("%s should be famous; got %v", i0, f)
//		}
//		if f := round4.Message.Events[index[i2]]; !(f.Witness &&
//			f.Famous == Trilean_UNDEFINED) {
//			t.Fatalf("%s should be famous; got %v", i2, f)
//		}
//		if f := round4.Message.Events[index[i1]]; !(f.Witness &&
//			f.Famous == Trilean_UNDEFINED) {
//			t.Fatalf("%s should be famous; got %v", i1, f)
//		}
//
//		expectedPendingRounds := []pendingRound{
//			{Index: 0, Decided: true},
//			{Index: 1, Decided: true},
//			{Index: 2, Decided: true},
//			{Index: 3, Decided: true},
//			{Index: 4, Decided: false},
//			{Index: 5, Decided: true},
//			{Index: 6, Decided: false},
//			{Index: 7, Decided: false},
//			{Index: 8, Decided: false},
//		}
//		for i, pd := range poset.PendingRounds {
//			if !reflect.DeepEqual(*pd, expectedPendingRounds[i]) {
//				t.Fatalf("pendingRounds[%d] should be %v, not %v",
//					i, expectedPendingRounds[i], *pd)
//			}
//		}
//	}
//
//	public TestDecideRoundReceived() {
//		p, index := initConsensusPoset(false, t)
//
//		poset.DivideRounds()
//		poset.DecideFame()
//		err := poset.DecideRoundReceived();
//		assertNull("No error", err);
//
//		for name, hash := range index {
//			e, _ := poset.Store.GetEvent(hash)
//
//			switch rune(name[0]) {
//			case rune('e'):
//				if r := *e.roundReceived; r != 1 {
//					t.Fatalf("%s round received should be 1 not %d", name, r)
//				}
//			case rune('f'):
//				if r := *e.roundReceived; r != 2 {
//					t.Fatalf("%s round received should be 2 not %d", name, r)
//				}
//			}
//		}
//
//		round0, err := poset.Store.GetRound(0)
//		if err != null {
//			t.Fatalf("could not retrieve Round 0. %s", err)
//		}
//		if ce := len(round0.ConsensusEvents()); ce != 0 {
//			t.Fatalf("round 0 should contain 0 ConsensusEvents, not %d", ce)
//		}
//
//		round1, err := poset.Store.GetRound(1)
//		if err != null {
//			t.Fatalf("could not retrieve Round 1. %s", err)
//		}
//		if ce := len(round1.ConsensusEvents()); ce != 4 {
//			t.Fatalf("round 1 should contain 4 ConsensusEvents, not %d", ce)
//		}
//
//		round2, err := poset.Store.GetRound(2)
//		if err != null {
//			t.Fatalf("could not retrieve Round 2. %s", err)
//		}
//		if ce := len(round2.ConsensusEvents()); ce != 4 {
//			t.Fatalf("round 2 should contain 9 ConsensusEvents, not %d", ce)
//		}
//
//		String[] expectedUndeterminedEvents = new String[]{
//			index[g0x],
//			index[h2],
//			index[h0],
//			index[h0b],
//			index[h10],
//			index[j2],
//			index[j0],
//			index[j1],
//			index[k0],
//			index[k2],
//			index[k10],
//			index[l2],
//			index[l0],
//			index[l1],
//			index[m0],
//			index[m2],
//		}
//
//		for i, eue := range expectedUndeterminedEvents {
//			if ue := poset.UndeterminedEvents[i]; ue != eue {
//				t.Fatalf("undetermined event %d should be %s, not %s", i, eue, ue)
//			}
//		}
//	}
//
//	public TestProcessDecidedRounds() {
//		p, index := initConsensusPoset(false, t)
//
//		poset.DivideRounds()
//		poset.DecideFame()
//		poset.DecideRoundReceived()
//		if err := poset.ProcessDecidedRounds(); err != null {
//			t.Fatal(err)
//		}
//
//		consensusEvents := poset.Store.ConsensusEvents()
//
//		for i, e := range consensusEvents {
//			t.Logf("consensus[%d]: %s\n", i, getName(index, e))
//		}
//
//		if l := len(consensusEvents); l != 12 {
//			t.Fatalf("length of consensus should be 12 not %d", l)
//		}
//
//		if ple := poset.PendingLoadedEvents; ple != 3 {
//			t.Fatalf("pending loaded events number should be 3, not %d", ple)
//		}
//
//		block0, err := poset.Store.GetBlock(0)
//		if err != null {
//			t.Fatalf("store should contain a block with Index 0: %v", err)
//		}
//
//		if ind := block0.Index(); ind != 0 {
//			t.Fatalf("block0's index should be 0, not %d", ind)
//		}
//
//		if rr := block0.RoundReceived(); rr != 2 {
//			t.Fatalf("block0's round received should be 2, not %d", rr)
//		}
//
//		if l := len(block0.Transactions()); l != 1 {
//			t.Fatalf("block0 should contain 1 transaction, not %d", l)
//		}
//		if tx := block0.Transactions()[0]; !reflect.DeepEqual(tx, []byte(f2)) {
//			t.Fatalf("transaction 0 from block0 should be '%s', not %s", f2, tx)
//		}
//
//		frame1, err := poset.GetFrame(block0.RoundReceived())
//		frame1Hash, err := frame1.Hash()
//		if !reflect.DeepEqual(block0.GetFrameHash(), frame1Hash) {
//			t.Fatalf("frame hash from block0 should be %v, not %v",
//				frame1Hash, block0.GetFrameHash())
//		}
//
//		block1, err := poset.Store.GetBlock(1)
//		if err != null {
//			t.Fatalf("store should contain a block with Index 1: %v", err)
//		}
//
//		if ind := block1.Index(); ind != 1 {
//			t.Fatalf("block1's index should be 1, not %d", ind)
//		}
//
//		if rr := block1.RoundReceived(); rr != 3 {
//			t.Fatalf("block1's round received should be 3, not %d", rr)
//		}
//
//		if l := len(block1.Transactions()); l != 1 {
//			t.Fatalf("block1 should contain 1 transactions, not %d", l)
//		}
//
//		if tx := block1.Transactions()[0]; !reflect.DeepEqual(tx, []byte(g1)) {
//			t.Fatalf("transaction 0 from block1 should be '%s', not %s", g1, tx)
//		}
//
//		frame2, err := poset.GetFrame(block1.RoundReceived())
//		frame2Hash, err := frame2.Hash()
//		if !reflect.DeepEqual(block1.GetFrameHash(), frame2Hash) {
//			t.Fatalf("frame hash from block1 should be %v, not %v",
//				frame2Hash, block1.GetFrameHash())
//		}
//
//		expRounds := []pendingRound{
//			{Index: 4, Decided: false},
//			{Index: 5, Decided: true},
//			{Index: 6, Decided: false},
//			{Index: 7, Decided: false},
//			{Index: 8, Decided: false},
//		}
//		for i, pd := range poset.PendingRounds {
//			if !reflect.DeepEqual(*pd, expRounds[i]) {
//				t.Fatalf("pending round %d should be %v, not %v", i,
//					expRounds[i], *pd)
//			}
//		}
//
//		if v := poset.AnchorBlock; v != null {
//			t.Fatalf("anchor block should be null, not %v", v)
//		}
//
//	}
//
//	private void BenchmarkConsensus() {
//		for (int n = 0; n < b.N; n++) {
//			// we do not want to benchmark the initialization code
//			b.StopTimer();
//			p, _ := initConsensusPoset(false, b);
//			b.StartTimer();
//
//			poset.DivideRounds();
//			poset.DecideFame();
//			poset.DecideRoundReceived();
//			poset.ProcessDecidedRounds();
//		}
//	}
//
//	@Test
//	public void TestKnown() {
//		initConsensusPoset(false);
//
//		participants = poset.Participants.ToPeerSlice();
//
//		expectedKnown := HashMap<Long,Long>{
//			participants[0).GetID(): 10,
//			participants[1).GetID(): 9,
//			participants[2).GetID(): 9,
//		}
//
//		known := poset.Store.KnownEvents()
//		for i := range poset.Participants.ToIDSlice() {
//			if l := known[int64(i)]; l != expectedKnown[int64(i)] {
//				t.Fatalf("known event %d should be %d, not %d", i,
//					expectedKnown[int64(i)], l)
//			}
//		}
//	}
//
//	public TestGetFrame() {
//		p, index := initConsensusPoset(false);
//
//		participants := poset.Participants.ToPeerSlice()
//
//		poset.DivideRounds()
//		poset.DecideFame()
//		poset.DecideRoundReceived()
//		poset.ProcessDecidedRounds()
//
//		t.Run("round 1", public() {
//			expRoots := make([]Root, n)
//			expRoots[0] = NewBaseRoot(participants[0).GetID())
//			expRoots[1] = NewBaseRoot(participants[1).GetID())
//			expRoots[2] = NewBaseRoot(participants[2).GetID())
//
//			frame, err := poset.GetFrame(1)
//			if err != null {
//				t.Fatal(err)
//			}
//
//			for p, r := range frame.Roots {
//				expRoot := expRoots[p]
//				compareRootEvents(t, r.SelfParent, expRoot.SelfParent, index)
//				compareOtherParents(t, r.Others, expRoot.Others, index)
//			}
//
//			var expEvents []Event
//
//			hashes := String[]{index[e0], index[e1], index[e2], index[e10]}
//			for _, eh := range hashes {
//				e, err := poset.Store.GetEvent(eh)
//				if err != null {
//					t.Fatal(err)
//				}
//				expEvents = append(expEvents, e)
//			}
//
//			sort.Sort(ByLamportTimestamp(expEvents))
//			expEventMessages := make([]*EventMessage, len(expEvents))
//			for k := range expEvents {
//				expEventMessages[k] = &expEvents[k].Message
//			}
//
//			messages := frame.GetEvents()
//			if len(expEventMessages) != len(messages) {
//				t.Fatalf("expected number of other parents: %d, got: %d",
//					len(expEventMessages), len(messages))
//			}
//
//			for k, msg := range expEventMessages {
//				compareEventMessages(t, messages[k], msg, index)
//			}
//		})
//
//		t.Run("round 2", public() {
//			expRoots := make([]Root, n)
//			expRoots[0] = Root{
//				NextRound: 1,
//				SelfParent: &RootEvent{
//					Hash:             index[e0],
//					CreatorID:        participants[0).GetID(),
//					Index:            0,
//					LamportTimestamp: 0,
//					Round:            0,
//				},
//				Others: HashMap<String,RootEvent>{
//					index[f0]: {
//						Hash:             index[f2b],
//						CreatorID:        participants[2).GetID(),
//						Index:            2,
//						LamportTimestamp: 3,
//						Round:            1,
//					},
//				},
//			}
//			expRoots[1] = Root{
//				NextRound: 1,
//				SelfParent: &RootEvent{
//					Hash:             index[e10],
//					CreatorID:        participants[1).GetID(),
//					Index:            1,
//					LamportTimestamp: 1,
//					Round:            0,
//				},
//				Others: HashMap<String,RootEvent>{
//					index[f1]: {
//						Hash:             index[f0],
//						CreatorID:        participants[0).GetID(),
//						Index:            1,
//						LamportTimestamp: 4,
//						Round:            1,
//					},
//				},
//			}
//			expRoots[2] = Root{
//				NextRound: 1,
//				SelfParent: &RootEvent{
//					Hash:             index[e2],
//					CreatorID:        participants[2).GetID(),
//					Index:            0,
//					LamportTimestamp: 0,
//					Round:            0,
//				},
//				Others: HashMap<String,RootEvent>{
//					index[f2]: {
//						Hash:             index[e10],
//						CreatorID:        participants[1).GetID(),
//						Index:            1,
//						LamportTimestamp: 1,
//						Round:            0,
//					},
//				},
//			}
//
//			frame, err := poset.GetFrame(2)
//			if err != null {
//				t.Fatal(err)
//			}
//
//			for p, r := range frame.Roots {
//				expRoot := expRoots[p]
//				compareRootEvents(t, r.SelfParent, expRoot.SelfParent, index)
//				compareOtherParents(t, r.Others, expRoot.Others, index)
//			}
//
//			expectedEventsHashes := String[]{
//				index[f2],
//				index[f2b],
//				index[f0],
//				index[f1],
//			}
//			var expEvents []Event
//			for _, eh := range expectedEventsHashes {
//				e, err := poset.Store.GetEvent(eh)
//				if err != null {
//					t.Fatal(err)
//				}
//				expEvents = append(expEvents, e)
//			}
//			sort.Sort(ByLamportTimestamp(expEvents))
//			expEventMessages := make([]*EventMessage, len(expEvents))
//			for k := range expEvents {
//				expEventMessages[k] = &expEvents[k].Message
//			}
//
//			messages := frame.GetEvents()
//			if len(expEventMessages) != len(messages) {
//				t.Fatalf("expected number of other parents: %d, got: %d",
//					len(expEventMessages), len(messages))
//			}
//
//			for k, msg := range expEventMessages {
//				compareEventMessages(t, messages[k], msg, index)
//			}
//
//			block0, err := poset.Store.GetBlock(0)
//			if err != null {
//				t.Fatalf("store should contain a block with Index 0: %v", err)
//			}
//
//			frameHash, err := frame.Hash()
//			if err != null {
//				t.Fatal(err)
//			}
//
//			if !reflect.DeepEqual(block0.GetFrameHash(), frameHash) {
//				t.Fatalf("frame hash (0x%X) from block 0 and frame hash"+
//					" (0x%X) differ", block0.GetFrameHash(), frameHash)
//			}
//		})
//
//	}
//
//	public TestResetFromFrame() {
//		p, index := initConsensusPoset(false);
//
//		participants := poset.Participants.ToPeerSlice()
//
//		poset.DivideRounds()
//		poset.DecideFame()
//		poset.DecideRoundReceived()
//		poset.ProcessDecidedRounds()
//
//		block, err := poset.Store.GetBlock(1)
//		if err != null {
//			t.Fatal(err)
//		}
//
//		frame, err := poset.GetFrame(block.RoundReceived())
//		if err != null {
//			t.Fatal(err)
//		}
//
//		// This operation clears the private fields which need to be recomputed
//		// in the Events (round, roundReceived,etc)
//		marshalledFrame, _ := frame.ProtoMarshal()
//		unmarshalledFrame := new(Frame)
//		unmarshalledFrame.ProtoUnmarshal(marshalledFrame)
//
//		p2 := NewPoset(poset.Participants,
//			NewInmemStore(poset.Participants, cacheSize),
//			null,
//			testLogger(t))
//		err = p2.Reset(block, *unmarshalledFrame)
//		if err != null {
//			t.Fatal(err)
//		}
//
//		/*
//			The poset should now look like this:
//
//			   |   |  f10  |
//			   |   | / |   |
//			   |   f0  |   f2
//			   |   | \ | / |
//			   |   |  f1b  |
//			   |   |   |   |
//			   |   |   f1  |
//			   |   |   |   |
//			   +-- R0  R1  R2
//		*/
//
//		// Test Known
//		expectedKnown := HashMap<Long,Long>{
//			participants[0).GetID(): 2,
//			participants[1).GetID(): 4,
//			participants[2).GetID(): 3,
//		}
//
//		known := p2.Store.KnownEvents()
//		for _, peer := range p2.Participants.ById {
//			if l := known[peer.ID]; l != expectedKnown[peer.ID] {
//				t.Fatalf("Known[%d] should be %d, not %d",
//					peer.ID, expectedKnown[peer.ID], l)
//			}
//		}
//
//		t.Run("TestDivideRounds", public() {
//			if err := p2.DivideRounds(); err != null {
//				t.Fatal(err)
//			}
//
//			pRound1, err := poset.Store.GetRound(2)
//			if err != null {
//				t.Fatal(err)
//			}
//			p2Round1, err := p2.Store.GetRound(2)
//			if err != null {
//				t.Fatal(err)
//			}
//
//			// Check round 1 witnesses
//			pWitnesses := pRound1.Witnesses()
//			p2Witnesses := p2Round1.Witnesses()
//			sort.Strings(pWitnesses)
//			sort.Strings(p2Witnesses)
//			if !reflect.DeepEqual(pWitnesses, p2Witnesses) {
//				t.Fatalf("Reset Hg Round 1 witnesses should be %v, not %v",
//					pWitnesses, p2Witnesses)
//			}
//
//			// check event rounds and lamport timestamps
//			for _, em := range frame.Events {
//				e := em.ToEvent()
//				ev := &e
//				p2r, err := p2.round(ev.Hex())
//				if err != null {
//					t.Fatalf("Error computing %s Round: %d",
//						getName(index, ev.Hex()), p2r)
//				}
//				hr, _ := poset.round(ev.Hex())
//				if p2r != hr {
//
//					t.Fatalf("p2[%v].Round should be %d, not %d",
//						getName(index, ev.Hex()), hr, p2r)
//				}
//
//				p2s, err := p2.lamportTimestamp(ev.Hex())
//				if err != null {
//					t.Fatalf("Error computing %s LamportTimestamp: %d",
//						getName(index, ev.Hex()), p2s)
//				}
//				hs, _ := poset.lamportTimestamp(ev.Hex())
//				if p2s != hs {
//					t.Fatalf("p2[%v].LamportTimestamp should be %d, not %d",
//						getName(index, ev.Hex()), hs, p2s)
//				}
//			}
//		})
//
//		t.Run("TestConsensus", public() {
//			p2.DecideFame()
//			p2.DecideRoundReceived()
//			p2.ProcessDecidedRounds()
//
//			if lbi := p2.Store.LastBlockIndex(); lbi != block.Index() {
//				t.Fatalf("LastBlockIndex should be %d, not %d",
//					block.Index(), lbi)
//			}
//
//			if r := p2.LastConsensusRound; r == null || *r != block.RoundReceived() {
//				t.Fatalf("LastConsensusRound should be %d, not %d",
//					block.RoundReceived(), *r)
//			}
//
//			if v := p2.AnchorBlock; v != null {
//				t.Fatalf("AnchorBlock should be null, not %v", v)
//			}
//		})
//
//		t.Run("TestContinueAfterReset", public() {
//			// Insert remaining Events into the Reset poset
//			for r := int64(2); r <= int64(2); r++ {
//				round, err := poset.Store.GetRound(r)
//				if err != null {
//					t.Fatal(err)
//				}
//
//				var events []Event
//				for _, e := range round.RoundEvents() {
//					ev, err := poset.Store.GetEvent(e)
//					if err != null {
//						t.Fatal(err)
//					}
//					events = append(events, ev)
//				}
//
//				sort.Sort(ByTopologicalOrder(events))
//
//				for _, ev := range events {
//
//					marshalledEv, _ := ev.ProtoMarshal()
//					unmarshalledEv := new(Event)
//					unmarshalledEv.ProtoUnmarshal(marshalledEv)
//					p2.InsertEvent(*unmarshalledEv, true)
//				}
//			}
//
//			p2.DivideRounds()
//			p2.DecideFame()
//			p2.DecideRoundReceived()
//			p2.ProcessDecidedRounds()
//
//			for r := int64(2); r <= 2; r++ {
//				pRound, err := poset.Store.GetRound(r)
//				if err != null {
//					t.Fatal(err)
//				}
//				p2Round, err := p2.Store.GetRound(r)
//				if err != null {
//					t.Fatal(err)
//				}
//
//				pWitnesses := pRound.Witnesses()
//				p2Witnesses := p2Round.Witnesses()
//				sort.Strings(pWitnesses)
//				sort.Strings(p2Witnesses)
//
//				if !reflect.DeepEqual(pWitnesses, p2Witnesses) {
//					t.Fatalf("Reset Hg Round %d witnesses should be %v, not %v",
//						r, pWitnesses, p2Witnesses)
//				}
//			}
//		})
//	}
//
//	//@Test
//	public void TestBootstrap() {
//
//		// Initialize a first Poset with a DB backend
//		// Add events and run consensus methods on it
//		p, _ := initConsensusPoset(true, t)
//		poset.DivideRounds()
//		poset.DecideFame()
//		poset.DecideRoundReceived()
//		poset.ProcessDecidedRounds()
//
//		poset.Store.Close()
//		defer os.RemoveAll(badgerDir)
//
//		// Now we want to create a new Poset based on the database of the previous
//		// Poset and see if we can boostrap it to the same state.
//		recycledStore, err := LoadBadgerStore(cacheSize, badgerDir)
//		np := NewPoset(recycledStore.participants,
//			recycledStore,
//			null,
//			logrus.New().WithField("id", "bootstrapped"))
//		err = np.Bootstrap()
//		if err != null {
//			t.Fatal(err)
//		}
//
//		hConsensusEvents := poset.Store.ConsensusEvents()
//		nhConsensusEvents := np.Store.ConsensusEvents()
//		if len(hConsensusEvents) != len(nhConsensusEvents) {
//			t.Fatalf("Bootstrapped poset should contain %d consensus events,"+
//				"not %d", len(hConsensusEvents), len(nhConsensusEvents))
//		}
//
//		hKnown := poset.Store.KnownEvents()
//		nhKnown := np.Store.KnownEvents()
//		if !reflect.DeepEqual(hKnown, nhKnown) {
//			t.Fatalf("Bootstrapped poset's Known should be %#v, not %#v",
//				hKnown, nhKnown)
//		}
//
//		if *poset.LastConsensusRound != *np.LastConsensusRound {
//			t.Fatalf("Bootstrapped poset's LastConsensusRound should be %#v,"+
//				" not %#v", *poset.LastConsensusRound, *np.LastConsensusRound)
//		}
//
//		if poset.LastCommitedRoundEvents != np.LastCommitedRoundEvents {
//			t.Fatalf("Bootstrapped poset's LastCommitedRoundEvents should be %#v,"+
//				" not %#v", poset.LastCommitedRoundEvents, np.LastCommitedRoundEvents)
//		}
//
//		if poset.ConsensusTransactions != np.ConsensusTransactions {
//			t.Fatalf("Bootstrapped poset's ConsensusTransactions should be %#v,"+
//				" not %#v", poset.ConsensusTransactions, np.ConsensusTransactions)
//		}
//
//		if poset.PendingLoadedEvents != np.PendingLoadedEvents {
//			t.Fatalf("Bootstrapped poset's PendingLoadedEvents should be %#v,"+
//				" not %#v", poset.PendingLoadedEvents, np.PendingLoadedEvents)
//		}
//	}
//
//	/*
//
//		|   <w51> |    |
//	    |    |  \ |    |
//		|    |   <e23> |
//		|	 |    |	\  |	   	ROUND 7
//		|    |    |  <w43>----------------------
//		|    |    | /  | 		ROUND 6
//	    |    |  [w42]  |
//	    |    | /  |    |
//	    |  [w41]  |    |
//		| /  |    |    |
//	  [w40]  |    |    |------------------------
//	    | \  |    |    |		ROUND 5
//	    |  <d13>  |    |
//	    |    |  \ |    |
//	  <w30>  |    \    |
//	    | \  |    | \  |
//	    |   \     |  <w33>----------------------
//	    |    | \  |  / |		ROUND 4
//	    |    |  [w32]  |
//	    |    |  / |    |
//		|  [w31]  |    |
//	    |  / |    |    |
//	   [w20] |    |    |------------------------
//	    |  \ |    |    | 		ROUND 3
//	    |    \    |    |
//	    |    | \  |    |
//	    |    |  <w22>  |
//	    |    | /  |    |
//	    |   c10   |    |
//	    | /  |    |    |
//	  <b00><w21>  |    |------------------------
//	    |    |  \ |    |		ROUND 2
//	    |    |    \    |
//	    |    |    | \  |
//	    |    |    |  [w23]
//	    |    |    | /  |
//	   [w10] |   b21   |
//		| \  | /  |    |
//	    |  [w11]  |    |
//	    |    |  \ |    |
//		|    |  [w12]  |------------------------
//	    |    |    | \  |		ROUND 1
//	    |    |    |  <w13>
//	    |    |    | /  |
//	    |   a10 <a21>  |
//	    |  / |  / |    |
//	    |/ <a12>  |    |------------------------
//	   a00   |  \ |    |		ROUND 0
//		|    |   a23   |
//	    |    |    | \  |
//	  [w00][w01][w02][w03]
//		0	 1	  2	   3
//	*/
//
//	public initFunkyPoset(, Logger logger, full boolean) (*Poset, HashMap<String,String>) {
//		nodes, index, orderedEvents, participants := initPosetNodes(4)
//
//		for i, peer := range participants.ToPeerSlice() {
//			name := fmt.Sprintf("w0%d", i)
//			event := NewEvent(new byte[][]{[]byte(name)}, null,
//				null, new String[]{rootSelfParent(peer.ID), ""}, nodes[i].Pub, 0,
//				HashMap<String,Long>{rootSelfParent(peer.ID): 1})
//			nodes[i].signAndAddEvent(event, name, index, orderedEvents)
//		}
//
//		plays := []play{
//			{2, 1, w02, w03, a23, new byte[][]{[]byte(a23)},
//				null, new String[]{w02, w03}},
//			{1, 1, w01, a23, a12, new byte[][]{[]byte(a12)},
//				null, new String[]{w01, w02, w03}},
//			{0, 1, w00, "", a00, new byte[][]{[]byte(a00)},
//				null, new String[]{w00}},
//			{1, 2, a12, a00, a10, new byte[][]{[]byte(a10)},
//				null, new String[]{w00, a12}},
//			{2, 2, a23, a12, a21, new byte[][]{[]byte(a21)},
//				null, new String[]{a12, w02, w03}},
//			{3, 1, w03, a21, w13, new byte[][]{[]byte(w13)},
//				null, new String[]{a12, a21, w03}},
//			{2, 3, a21, w13, w12, new byte[][]{[]byte(w12)},
//				null, new String[]{a12, a21, w13}},
//			{1, 3, a10, w12, w11, new byte[][]{[]byte(w11)},
//				null, new String[]{w12, a12}},
//			{0, 2, a00, w11, w10, new byte[][]{[]byte(w10)},
//				null, new String[]{w11, w12, w00}},
//			{2, 4, w12, w11, b21, new byte[][]{[]byte(b21)},
//				null, new String[]{w11, w12}},
//			{3, 2, w13, b21, w23, new byte[][]{[]byte(w23)},
//				null, new String[]{w11, w12, w13}},
//			{1, 4, w11, w23, w21, new byte[][]{[]byte(w21)},
//				null, new String[]{w11, w12, w23}},
//			{0, 3, w10, "", b00, new byte[][]{[]byte(b00)},
//				null, new String[]{w10, w11, w12}},
//			{1, 5, w21, b00, c10, new byte[][]{[]byte(c10)},
//				null, new String[]{b00, w21}},
//			{2, 5, b21, c10, w22, new byte[][]{[]byte(w22)},
//				null, new String[]{b00, w21, w11, w12}},
//			{0, 4, b00, w22, w20, new byte[][]{[]byte(w20)},
//				null, new String[]{b00, w21, w22}},
//			{1, 6, c10, w20, w31, new byte[][]{[]byte(w31)},
//				null, new String[]{w20, b00, w21}},
//			{2, 6, w22, w31, w32, new byte[][]{[]byte(w32)},
//				null, new String[]{w31, w20, w22, b00, w21}},
//			{0, 5, w20, w32, w30, new byte[][]{[]byte(w30)},
//				null, new String[]{w32, w31, w20}},
//			{3, 3, w23, w32, w33, new byte[][]{[]byte(w33)},
//				null, new String[]{w23, w11, w12, w32, w31, w20}},
//			{1, 7, w31, w33, d13, new byte[][]{[]byte(d13)},
//				null, new String[]{w33, w31, w20}},
//			{0, 6, w30, d13, w40, new byte[][]{[]byte(w40)},
//				null, new String[]{w30, d13, w33}},
//			{1, 8, d13, w40, w41, new byte[][]{[]byte(w41)},
//				null, new String[]{w40, d13, w33}},
//			{2, 7, w32, w41, w42, new byte[][]{[]byte(w42)},
//				null, new String[]{w41, w40, w32, w31, w20}},
//			{3, 4, w33, w42, w43, new byte[][]{[]byte(w43)},
//				null, new String[]{w42, w41, w40, w33}},
//		}
//		if full {
//			newPlays := []play{
//				{2, 8, w42, w43, e23, new byte[][]{[]byte(e23)},
//					null, new String[]{w43, w42, w41, w40}},
//				{1, 9, w41, e23, w51, new byte[][]{[]byte(w51)},
//					null, new String[]{e23, w43, w41, w40}},
//			}
//			plays = append(plays, newPlays...)
//		}
//
//		playEvents(plays, nodes, index, orderedEvents)
//
//		poset := createPoset(t, false, orderedEvents, participants, logger.WithField("test", 6))
//
//		return poset, index
//	}
//
//	@Test
//	public void TestFunkyPosetFame() {
//		p, index := initFunkyPoset(t, common.NewTestLogger(t), false)
//
//		if err := poset.DivideRounds(); err != null {
//			t.Fatal(err)
//		}
//		if err := poset.DecideFame(); err != null {
//			t.Fatal(err)
//		}
//
//		l := poset.Store.LastRound()
//		if l != 7 {
//			t.Fatalf("last round should be 7 not %d", l)
//		}
//
//		for r := int64(0); r < l+1; r++ {
//			round, err := poset.Store.GetRound(r)
//			if err != null {
//				t.Fatal(err)
//			}
//			var witnessNames String[]
//			for _, w := range round.Witnesses() {
//				witnessNames = append(witnessNames, getName(index, w))
//			}
//			t.Logf("round %d witnesses: %v", r, witnessNames)
//		}
//
//		expPendingRounds := []pendingRound{
//			{Index: 0, Decided: true},
//			{Index: 1, Decided: true},
//			{Index: 2, Decided: true},
//			{Index: 3, Decided: true},
//			{Index: 4, Decided: true},
//			{Index: 5, Decided: false},
//			{Index: 6, Decided: false},
//			{Index: 7, Decided: false},
//		}
//
//		for i, pd := range poset.PendingRounds {
//			if !reflect.DeepEqual(*pd, expPendingRounds[i]) {
//				t.Fatalf("pending round %d should be %v, not %v", i,
//					expPendingRounds[i], *pd)
//			}
//		}
//
//		if err := poset.DecideRoundReceived(); err != null {
//			t.Fatal(err)
//		}
//		if err := poset.ProcessDecidedRounds(); err != null {
//			t.Fatal(err)
//		}
//
//		for i := 5; i < len(poset.PendingRounds)+5; i++ {
//			if !reflect.DeepEqual(*poset.PendingRounds[i-5], expPendingRounds[i]) {
//				t.Fatalf("pending round %d should be %v, not %v", i,
//					expPendingRounds[i], *poset.PendingRounds[i-5])
//			}
//		}
//	}
//
//	@Test
//	public void TestFunkyPosetBlocks() {
//		p, index := initFunkyPoset(t, common.NewTestLogger(t), true)
//
//		if err := poset.DivideRounds(); err != null {
//			t.Fatal(err)
//		}
//		if err := poset.DecideFame(); err != null {
//			t.Fatal(err)
//		}
//		if err := poset.DecideRoundReceived(); err != null {
//			t.Fatal(err)
//		}
//		if err := poset.ProcessDecidedRounds(); err != null {
//			t.Fatal(err)
//		}
//
//		l := poset.Store.LastRound()
//		if l != 7 {
//			t.Fatalf("last round should be 7 not %d", l)
//		}
//
//		for r := int64(0); r < l+1; r++ {
//			round, err := poset.Store.GetRound(r)
//			if err != null {
//				t.Fatal(err)
//			}
//			var witnessNames String[]
//			for _, w := range round.Witnesses() {
//				witnessNames = append(witnessNames, getName(index, w))
//			}
//			t.Logf("round %d witnesses: %v", r, witnessNames)
//		}
//
//		// Rounds 0,1,2,3,4 and 5 should be decided.
//		expPendingRounds := []pendingRound{
//			{Index: 6, Decided: false},
//			{Index: 7, Decided: false},
//		}
//		for i, pd := range poset.PendingRounds {
//			if !reflect.DeepEqual(*pd, expPendingRounds[i]) {
//				t.Fatalf("pending round %d should be %v, not %v",
//					i, expPendingRounds[i], *pd)
//			}
//		}
//
//		expBlockTxCounts := HashMap<Long,Long>{0: 4, 1: 3, 2: 5, 3: 7, 4: 3}
//
//		for bi := int64(0); bi < 5; bi++ {
//			b, err := poset.Store.GetBlock(bi)
//			if err != null {
//				t.Fatal(err)
//			}
//			for i, tx := range b.Transactions() {
//				t.Logf("block %d, tx %d: %s", bi, i, string(tx))
//			}
//			if txs := int64(len(b.Transactions())); txs != expBlockTxCounts[bi] {
//				t.Fatalf("Blocks[%d] should contain %d transactions, not %d", bi,
//					expBlockTxCounts[bi], txs)
//			}
//		}
//	}
//
//	@Test
//	public void TestFunkyPosetFrames() {
//		p, index := initFunkyPoset(t, common.NewTestLogger(t), true)
//
//		participants := poset.Participants.ToPeerSlice()
//
//		if err := poset.DivideRounds(); err != null {
//			t.Fatal(err)
//		}
//		if err := poset.DecideFame(); err != null {
//			t.Fatal(err)
//		}
//		if err := poset.DecideRoundReceived(); err != null {
//			t.Fatal(err)
//		}
//		if err := poset.ProcessDecidedRounds(); err != null {
//			t.Fatal(err)
//		}
//
//		for bi := int64(0); bi < 5; bi++ {
//			block, err := poset.Store.GetBlock(bi)
//			if err != null {
//				t.Fatal(err)
//			}
//
//			frame, err := poset.GetFrame(block.RoundReceived())
//			for k, em := range frame.Events {
//				e := em.ToEvent()
//				ev := &e
//				r, _ := poset.round(ev.Hex())
//				t.Logf("frame %d events %d: %s, round %d",
//					frame.Round, k, getName(index, ev.Hex()), r)
//			}
//			for k, r := range frame.Roots {
//				t.Logf("frame %d root %d: next round %d, self parent: %v,"+
//					" others: %v", frame.Round, k, r.NextRound,
//					r.SelfParent, r.Others)
//			}
//		}
//
//		expFrameRoots := map[int64][]Root{
//			1: {
//				NewBaseRoot(participants[0).GetID()),
//				NewBaseRoot(participants[1).GetID()),
//				NewBaseRoot(participants[2).GetID()),
//				NewBaseRoot(participants[3).GetID()),
//			},
//			2: {
//				NewBaseRoot(participants[0).GetID()),
//				{
//					NextRound: 1,
//					SelfParent: &RootEvent{Hash: index[w01],
//						CreatorID: participants[1).GetID(), Index: 0,
//						LamportTimestamp: 0, Round: 0},
//					Others: HashMap<String,RootEvent>{
//						index[a12]: {Hash: index[a23],
//							CreatorID: participants[2).GetID(), Index: 1,
//							LamportTimestamp: 1, Round: 0},
//					},
//				},
//				{
//					NextRound: 1,
//					SelfParent: &RootEvent{Hash: index[a23],
//						CreatorID: participants[2).GetID(), Index: 1,
//						LamportTimestamp: 1, Round: 0},
//					Others: HashMap<String,RootEvent>{
//						index[a21]: {Hash: index[a12],
//							CreatorID: participants[1).GetID(), Index: 1,
//							LamportTimestamp: 2, Round: 1},
//					},
//				},
//				{
//					NextRound: 1,
//					SelfParent: &RootEvent{Hash: index[w03],
//						CreatorID: participants[3).GetID(), Index: 0,
//						LamportTimestamp: 0, Round: 0},
//					Others: HashMap<String,RootEvent>{
//						index[w13]: {Hash: index[a21],
//							CreatorID: participants[2).GetID(), Index: 2,
//							LamportTimestamp: 3, Round: 1},
//					},
//				},
//			},
//			3: {
//				NewBaseRoot(participants[0).GetID()),
//				{
//					NextRound: 1,
//					SelfParent: &RootEvent{Hash: index[a12],
//						CreatorID: participants[1).GetID(), Index: 1,
//						LamportTimestamp: 2, Round: 1},
//					Others: HashMap<String,RootEvent>{
//						index[a10]: {Hash: index[a00],
//							CreatorID: participants[0).GetID(), Index: 1,
//							LamportTimestamp: 1, Round: 0},
//					},
//				},
//				{
//					NextRound: 2,
//					SelfParent: &RootEvent{Hash: index[a21],
//						CreatorID: participants[2).GetID(), Index: 2,
//						LamportTimestamp: 3, Round: 1},
//					Others: HashMap<String,RootEvent>{
//						index[w12]: {Hash: index[w13],
//							CreatorID: participants[3).GetID(), Index: 1,
//							LamportTimestamp: 4, Round: 1},
//					},
//				},
//				{
//					NextRound: 1,
//					SelfParent: &RootEvent{Hash: index[w03],
//						CreatorID: participants[3).GetID(), Index: 0,
//						LamportTimestamp: 0, Round: 0},
//					Others: HashMap<String,RootEvent>{
//						index[w13]: {Hash: index[a21],
//							CreatorID: participants[2).GetID(), Index: 2,
//							LamportTimestamp: 3, Round: 1},
//					},
//				},
//			},
//			4: {
//				{
//					NextRound: 2,
//					SelfParent: &RootEvent{Hash: index[a00],
//						CreatorID: participants[0).GetID(), Index: 1,
//						LamportTimestamp: 1, Round: 0},
//					Others: HashMap<String,RootEvent>{
//						index[w10]: {Hash: index[w11],
//							CreatorID: participants[1).GetID(), Index: 3,
//							LamportTimestamp: 6, Round: 2},
//					},
//				},
//				{
//					NextRound: 3,
//					SelfParent: &RootEvent{Hash: index[w11],
//						CreatorID: participants[1).GetID(), Index: 3,
//						LamportTimestamp: 6, Round: 2},
//					Others: HashMap<String,RootEvent>{
//						index[w21]: {Hash: index[w23],
//							CreatorID: participants[3).GetID(), Index: 2,
//							LamportTimestamp: 8, Round: 2},
//					},
//				},
//				{
//					NextRound: 2,
//					SelfParent: &RootEvent{Hash: index[w12],
//						CreatorID: participants[2).GetID(), Index: 3,
//						LamportTimestamp: 5, Round: 2},
//					Others: HashMap<String,RootEvent>{
//						index[b21]: {Hash: index[w11],
//							CreatorID: participants[1).GetID(), Index: 3,
//							LamportTimestamp: 6, Round: 2},
//					},
//				},
//				{
//					NextRound: 2,
//					SelfParent: &RootEvent{Hash: index[w13],
//						CreatorID: participants[3).GetID(), Index: 1,
//						LamportTimestamp: 4, Round: 1},
//					Others: HashMap<String,RootEvent>{
//						index[w23]: {Hash: index[b21],
//							CreatorID: participants[2).GetID(), Index: 4,
//							LamportTimestamp: 7, Round: 2},
//					},
//				},
//			},
//			5: {
//				{
//					NextRound: 4,
//					SelfParent: &RootEvent{Hash: index[b00],
//						CreatorID: participants[0).GetID(), Index: 3,
//						LamportTimestamp: 8, Round: 3},
//					Others: HashMap<String,RootEvent>{
//						index[w20]: {Hash: index[w22],
//							CreatorID: participants[2).GetID(), Index: 5,
//							LamportTimestamp: 11, Round: 3},
//					},
//				},
//				{
//					NextRound: 4,
//					SelfParent: &RootEvent{Hash: index[c10],
//						CreatorID: participants[1).GetID(), Index: 5,
//						LamportTimestamp: 10, Round: 3},
//					Others: HashMap<String,RootEvent>{
//						index[w31]: {Hash: index[w20],
//							CreatorID: participants[0).GetID(), Index: 4,
//							LamportTimestamp: 12, Round: 4},
//					},
//				},
//				{
//					NextRound: 4,
//					SelfParent: &RootEvent{Hash: index[w22],
//						CreatorID: participants[2).GetID(), Index: 5,
//						LamportTimestamp: 11, Round: 3},
//					Others: HashMap<String,RootEvent>{
//						index[w32]: {Hash: index[w31],
//							CreatorID: participants[1).GetID(), Index: 6,
//							LamportTimestamp: 13, Round: 4},
//					},
//				},
//				{
//					NextRound: 2,
//					SelfParent: &RootEvent{Hash: index[w13],
//						CreatorID: participants[3).GetID(), Index: 1,
//						LamportTimestamp: 4, Round: 1},
//					Others: HashMap<String,RootEvent>{
//						index[w23]: {Hash: index[b21],
//							CreatorID: participants[2).GetID(), Index: 4,
//							LamportTimestamp: 7, Round: 2},
//					},
//				},
//			},
//		}
//
//		for bi := int64(0); bi < 5; bi++ {
//			block, err := poset.Store.GetBlock(bi)
//			if err != null {
//				t.Fatal(err)
//			}
//
//			frame, err := poset.GetFrame(block.RoundReceived())
//			if err != null {
//				t.Fatal(err)
//			}
//
//			for k, r := range frame.Roots {
//				compareRoots(t, r, &expFrameRoots[frame.Round][k], index)
//			}
//		}
//	}
//
//	@Test
//	public void TestFunkyPosetReset() {
//		p, index := initFunkyPoset(t, common.NewTestLogger(t), true)
//
//		poset.DivideRounds()
//		poset.DecideFame()
//		poset.DecideRoundReceived()
//		poset.ProcessDecidedRounds()
//
//		for bi := int64(0); bi < 3; bi++ {
//			block, err := poset.Store.GetBlock(bi)
//			if err != null {
//				t.Fatal(err)
//			}
//
//			frame, err := poset.GetFrame(block.RoundReceived())
//			if err != null {
//				t.Fatal(err)
//			}
//
//			// This operation clears the private fields which need to be recomputed
//			// in the Events (round, roundReceived,etc)
//			marshalledFrame, _ := frame.ProtoMarshal()
//			unmarshalledFrame := new(Frame)
//			unmarshalledFrame.ProtoUnmarshal(marshalledFrame)
//
//			p2 := NewPoset(poset.Participants,
//				NewInmemStore(poset.Participants, cacheSize),
//				null,
//				testLogger(t))
//			err = p2.Reset(block, *unmarshalledFrame)
//			if err != null {
//				t.Fatal(err)
//			}
//
//			// Test continue after reset
//			// Compute diff
//			p2Known := p2.Store.KnownEvents()
//			diff := getDiff(p, p2Known, t)
//
//			wireDiff := make([]WireEvent, len(diff), len(diff))
//			for i, e := range diff {
//				wireDiff[i] = e.ToWire()
//			}
//
//			// Insert remaining Events into the Reset poset
//			for i, wev := range wireDiff {
//				ev, err := p2.ReadWireInfo(wev)
//				if err != null {
//					t.Fatalf("Reading WireInfo for %s: %s",
//						getName(index, diff[i].Hex()), err)
//				}
//				err = p2.InsertEvent(*ev, false)
//				if err != null {
//					t.Fatal(err)
//				}
//			}
//
//			p2.DivideRounds()
//			p2.DecideFame()
//			p2.DecideRoundReceived()
//			p2.ProcessDecidedRounds()
//
//			compareRoundWitnesses(p, p2, index, bi, true, t)
//		}
//
//	}
//
//	/*
//
//	    |  <w51>  |    |
//	    |    |    \    |
//		|    |    | \  |  7
//	    |    |    |  <i32>-
//	    |    |    | /  |  6
//	    |    |  [w42]  |
//	    |    |  / |    |
//	    |  [w41]  |    |
//		|    |   \     |
//		|    |    | \  |  6
//	    |    |    |  [w43]-
//		|    |    | /  |  5
//	    |    | <h21>   |
//	    |    | /  |    |
//	    |  <w31>  |    |
//		|    |   \     |
//		|    |    | \  |  5
//	    |    |    |  <w33>-
//	    |    |    | /  |  4
//	    |    |  [w32]  |
//		|    | /  |    |
//	    |  [g13]  |    |
//		|    |   \     |
//		|    |    | \  |  4
//	    |    |    |  [w23]-
//	    |    |    | /  |  3
//	    |    |  <w22>  |
//	    |    | /  |    |
//	    |  <w21>  |    |
//		|	 |	 \	   |
//		|    |      \  |  3
//	    |    |    |  <w13>-
//	    |    |    | /  |  2
//	    |    |  [w12]  |
//	    |     /   |    |
//		|  / |    |    |
//	  [f01]  |    |    |
//		| \  |    |    |  2
//	    |  [w11]  |    |-
//	    | /  |    |    |  1
//	  <w10>  |    |    |
//	    |    \    |    |
//	    |    |    \    |
//	    |    |    |  <e32>
//	    |    |    | /  |  1
//	    |    |  <e21>  |-
//	    |    | /  |    |  0
//	    |   e10   |    |
//	    |  / |    |    |
//	  [w00][w01][w02][w03]
//		|    |    |    |
//	    R0   R1   R2   R3
//		0	 1	  2	   3
//	*/
//
//	public initSparsePoset(Logger logger) {
//		initPosetNodes(4);
//
//		for i, peer := range participants.ToPeerSlice() {
//			name = String.format("w0%d", i);
//			event = new Event(new byte[][]{[]byte(name)}, null,
//				null, new String[]{rootSelfParent(peer.ID), ""}, nodes[i].Pub, 0,
//				HashMap<String,Long>{rootSelfParent(peer.ID): 1});
//			nodes[i].signAndAddEvent(event, name, index, orderedEvents)
//		}
//
//		play[] plays = new play[]{
//			new play(1, 1, w01, w00, e10, new byte[][]{[]byte(e10)),
//				null, new String[]{w00, w01}),
//			new play(2, 1, w02, e10, e21, new byte[][]{[]byte(e21)},
//					null, new String[]{w00, w01, w02}),
//			new play(3, 1, w03, e21, e32, new byte[][]{[]byte(e32)},
//					null, new String[]{e21, w03}),
//			new play(0, 1, w00, e32, w10, new byte[][]{[]byte(w10)},
//					null, new String[]{e21, e32, w00}),
//			new play(1, 2, e10, w10, w11, new byte[][]{[]byte(w11)},
//					null, new String[]{w10, e32, e21, w01, w00}),
//			new play(0, 2, w10, w11, f01, new byte[][]{[]byte(f01)},
//					null, new String[]{w11, w10, e32, e21}),
//			new play(2, 2, e21, f01, w12, new byte[][]{[]byte(w12)},
//					null, new String[]{f01, w11, e21}),
//			new play(3, 2, e32, w12, w13, new byte[][]{[]byte(w13)},
//					null, new String[]{w12, f01, w11, e32, e21}),
//			new play(1, 3, w11, w13, w21, new byte[][]{[]byte(w21)},
//					null, new String[]{w13, w11}),
//			new play(2, 3, w12, w21, w22, new byte[][]{[]byte(w22)},
//					null, new String[]{w21, w13, w12, f01, w11}),
//			new play(3, 3, w13, w22, w23, new byte[][]{[]byte(w23)},
//					null, new String[]{w22, w21, w13}),
//			new play(1, 4, w21, w23, g13, new byte[][]{[]byte(g13)},
//					null, new String[]{w23, w21, w13}),
//			new play(2, 4, w22, g13, w32, new byte[][]{[]byte(w32)},
//					null, new String[]{g13, w23, w22, w21, w13}),
//			new play(3, 4, w23, w32, w33, new byte[][]{[]byte(w33)},
//					null, new String[]{w32, g13, w23}),
//			new play(1, 5, g13, w33, w31, new byte[][]{[]byte(w31)},
//					null, new String[]{w33, g13, w23}),
//			new play(2, 5, w32, w31, h21, new byte[][]{[]byte(h21)},
//					null, new String[]{w31, w33, w32, g13, w23}),
//			new play(3, 5, w33, h21, w43, new byte[][]{[]byte(w43)},
//					null, new String[]{h21, w31, w33}),
//			new play(1, 6, w31, w43, w41, new byte[][]{[]byte(w41)},
//					null, new String[]{w43, w31, w33}),
//			new play(2, 6, h21, w41, w42, new byte[][]{[]byte(w42)},
//					null, new String[]{w41, w43, h21, w31, w33}),
//			new play(3, 6, w43, w42, i32, new byte[][]{[]byte(i32)},
//					null, new String[]{w42, w41, w43}),
//			new play(1, 7, w41, i32, w51, new byte[][]{[]byte(w51)},
//					null, new String[]{i32, w41, w43})
//		};
//
//		playEvents(plays, nodes, index, orderedEvents)
//
//		poset := createPoset(t, false, orderedEvents, participants,
//			logger.WithField("test", 6))
//
//		return poset, index
//	}
//
//	@Test
//	public void TestSparsePosetFrames() {
//		p, index := initSparsePoset(t, common.NewTestLogger(t))
//
//		participants := poset.Participants.ToPeerSlice()
//
//		if err := poset.DivideRounds(); err != null {
//			t.Fatal(err)
//		}
//		if err := poset.DecideFame(); err != null {
//			t.Fatal(err)
//		}
//		if err := poset.DecideRoundReceived(); err != null {
//			t.Fatal(err)
//		}
//		if err := poset.ProcessDecidedRounds(); err != null {
//			t.Fatal(err)
//		}
//
//		for bi := int64(0); bi < 5; bi++ {
//			block, err := poset.Store.GetBlock(bi)
//			if err != null {
//				t.Fatal(err)
//			}
//
//			frame, err := poset.GetFrame(block.RoundReceived())
//			for k, ev := range frame.Events {
//				ev.Body.Hash()
//				hash, err := ev.Body.Hash()
//				if err != null {
//					t.Fatal(err)
//				}
//				hex := fmt.Sprintf("0x%X", hash)
//				r, err := poset.round(hex)
//				if err != null {
//					t.Fatal(err)
//				}
//				t.Logf("frame %d event %d: %s, round %d",
//					frame.Round, k, getName(index, hex), r)
//			}
//			for k, r := range frame.Roots {
//				sp := getName(index, r.SelfParent.Hash)
//				var ops String[]
//				for k := range r.Others {
//					ops = append(ops, getName(index, k))
//				}
//
//				t.Logf("frame %d root %d: self parent index %s:"+
//					" %v, others indexes %s: %v", frame.Round, k, sp,
//					r.SelfParent, ops, r.Others)
//			}
//		}
//
//		expectedFrameRoots := map[int64][]Root{
//			1: {
//				NewBaseRoot(participants[0).GetID()),
//				NewBaseRoot(participants[1).GetID()),
//				NewBaseRoot(participants[2).GetID()),
//				NewBaseRoot(participants[3).GetID()),
//			},
//			2: {
//				{
//					NextRound: 1,
//					SelfParent: &RootEvent{Hash: index[w00],
//						CreatorID: participants[0).GetID(), Index: 0,
//						LamportTimestamp: 0, Round: 0},
//					Others: HashMap<String,RootEvent>{
//						index[w10]: {Hash: index[e32],
//							CreatorID: participants[3).GetID(), Index: 1,
//							LamportTimestamp: 3, Round: 1},
//					},
//				},
//				{
//					NextRound: 0,
//					SelfParent: &RootEvent{Hash: index[w01],
//						CreatorID: participants[1).GetID(), Index: 0,
//						LamportTimestamp: 0, Round: 0},
//					Others: HashMap<String,RootEvent>{
//						index[e10]: {Hash: index[w00],
//							CreatorID: participants[0).GetID(), Index: 0,
//							LamportTimestamp: 0, Round: 0},
//					},
//				},
//				{
//					NextRound: 1,
//					SelfParent: &RootEvent{Hash: index[w02],
//						CreatorID: participants[2).GetID(), Index: 0,
//						LamportTimestamp: 0, Round: 0},
//					Others: HashMap<String,RootEvent>{
//						index[e21]: {Hash: index[e10],
//							CreatorID: participants[1).GetID(), Index: 1,
//							LamportTimestamp: 1, Round: 0},
//					},
//				},
//				NewBaseRoot(participants[3).GetID()),
//			},
//			3: {
//				{
//					NextRound: 2,
//					SelfParent: &RootEvent{Hash: index[w10],
//						CreatorID: participants[0).GetID(), Index: 1,
//						LamportTimestamp: 4, Round: 1},
//					Others: HashMap<String,RootEvent>{
//						index[f01]: {Hash: index[w11],
//							CreatorID: participants[1).GetID(), Index: 2,
//							LamportTimestamp: 5, Round: 2},
//					},
//				},
//				{
//					NextRound: 2,
//					SelfParent: &RootEvent{Hash: index[e10],
//						CreatorID: participants[1).GetID(), Index: 1,
//						LamportTimestamp: 1, Round: 0},
//					Others: HashMap<String,RootEvent>{
//						index[w11]: {Hash: index[w10],
//							CreatorID: participants[0).GetID(), Index: 1,
//							LamportTimestamp: 4, Round: 1},
//					},
//				},
//				{
//					NextRound: 2,
//					SelfParent: &RootEvent{Hash: index[e21],
//						CreatorID: participants[2).GetID(), Index: 1,
//						LamportTimestamp: 2, Round: 1},
//					Others: HashMap<String,RootEvent>{
//						index[w12]: {Hash: index[f01],
//							CreatorID: participants[0).GetID(), Index: 2,
//							LamportTimestamp: 6, Round: 2},
//					},
//				},
//				{
//					NextRound: 1,
//					SelfParent: &RootEvent{Hash: index[w03],
//						CreatorID: participants[3).GetID(), Index: 0,
//						LamportTimestamp: 0, Round: 0},
//					Others: HashMap<String,RootEvent>{
//						index[e32]: {Hash: index[e21],
//							CreatorID: participants[2).GetID(), Index: 1,
//							LamportTimestamp: 2, Round: 1},
//					},
//				},
//			},
//			4: {
//				{
//					NextRound: 2,
//					SelfParent: &RootEvent{Hash: index[w10],
//						CreatorID: participants[0).GetID(), Index: 1,
//						LamportTimestamp: 4, Round: 1},
//					Others: HashMap<String,RootEvent>{
//						index[f01]: {Hash: index[w11],
//							CreatorID: participants[1).GetID(), Index: 2,
//							LamportTimestamp: 5, Round: 2},
//					},
//				},
//				{
//					NextRound: 3,
//					SelfParent: &RootEvent{Hash: index[w11],
//						CreatorID: participants[1).GetID(), Index: 2,
//						LamportTimestamp: 5, Round: 2},
//					Others: HashMap<String,RootEvent>{
//						index[w21]: {Hash: index[w13],
//							CreatorID: participants[3).GetID(), Index: 2,
//							LamportTimestamp: 8, Round: 3},
//					},
//				},
//				{
//					NextRound: 3,
//					SelfParent: &RootEvent{Hash: index[w12],
//						CreatorID: participants[2).GetID(), Index: 2,
//						LamportTimestamp: 7, Round: 2},
//					Others: HashMap<String,RootEvent>{
//						index[w22]: {Hash: index[w21],
//							CreatorID: participants[1).GetID(), Index: 3,
//							LamportTimestamp: 9, Round: 3},
//					},
//				},
//				{
//					NextRound: 3,
//					SelfParent: &RootEvent{Hash: index[e32],
//						CreatorID: participants[3).GetID(), Index: 1,
//						LamportTimestamp: 3, Round: 1},
//					Others: HashMap<String,RootEvent>{
//						index[w13]: {Hash: index[w12],
//							CreatorID: participants[2).GetID(), Index: 2,
//							LamportTimestamp: 7, Round: 2},
//					},
//				},
//			},
//			5: {
//				{
//					NextRound: 2,
//					SelfParent: &RootEvent{Hash: index[w10],
//						CreatorID: participants[0).GetID(), Index: 1,
//						LamportTimestamp: 4, Round: 1},
//					Others: HashMap<String,RootEvent>{
//						index[f01]: {Hash: index[w11],
//							CreatorID: participants[1).GetID(), Index: 2,
//							LamportTimestamp: 5, Round: 2},
//					},
//				},
//				{
//					NextRound: 4,
//					SelfParent: &RootEvent{Hash: index[w21],
//						CreatorID: participants[1).GetID(), Index: 3,
//						LamportTimestamp: 9, Round: 3},
//					Others: HashMap<String,RootEvent>{
//						index[g13]: {Hash: index[w23],
//							CreatorID: participants[3).GetID(), Index: 3,
//							LamportTimestamp: 11, Round: 4},
//					},
//				},
//				{
//					NextRound: 4,
//					SelfParent: &RootEvent{Hash: index[w22],
//						CreatorID: participants[2).GetID(), Index: 3,
//						LamportTimestamp: 10, Round: 3},
//					Others: HashMap<String,RootEvent>{
//						index[w32]: {Hash: index[g13],
//							CreatorID: participants[1).GetID(), Index: 4,
//							LamportTimestamp: 12, Round: 4},
//					},
//				},
//				{
//					NextRound: 4,
//					SelfParent: &RootEvent{Hash: index[w13],
//						CreatorID: participants[3).GetID(), Index: 2,
//						LamportTimestamp: 8, Round: 3},
//					Others: HashMap<String,RootEvent>{
//						index[w23]: {Hash: index[w22],
//							CreatorID: participants[2).GetID(), Index: 3,
//							LamportTimestamp: 10, Round: 3},
//					},
//				},
//			},
//		}
//
//		for bi := int64(0); bi < 5; bi++ {
//			block, err := poset.Store.GetBlock(bi)
//			if err != null {
//				t.Fatal(err)
//			}
//
//			frame, err := poset.GetFrame(block.RoundReceived())
//			if err != null {
//				t.Fatal(err)
//			}
//
//			for k, r := range frame.Roots {
//				compareRoots(t, r, &expectedFrameRoots[frame.Round][k], index)
//			}
//		}
//	}
//
//	@Test
//	public void TestSparsePosetReset() {
//		p, index := initSparsePoset(t, common.NewTestLogger(t))
//
//		poset.DivideRounds()
//		poset.DecideFame()
//		poset.DecideRoundReceived()
//		poset.ProcessDecidedRounds()
//
//		for bi := 0; bi < 5; bi++ {
//			block, err := poset.Store.GetBlock(int64(bi))
//			if err != null {
//				t.Fatal(err)
//			}
//
//			frame, err := poset.GetFrame(block.RoundReceived())
//			if err != null {
//				t.Fatal(err)
//			}
//
//			// This operation clears the private fields which need to be recomputed
//			// in the Events (round, roundReceived,etc)
//			marshalledFrame, _ := frame.ProtoMarshal()
//			unmarshalledFrame := new(Frame)
//			unmarshalledFrame.ProtoUnmarshal(marshalledFrame)
//
//			p2 := NewPoset(poset.Participants,
//				NewInmemStore(poset.Participants, cacheSize),
//				null,
//				testLogger(t))
//			err = p2.Reset(block, *unmarshalledFrame)
//			if err != null {
//				t.Fatal(err)
//			}
//
//			// Test continue after Reset
//
//			// Compute diff
//			p2Known := p2.Store.KnownEvents()
//			diff := getDiff(p, p2Known, t)
//
//			t.Logf("p2.Known: %v", p2Known)
//			t.Logf("diff: %v", len(diff))
//
//			wireDiff := make([]WireEvent, len(diff), len(diff))
//			for i, e := range diff {
//				wireDiff[i] = e.ToWire()
//			}
//
//			// Insert remaining Events into the Reset poset
//			for i, wev := range wireDiff {
//				eventName := getName(index, diff[i].Hex())
//				ev, err := p2.ReadWireInfo(wev)
//				if err != null {
//					t.Fatalf("ReadWireInfo(%s): %s", eventName, err)
//				}
//				compareEventMessages(t, &ev.Message, &diff[i].Message, index)
//				err = p2.InsertEvent(*ev, false)
//				if err != null {
//					t.Fatalf("InsertEvent(%s): %s", eventName, err)
//				}
//			}
//
//			p2.DivideRounds()
//			p2.DecideFame()
//			p2.DecideRoundReceived()
//			p2.ProcessDecidedRounds()
//
//			compareRoundWitnesses(p, p2, index, int64(bi), true, t)
//		}
//
//	}

	public boolean contains(String[] s, String x)  {
		for (String e: s) {
			if (e.equals(x)) {
				return true;
			}
		}
		return false;
	}

	public void compareRoundWitnesses(Poset p, Poset p2, HashMap<String,String> index, long round, boolean check) {

		for (int i = (int) round; i <= 5; i++) {
			RResult<RoundInfo> getRound = poset.Store.getRound(i);
			RoundInfo pRound = getRound.result;
			error err = getRound.err;
			assertNull("No error", err);

			getRound = p2.Store.getRound(i);
			RoundInfo p2Round = getRound.result;
			err = getRound.err;
			assertNull("No error", err);

			//Check Round1 Witnesses
			String[] pWitnesses = pRound.Witnesses();
			String[] p2Witnesses = p2Round.Witnesses();
			Arrays.sort(pWitnesses);
			Arrays.sort(p2Witnesses);
			String[] hwn = new String[pWitnesses.length];
			String[] p2wn = new String[p2Witnesses.length];
			for (String w : pWitnesses) {
				hwn = Appender.append(hwn, getName(index, w));
			}
			for (String w : p2Witnesses){
				p2wn = Appender.append(p2wn, getName(index, w));
			}

			if (check) {
				assertArrayEquals(String.format("Reset Hg Round %d witnesses should match", i), hwn, p2wn);
			}
		}

	}

	public Event[] getDiff(Poset p, HashMap<Long,Long> known) {
		Event[] diff = null;
		for (long id: known.keySet()) {
			long ct = known.get(id);
			String pk = poset.Participants.byId(id).getPubKeyHex();
			// get participant Events with index > ct
			RResult<String[]> pEventsCall = poset.Store.participantEvents(pk, ct);
			String[] participantEvents = pEventsCall.result;
			error err = pEventsCall.err;
			assertNull("No error", err);

			for (String e : participantEvents) {
				RResult<Event> getEvent = poset.Store.getEvent(e);
				Event ev = getEvent.result;
				err = getEvent.err;
				assertNull("No error", err);
				diff = Appender.append(diff, ev);
			}
		}
		Arrays.sort(diff, new EventComparatorByTopologicalOrder());
		return diff;
	}

	public String getName(Map<String,String> index, String hash)  {
		for (String name : index.keySet()) {
			String h = index.get(name);
			if (h.equals(hash)) {
				return name;
			}
		}
		return "";
	}

	public void compareRootEvents(RootEvent x, RootEvent exp, Map<String,String> index) {
		if (!x.Hash.equals(exp.Hash) || x.Index != exp.Index ||
			x.CreatorID != exp.CreatorID || x.Round != exp.Round ||
			x.LamportTimestamp != exp.LamportTimestamp) {
			fail(String.format("expected root event %s: %v, got %s: %v",
				getName(index, exp.Hash), exp, getName(index, x.Hash), x));
		}
	}

	public void compareOtherParents(Map<String,RootEvent> x, Map<String,RootEvent> exp,
		Map<String,String> index) {
		assertEquals("expected number of other parents", exp.size(), x.size());

		String[] others = null;
		for (String k : x.keySet()) {
			others = Appender.append(others, getName(index, k));
		}

		for (String k : exp.keySet()) {
			RootEvent v = exp.get(k);
			RootEvent root = x.get(k);
			assertNotNull(String.format("root %v exists in other roots", v), root);
			compareRootEvents(root, v, index);
		}
	}

	public void compareRoots(Root x, Root exp, HashMap<String,String> index) {
		compareRootEvents(x.SelfParent, exp.SelfParent, index);
		compareOtherParents(x.Others, exp.Others, index);
		assertEquals("expected next round should match", exp.NextRound, x.NextRound);
	}

	public void compareEventMessages(EventMessage x, EventMessage exp, HashMap<String,String> index) {
		if (!x.WitnessProof.equals(exp.WitnessProof) ||
			!x.FlagTable.equals(exp.FlagTable) ||
			!x.Signature.equals(exp.Signature)) {
			byte[] hash = exp.Body.Hash().result;
			String hex = crypto.Utils.toHexString(hash);
			assertEquals(String.format("message to event %s should match", getName(index, hex)), exp, x);
		}
		compareEventBody(x.Body, exp.Body);
	}

	public void compareEventBody(EventBody x, EventBody exp) {
		if (x.Index != exp.Index || !x.Creator.equals(exp.Creator) ||
			!x.BlockSignatures.equals(exp.BlockSignatures) ||
			!x.InternalTransactions.equals(exp.InternalTransactions) ||
			!x.Parents.equals(exp.Parents) ||
			!x.Transactions.equals(exp.Transactions)) {
			assertEquals("Event body should match", exp, x);
		}
	}
}