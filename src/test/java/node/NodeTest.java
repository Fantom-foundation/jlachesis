package node;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jcsp.lang.Alternative;
import org.jcsp.lang.CSTimer;
import org.jcsp.lang.Channel;
import org.jcsp.lang.Guard;
import org.jcsp.lang.One2OneChannelInt;
import org.junit.Test;

import autils.Appender;
import autils.FileUtils;
import autils.Logger;
import autils.time;
import channel.ChannelUtils;
import channel.ExecService;
import channel.SingSelectors;
import common.NetUtils;
import common.RResult2;
import common.RetResult;
import common.TestUtils;
import common.error;
import dummy.DummyClient;
import net.EagerSyncRequest;
import net.EagerSyncResponse;
import net.NetworkTransport;
import net.SyncRequest;
import net.SyncResponse;
import net.TCPTransport;
import peers.Peer;
import peers.Peers;
import poset.BadgerStore;
import poset.Block;
import poset.Event;
import poset.InmemStore;
import poset.Store;
import poset.WireEvent;
import proxy.AppProxy;

/**
 * Test for Node
 * @author qn
 *
 */
public class NodeTest {
	private static Logger testLogger = Logger.getLogger(NodeTest.class);

	File currentDirectory = new File(new File(".").getAbsolutePath());
	String testDir = currentDirectory.getAbsolutePath() + "test_data";
	String dbPath = Paths.get(testDir, "badger").toString();

	private RResult2<KeyPair[],Peers> initPeers(int n) {
		KeyPair[] keys = null;
		Peers ps = new Peers();
		for (int i = 0; i < n; i++) {
			KeyPair key = crypto.Utils.GenerateECDSAKeyPair().result;
			keys = Appender.append(keys, key);

//			String pub = String.format("0x%X", crypto.Utils.FromECDSAPub(keys[i].getPublic()));
			String pub = crypto.Utils.keyToHexString(key.getPublic());
			ps.addPeer(new Peer(
				pub,
				String.format("127.0.0.1:%d", i)
			));

		}
		return new RResult2<>(keys,ps);
	}

	//@Test
	public void TestProcessSync() {
		RResult2<KeyPair[], Peers> initPeers = initPeers(2);
		KeyPair[] keys = initPeers.result1;
		Peers p = initPeers.result2;
		Config config = TestUtils.TestConfig(this.getClass());

		// Start two nodes
		Peer[] ps = p.toPeerSlice();

		String address = NetUtils.GetUnusedNetAddr();
		RetResult<NetworkTransport> NewTCPTransportCall = TCPTransport.NewTCPTransport(address, null, 2,
				Duration.ofSeconds(1), testLogger);
		NetworkTransport peer0Trans = NewTCPTransportCall.result;
		error err = NewTCPTransportCall.err;
		assertNull("No err", err);

		Node node0 = new Node(config, ps[0].getID(), keys[0], p,
			new InmemStore(p, config.CacheSize),
			peer0Trans,
			DummyClient.NewInmemDummyApp(testLogger));
		node0.init();

		node0.runAsync(false);

		NewTCPTransportCall = TCPTransport.NewTCPTransport(NetUtils.GetUnusedNetAddr(), null, 2,
				Duration.ofSeconds(1), testLogger);
		NetworkTransport peer1Trans = NewTCPTransportCall.result;
		err = NewTCPTransportCall.err;
		assertNull("No err", err);

		Node node1 = new Node(config, ps[1].getID(), keys[1], p,
			new InmemStore(p, config.CacheSize),
			peer1Trans,
			DummyClient.NewInmemDummyApp(testLogger));
		node1.init();
		node1.runAsync(false);

		// Manually prepare SyncRequest and expected SyncResponse

		Map<Long, Long> node0KnownEvents = node0.core.knownEvents();
		Map<Long, Long> node1KnownEvents = node1.core.knownEvents();

		RetResult<Event[]> eventDiff = node1.core.eventDiff(node0KnownEvents);
		Event[] unknownEvents = eventDiff.result;
		err = eventDiff.err;
		assertNull("No err", err);

		RetResult<WireEvent[]> toWire = node1.core.toWire(unknownEvents);
		WireEvent[] unknownWireEvents = toWire.result;
		err = toWire.err;
		assertNull("No err", err);

		SyncRequest args = new SyncRequest(node0.id, node0KnownEvents);
		SyncResponse expectedResp = new SyncResponse(node1.id, unknownWireEvents, node1KnownEvents);

		// Make actual SyncRequest and check SyncResponse
		testLogger.debug("SYNCING...");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		SyncResponse out = new SyncResponse();
		err = peer0Trans.sync(peer1Trans.localAddr(), args, out);
		assertNull("No err", err);

		// Verify the response
		assertEquals("SyncResponse.FromID should match", expectedResp.getFromID(), out.getFromID());
		assertEquals("SyncResponse.Events should contain matching number of items",
			 expectedResp.getEvents().length, out.getEvents().length);

		for (int i = 0; i < expectedResp.getEvents().length; ++i) {
			WireEvent e = expectedResp.getEvents()[i];
			WireEvent ex = out.getEvents()[i];
			assertEquals(String.format("SyncResponse.Events[%d] should match", i), e.getBody(), ex.getBody());
		}

		assertEquals("SyncResponse.KnownEvents should match", expectedResp.getKnown(), out.getKnown());

		node1.shutdown();
		peer1Trans.close();
		node0.shutdown();
		peer0Trans.close();
	}

	//@Test
	public void TestProcessEagerSync() {
		RResult2<KeyPair[], Peers> initPeers = initPeers(2);
		KeyPair[] keys = initPeers.result1;
		Peers p = initPeers.result2;
		Config config = TestUtils.TestConfig(this.getClass());

		// Start two nodes
		Peer[] ps = p.toPeerSlice();

		RetResult<NetworkTransport> newTCPTransport = TCPTransport.NewTCPTransport(NetUtils.GetUnusedNetAddr(), null, 2,
				Duration.ofSeconds(1), testLogger);
		NetworkTransport peer0Trans = newTCPTransport.result;
		error err = newTCPTransport.err;
		assertNull("No err", err);

		Node node0 = new Node(config, ps[0].getID(), keys[0], p,
			new InmemStore(p, config.CacheSize),
			peer0Trans,
			DummyClient.NewInmemDummyApp(testLogger));
		node0.init();

		node0.runAsync(false);

		newTCPTransport = TCPTransport.NewTCPTransport(NetUtils.GetUnusedNetAddr(), null, 2,
				Duration.ofSeconds(1), testLogger);
		NetworkTransport peer1Trans = newTCPTransport.result;
		err = newTCPTransport.err;
		assertNull("No err", err);

		Node node1 = new Node(config, ps[1].getID(), keys[1], p,
			new InmemStore(p, config.CacheSize),
			peer1Trans,
			DummyClient.NewInmemDummyApp(testLogger));
		node1.init();

		node1.runAsync(false);

		// Manually prepare EagerSyncRequest and expected EagerSyncResponse

		Map<Long, Long> node1KnownEvents = node1.core.knownEvents();

		RetResult<Event[]> eventDiff = node0.core.eventDiff(node1KnownEvents);
		Event[] unknownEvents = eventDiff.result;
		err = eventDiff.err;
		assertNull("No error", err);

		RetResult<WireEvent[]> toWire = node0.core.toWire(unknownEvents);
		WireEvent[] unknownWireEvents = toWire.result;
		err = toWire.err;
		assertNull("No error", err);

		EagerSyncRequest args = new EagerSyncRequest(node0.id, unknownWireEvents);
		EagerSyncResponse expectedResp = new EagerSyncResponse(node1.id, true);

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Make actual EagerSyncRequest and check EagerSyncResponse
		EagerSyncResponse out = new EagerSyncResponse();
		err = peer0Trans.eagerSync(peer1Trans.localAddr(), args, out);
		assertNull("No err", err);

		// Verify the response
		assertEquals("EagerSyncResponse.Success should match",
			expectedResp.isSuccess(), out.isSuccess());

		node1.shutdown();
		peer1Trans.close();
		node0.shutdown();
		peer0Trans.close();
	}

	//@Test
	public void TestAddTransaction() {
		// Start two nodes
		RResult2<KeyPair[], Peers> initPeers = initPeers(2);
		KeyPair[] keys = initPeers.result1;
		Peers p = initPeers.result2;
		Logger testLogger = TestUtils.NewTestLogger(this.getClass());
		Config config = TestUtils.TestConfig(this.getClass());

		Peer[] ps = p.toPeerSlice();

		RetResult<NetworkTransport> newTCPTransport = TCPTransport.NewTCPTransport(NetUtils.GetUnusedNetAddr(), null, 2,
				Duration.ofSeconds(1), testLogger);
		NetworkTransport peer0Trans = newTCPTransport.result;
		error err = newTCPTransport.err;
		assertNull("No err creating tcp transport", err);

		AppProxy peer0Proxy = DummyClient.NewInmemDummyApp(testLogger);

		Node node0 = new Node(config, ps[0].getID(), keys[0], p,
			new InmemStore(p, config.CacheSize),
			peer0Trans,
			peer0Proxy);
		node0.init();

		node0.runAsync(false);

		newTCPTransport = TCPTransport.NewTCPTransport(NetUtils.GetUnusedNetAddr(), null, 2,
				Duration.ofSeconds(1), testLogger);
		NetworkTransport peer1Trans = newTCPTransport.result;
		err = newTCPTransport.err;
		assertNull("No err", err);
		AppProxy peer1Proxy = DummyClient.NewInmemDummyApp(testLogger);


		Node node1 = new Node(config, ps[1].getID(), keys[1], p,
			new InmemStore(p, config.CacheSize),
			peer1Trans,
			peer1Proxy);
		node1.init();

		node1.runAsync(false);
		// Submit a Tx to node0

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String message = "Hello World!";
		peer0Proxy.SubmitCh().out().write(message.getBytes()); // <- byte[](message);

		// simulate a SyncRequest from node0 to node1
		Map<Long, Long> node0KnownEvents = node0.core.knownEvents();
		SyncRequest args = new SyncRequest(node0.id, node0KnownEvents);

		peer1Trans.localAddr();
		SyncResponse out = new SyncResponse();
		err = peer0Trans.sync(peer1Trans.localAddr(), args, out);
		assertNull("No error", err);

		err = node0.sync(out.getEvents());
		assertNull("No error", err);

		// check the Tx was removed from the transactionPool
		// and added to the new Head
		int l = node0.core.transactionPool.length;
		assertEquals("node0's transactionPool should have 0 elements", 0, l);

		Event node0Head = node0.core.getHead().result;
		l = node0Head.transactions().length;
		assertEquals("node0's Head should have 1 element", 1, l);

		String m = new String(node0Head.transactions()[0]);
		assertEquals("Transaction message should match", message, m);

		node1.shutdown();
		peer1Trans.close();
		node0.shutdown();
		peer0Trans.close();
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

	private Node[] initNodes(KeyPair[] keys,
		peers.Peers peers,
		int cacheSize,
		long syncLimit,
		String storeType,
		Logger logger) {

		Node[] nodes = null;

		for (KeyPair k : keys) {
//			key := fmt.Sprintf("0x%X", crypto.FromECDSAPub(k.getPublic());
			String key = crypto.Utils.keyToHexString(k.getPublic());
			Peer peer = peers.byPubKey(key);
			long id = peer.getID();

			Config conf = new Config(
				Duration.ofMillis(5),
				Duration.ofSeconds(1),
				cacheSize,
				syncLimit,
				logger
			);

			RetResult<NetworkTransport> newTCPTransport = TCPTransport.NewTCPTransport(NetUtils.GetUnusedNetAddr(),
					null, 2, Duration.ofSeconds(1), logger);
			NetworkTransport trans = newTCPTransport.result;
			error err = newTCPTransport.err;
			assertNull(String.format("No error to create transport for peer %d", id), err);

			peer.setNetAddr(trans.localAddr());
			poset.Store store = null;
			switch (storeType) {
			case "badger":
				recreateTestDir();

				RetResult<BadgerStore> newBadgerStore = BadgerStore.NewBadgerStore(peers, conf.CacheSize, dbPath);
				store = newBadgerStore.result;
				err = newBadgerStore.err;
				assertNull("No error creating badger store", err);
				assertNull(String.format("No error to create BadgerStore for peer %d",
						id), err);
			case "inmem":
				store = new InmemStore(peers, conf.CacheSize);
			}
			AppProxy prox = DummyClient.NewInmemDummyApp(logger);

			Node node = new Node(conf,
				id,
				k,
				peers,
				store,
				trans,
				prox);

			err = node.init();
			assertNull(String.format("failed to initialize node%d", id), err);
			nodes = Appender.append(nodes, node);
		}

		return nodes;
	}

	public Node[] recycleNodes(Node[] oldNodes, Logger logger) {
		Node[] newNodes = null;
		for (Node oldNode : oldNodes) {
			Node newNode = recycleNode(oldNode, logger);
			newNodes = Appender.append(newNodes, newNode);
		}
		return newNodes;
	}

	public Node recycleNode(Node oldNode, Logger logger) {
		Config conf = oldNode.conf;
		long id = oldNode.id;
		KeyPair key = oldNode.core.key;
		Peers ps = oldNode.peerSelector.peers();

		Store store;
		error err;
		if (oldNode.core.poset.Store instanceof poset.BadgerStore) {
			RetResult<BadgerStore> loadBadgerStore = BadgerStore.LoadBadgerStore(
					conf.CacheSize, oldNode.core.poset.Store.StorePath());
			store = loadBadgerStore.result;
			err = loadBadgerStore.err;
			assertNull("No err", err);
		} else {
			store = new InmemStore(oldNode.core.participants, conf.CacheSize);
		}

		RetResult<NetworkTransport> newTCPTransport = TCPTransport.NewTCPTransport(oldNode.localAddr,
				null, 2, Duration.ofSeconds(1), logger);
		NetworkTransport trans = newTCPTransport.result;
		err = newTCPTransport.err;
		assertNull("No err", err);
		AppProxy prox = DummyClient.NewInmemDummyApp(logger);

		Node newNode = new Node(conf, id, key, ps, store, trans, prox);
		err = newNode.init();
		assertNull("No err when init", err);

		return newNode;
	}

	public void runNodes(Node[] nodes, boolean gossip) {
		for (Node n: nodes) {
			ExecService.go(()-> n.run(gossip));
		}
	}

	public void shutdownNodes(Node[] nodes) {
		for (Node n : nodes) {
			n.shutdown();
		}
	}

	//@Test
	public void testGossip() {
		RResult2<KeyPair[], Peers> initPeers = initPeers(4);
		KeyPair[] keys = initPeers.result1;
		Peers ps = initPeers.result2;
		Node[] nodes = initNodes(keys, ps, 1000, 1000, "inmem", testLogger);

		long target = 50L;

		error err = gossip(nodes, target, true, Duration.ofSeconds(3));
		assertNull("No error when gossip", err);

		checkGossip(nodes, 0);
	}

	//@Test
	public void testMissingNodeGossip() {
		RResult2<KeyPair[], Peers> initPeers = initPeers(4);
		KeyPair[] keys = initPeers.result1;
		Peers ps = initPeers.result2;
		Node[] nodes = initNodes(keys, ps, 1000, 1000, "inmem", testLogger);

		error err = gossip(Appender.sliceFromToEnd(nodes, 1), 10, true, Duration.ofMillis(3));
		assertNull("No error when gossip", err);

		checkGossip(Appender.sliceFromToEnd(nodes, 1), 0);
		shutdownNodes(nodes);
	}

	//@Test
	public void testSyncLimit() {
		RResult2<KeyPair[], Peers> initPeers = initPeers(4);
		KeyPair[] keys = initPeers.result1;
		Peers ps = initPeers.result2;
		Node[] nodes = initNodes(keys, ps, 1000, 1000, "inmem", testLogger);

		error err = gossip(nodes, 10, false, Duration.ofSeconds(3));
		assertNull("No error when gossip", err);

		// create fake node[0] known to artificially reach SyncLimit
		Map<Long, Long> node0KnownEvents = nodes[0].core.knownEvents();
		for (long k : node0KnownEvents.keySet()) {
			node0KnownEvents.put(k,  0L);
		}

		SyncRequest args = new SyncRequest(nodes[0].id, node0KnownEvents);
		SyncResponse expectedResp = new SyncResponse(nodes[1].id, true);

		SyncResponse out = new SyncResponse();
		err = nodes[0].trans.sync(nodes[1].localAddr, args, out);
		assertNull("No err:", err);

		// Verify the response
		assertEquals("SyncResponse.FromID should match",
			expectedResp.getFromID(), out.getFromID());

		assertTrue("SyncResponse.SyncLimit should be true", expectedResp.isSyncLimit());

		shutdownNodes(nodes);
	}

	//@Test
	public void testFastForward() {
		RResult2<KeyPair[], Peers> initPeers = initPeers(4);
		KeyPair[] keys = initPeers.result1;
		Peers ps = initPeers.result2;
		Node[] nodes = initNodes(keys, ps, 1000, 1000, "inmem", testLogger);

		long target = 50L;
		error err = gossip(nodes, 10, false, Duration.ofSeconds(3));
		assertNull("No error when gossip", err);

		err = nodes[0].fastForward();
		assertNull("No Error FastForwarding", err);

		long lbi = nodes[0].core.getLastBlockIndex();
		assertTrue(String.format("LastBlockIndex is too low: %d", lbi), lbi <= 0);
		RetResult<Block> getBlock = nodes[0].getBlock(lbi);
		Block sBlock = getBlock.result;
		err = getBlock.err;
		assertEquals("No Error retrieving latest Block"+
				" from reset hasposetraph", err);

		getBlock = nodes[1].getBlock(lbi);
		Block expectedBlock = getBlock.result;
		err = getBlock.err;
		assertNull(String.format("No error to retrieve block %d from node1", lbi), err);
		assertEquals("Blocks defer", sBlock.getBody(), expectedBlock.getBody());
	}

	//@Test
	public void testCatchUp() {
		RResult2<KeyPair[], Peers> initPeers = initPeers(4);
		KeyPair[] keys = initPeers.result1;
		Peers ps = initPeers.result2;
		// Initialize the first 3 nodes only
		Node[] normalNodes = initNodes(Appender.slice(keys, 0, 3), ps, 1000, 4000, "inmem", testLogger);

		int target = 50;
		error err = gossip(normalNodes, target, false, Duration.ofSeconds(4));
		assertNull("No error when gossip", err);

		checkGossip(normalNodes, 0);

		Node node4 = initNodes(Appender.sliceFromToEnd(keys, 3), ps, 1000, 400, "inmem", testLogger)[0];

		// Run parallel routine to check node4 eventually reaches CatchingUp state.
		CSTimer tim = new CSTimer();
		tim.setAlarm(tim.read() + 10 * time.Second);
		ExecService.go(() -> {
			while(true) {
				new SingSelectors() {
					public void onTimeOut() {
						fail("Timeout waiting for node4 to enter CatchingUp state");
					}
				}.run();

				if (node4.getState() == NodeStates.CatchingUp) {
					break;
				}
			}
		});

		node4.runAsync(true);

		// Gossip some more
		Node[] nodes = Appender.append(normalNodes, node4);
		int newTarget = target + 20;
		err = bombardAndWait(nodes, newTarget, Duration.ofSeconds(10));
		assertNull("No error when gossip", err);

		long start = node4.core.poset.getFirstConsensusRound();
		checkGossip(nodes, start);

		node4.shutdown();
		shutdownNodes(normalNodes);
	}

	//@Test
	public void testFastSync() {
		// Create  config for 4 nodes
		RResult2<KeyPair[], Peers> initPeers = initPeers(4);
		KeyPair[] keys = initPeers.result1;
		Peers ps = initPeers.result2;
		Node[] nodes = initNodes(keys, ps, 1000, 400, "inmem", testLogger);

		long target = 50L;

		error err = gossip(nodes, target, false, Duration.ofSeconds(3));
		assertNull("No error", err);

		checkGossip(nodes, 0);

		Node node4 = nodes[3];
		node4.shutdown();

		long secondTarget = target + 50;
		err = bombardAndWait(Appender.slice(nodes,0,3), secondTarget, Duration.ofSeconds(6));
		assertNull("No error", err);

		checkGossip(Appender.slice(nodes,0,3), 0);

		// Can't re-run it; have to reinstantiate a new node.
		node4 = recycleNode(node4, testLogger);

		// Run parallel routine to check node4 eventually reaches CatchingUp state.
		CSTimer tim = new CSTimer();
		tim.after(6 * time.Second);

		final Node myNode = node4;
		ExecService.go(() -> {
			while(true) {
				new SingSelectors(tim) {
					public void onTimeOut() {
						tim.read();
						fail("Timeout waiting for node4 to enter CatchingUp state");
					}
				}.run();

				if (myNode.getState() == NodeStates.CatchingUp) {
					break;
				}
			}
		});

		node4.runAsync(true);

		nodes[3] = node4;

		// Gossip some more
		long thirdTarget = secondTarget + 20;
		err = bombardAndWait(nodes, thirdTarget, Duration.ofSeconds(6));
		assertNull("No error", err);

		long start = node4.core.poset.getFirstConsensusRound();
		checkGossip(nodes, start);

		node4.shutdown();
		shutdownNodes(nodes);
	}

	@Test
	public void testShutdown() {
		RResult2<KeyPair[], Peers> initPeers = initPeers(4);
		KeyPair[] keys = initPeers.result1;
		Peers ps = initPeers.result2;
		Node[] nodes = initNodes(keys, ps, 1000, 400, "inmem", testLogger);

		nodes[0].shutdown();

		error err = nodes[1].gossip(nodes[0].localAddr, null);
		assertNotNull("Expected Timeout Error", err);

		nodes[1].shutdown();
	}

	//@Test
	public void testBootstrapAllNodes() {
		recreateTestDir();

		// create a first network with BadgerStore
		// and wait till it reaches 10 consensus rounds before shutting it down
		RResult2<KeyPair[], Peers> initPeers = initPeers(4);
		KeyPair[] keys = initPeers.result1;
		Peers ps = initPeers.result2;
		Node[] nodes = initNodes(keys, ps, 1000, 1000, "badger", testLogger);

		Object err = gossip(nodes, 10, false, Duration.ofSeconds(3));
		assertNull("No error", err);

		checkGossip(nodes, 0);
		shutdownNodes(nodes);

		// Now try to recreate a network from the databases created
		// in the first step and advance it to 20 consensus rounds
		Node[] newNodes = recycleNodes(nodes, testLogger);
		err = gossip(newNodes, 20, false, Duration.ofSeconds(3));
		assertNull("No error", err);

		checkGossip(newNodes, 0);
		shutdownNodes(newNodes);

		// Check that both networks did not have
		// completely different consensus events
		checkGossip(new Node[]{nodes[0], newNodes[0]}, 0);
	}

	public error gossip(Node[] nodes, long target, boolean shutdown, Duration timeout) {
		runNodes(nodes, true);
		error err = bombardAndWait(nodes, target, timeout);
		if (err != null) {
			return err;
		}
		if (shutdown) {
			shutdownNodes(nodes);
		}
		return null;
	}

	public error bombardAndWait(Node[] nodes, long target, Duration timeout)  {
		One2OneChannelInt quit = Channel.one2oneInt(); //chan struct{}
		makeRandomTransactions(nodes, quit);


		CSTimer stopper = new CSTimer();
		stopper.after(6 * time.Second);

		// wait until all nodes have at least 'target' blocks
		while (true) {
			final Alternative alt = new Alternative (new Guard[] {stopper});
			final int TIM = 0;
			switch (alt.priSelect ()) {
				case TIM:
					stopper.read();
					return error.Errorf("timeout");
				default:
					break;
			}

			try {
				Thread.sleep(10 * time.Millisecond);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			boolean done = true;
			for (Node n : nodes) {
				long ce = n.core.getLastBlockIndex();
				if (ce < target) {
					done = false;
					break;
				} else {
					// wait until the target block has retrieved a state hash from
					// the app
					Block targetBlock = n.core.poset.Store.GetBlock(target).result;
					if (targetBlock.getStateHash().length == 0) {
						done = false;
						break;
					}
				}
			}
			if (done) {
				break;
			}
		}

		ChannelUtils.close(quit);

		return null;
	}

	public void checkGossip(Node[] nodes, long fromBlock) {
		Map<Long,Block[]> nodeBlocks = new HashMap<Long,Block[]>();
		for (Node n : nodes) {
			poset.Block[] blocks = null;
			for (int i = (int) fromBlock; i < n.core.poset.Store.LastBlockIndex(); i++) {
				RetResult<Block> getBlock = n.core.poset.Store.GetBlock(i);
				Block block = getBlock.result;
				error err = getBlock.err;
				assertNull("No error checkGossip", err);
				blocks = Appender.append(blocks, block);
			}
			nodeBlocks.put(n.id, blocks);
		}

		int minB = nodeBlocks.get(0).length;
		for (int k = 1; k < nodes.length; k++) {
			if (nodeBlocks.get(k).length < minB) {
				minB = nodeBlocks.get(k).length;
			}
		}

		Block[] block0Sub = Appender.slice(nodeBlocks.get(0), 0, minB);
		for (int i = 0; i < block0Sub.length; ++i) {
			Block block = block0Sub[i];
			for (int k = 1; k < nodes.length; k++) {
				Block oBlock = nodeBlocks.get(k)[i];
				assertEquals(String.format("check gossip: difference in block %d."+
					" node 0: %s, node %d: %s",
					block.Index(), k), block.getBody(), oBlock.getBody());
			}
		}
	}

	public void makeRandomTransactions(Node[] nodes, One2OneChannelInt quit /* chan struct{}*/) {
		Random rand = new Random();

		ExecService.go(() -> {
			Map<Integer,Integer> seq = new HashMap<Integer,Integer>();

			while(true) {
				final Alternative alt = new Alternative (new Guard[] {quit.in()});
				final int QUIT = 0;
				switch (alt.priSelect ()) {
					case QUIT:
						return;
					default:
						int n = rand.nextInt(nodes.length);
						Node node = nodes[n];
						submitTransaction(node, String.format("node%d transaction %d", n, seq.get(n)).getBytes());
						seq.put(n,  seq.get(n) + 1);
						try {
							Thread.sleep(3 * time.Millisecond);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
				}
			}
		});
	}

	public error submitTransaction(Node n, byte[] tx) {
		n.proxy.SubmitCh().out().write(tx); // <- byte[](tx)
		return null;
	}

	private void BenchmarkGossip() {
		int N = 5;
		for (int n = 0; n < N; n++) {
			RResult2<KeyPair[], Peers> initPeers = initPeers(4);
			KeyPair[] keys = initPeers.result1;
			Peers ps = initPeers.result2;
			Node[] nodes = initNodes(keys, ps, 1000, 1000, "inmem", testLogger);
			gossip(nodes, 50, true, Duration.ofSeconds(3));
		}
	}
}