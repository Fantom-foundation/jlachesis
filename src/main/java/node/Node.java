package node;

import java.security.KeyPair;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Logger;
import org.jcsp.lang.Alternative;
import org.jcsp.lang.Channel;
import org.jcsp.lang.Guard;
import org.jcsp.lang.One2OneChannel;
import org.jcsp.lang.One2OneChannelInt;

import autils.Appender;
import autils.time;
import channel.ChannelUtils;
import channel.ExecService;
import common.RetResult;
import common.RetResult3;
import common.error;
import net.EagerSyncRequest;
import net.EagerSyncResponse;
import net.FastForwardRequest;
import net.FastForwardResponse;
import net.RPC;
import net.SyncRequest;
import net.SyncResponse;
import peers.Peer;
import peers.Peers;
import poset.Block;
import poset.BlockSignature;
import poset.Event;
import poset.Frame;
import poset.WireEvent;

public class Node extends NodeState {
	// TBD: how to transfer this nodeState into Java
	NodeState nodeState;

	Config conf;
	Logger logger;

	long id;
	Core core;
	Lock coreLock; // sync.Mutex

	String localAddr;

	PeerSelector peerSelector;
	Lock selectorLock;

	net.Transport trans;
	One2OneChannel<net.RPC> netCh; // <-chan net.RPC;

	proxy.AppProxy proxy;
	One2OneChannel<byte[]> submitCh; // TBD: chan []byte
	One2OneChannel<poset.InternalTransaction> submitInternalCh; // chan poset.InternalTransaction

	One2OneChannel<poset.Block> commitCh; // chan poset.Block

	One2OneChannel<Object> shutdownCh; // chan struct{}

	ControlTimer controlTimer;

	long start;
	int syncRequests;
	int syncErrors;

	boolean needBoostrap;
	AtomicLong gossipJobs;
	AtomicLong rpcJobs;

	public Node(Config conf,
			long id,
		KeyPair key,
		peers.Peers participants,
		poset.Store store,
		net.Transport trans,
		proxy.AppProxy proxy) {

		String localAddr = trans.LocalAddr();

		Peers pmap = store.Participants().result;

		One2OneChannel<poset.Block> commitCh = Channel.one2one(); // TBD // make(chan poset.Block, 400);
		Core core = new Core(id, key, pmap, store, commitCh, conf.getLogger());

		String pubKey = core.HexID();

		PeerSelector peerSelector = new SmartPeerSelector(participants, pubKey,
				new FlagtableContainer() {
					@Override
					public RetResult<Map<String, Long>> GetFlagTable() {
						return core.poset.GetFlagTableOfRandomUndeterminedEvent();
					}}
			);

		this.id = id;
		this.conf= conf;
		this.core = core;
		this.localAddr = localAddr;
		this.logger = conf.getLogger(); //.WithField("this_id", id);
		this.peerSelector = peerSelector;
		this.trans = trans;
		this.netCh = trans.Consumer();
		this.proxy = proxy;
		this.submitCh = proxy.SubmitCh();
		this.submitInternalCh = proxy.SubmitInternalCh();
		this.commitCh = commitCh;
		this.shutdownCh = Channel.one2one(); //make(chan struct{});
		this.controlTimer = ControlTimer.RandomControlTimer();
		this.start = System.nanoTime();
		this.gossipJobs = new AtomicLong(0);
		this.rpcJobs = new AtomicLong(0);

//		node.logger.WithField("peers", pmap).Debug("pmap")
//		node.logger.WithField("pubKey", pubKey).Debug("pubKey")

		needBoostrap = store.NeedBoostrap();

		// Initialize
		setState(NodeStates.Gossiping);
	}

	public error Init() {
		String[] peerAddresses = null;

		for (Peer p : peerSelector.Peers().ToPeerSlice()) {
			peerAddresses = Appender.append(peerAddresses, p.GetNetAddr());
		}
//		logger.WithField("peers", peerAddresses).Debug("Initialize Node")

		if (needBoostrap) {
			logger.debug("Bootstrap");
			error err = core.Bootstrap();
			if (err != null) {
				return err;
			}
		}

		return core.SetHeadAndSeq();
	}

	public void RunAsync(boolean gossip) {
		logger.debug("RunAsync(gossip bool)");
		ExecService.go(() -> Run(gossip));
	}

	public void Run(boolean gossip) {
		// The ControlTimer allows the background routines to control the
		// heartbeat timer when the node is in the Gossiping state. The timer should
		// only be running when there are uncommitted transactions in the system.
		ExecService.go(() -> controlTimer.Run(conf.HeartbeatTimeout));

		// Execute some background work regardless of the state of the node.
		// Process SubmitTx and CommitBlock requests
		ExecService.go(() -> doBackgroundWork());

		// pause before gossiping test transactions to allow all nodes come up
		try {
			Thread.sleep(Duration.ofSeconds(conf.getTestDelay()).toMillis());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Execute Node State Machine
		while (true) {
			// Run different routines depending on node state
			NodeStates state = nodeState.getState();
//			logger.WithField("state", state.String()).Debug("RunAsync(gossip bool)")

			switch (state) {
			case Gossiping:
				lachesis(gossip);
			case CatchingUp:
				fastForward();
			case Shutdown:
				return;
			default:
				break;
			}
		}
	}

	public void resetTimer() {
		if (!controlTimer.set) {
			Duration ts = conf.HeartbeatTimeout;
			//Slow gossip if nothing interesting to say
			if (core.poset.getPendingLoadedEvents() == 0 &&
				core.transactionPool.length == 0 &&
				core.blockSignaturePool.length == 0) {
				ts = Duration.ofSeconds(1); //(time.Second);
			}
			controlTimer.resetCh.out().write(ts);
		}
	}

	public void doBackgroundWork() {
		while (true) {
//			select {
//			case t := <- submitCh:
////				logger.Debug("Adding Transactions to Transaction Pool")
//				addTransaction(t);
//				resetTimer();
//			case t := <- submitInternalCh:
////				logger.Debug("Adding Internal Transaction")
//				addInternalTransaction(t);
//				resetTimer();
//			case block := <- commitCh:
////				logger.WithFields(logrus.Fields{
////					"index":          block.Index(),
////					"round_received": block.RoundReceived(),
////					"transactions":   len(block.Transactions()),
////				}).Debug("Adding EventBlock")
//				error err = commit(block);
//				if  (err != null) {
////					logger.WithField("error", err).Error("Adding EventBlock")
//				}
//			case <-shutdownCh:
//				return;
//			}

			final Alternative alt = new Alternative (new Guard[] {submitCh.in(), submitInternalCh.in(), commitCh.in(), shutdownCh.in()});
			final int SUBMIT = 0, SUBMIT_INT = 1, COMMIT = 2, SHUTDOWN = 3;
			switch (alt.priSelect ()) {
				// fall through
				case SUBMIT:
					byte[] t = submitCh.in().read();
					logger.debug("Adding Transactions to Transaction Pool");
					addTransaction(t);
					resetTimer();
					break;
				case SUBMIT_INT:
					poset.InternalTransaction t1 = submitInternalCh.in().read();
					logger.debug("Adding Internal Transaction");
					addInternalTransaction(t1);
					resetTimer();
				case COMMIT:
					Block block = commitCh.in().read();
					error err = commit(block);
					if  (err != null) {
//						logger.WithField("error", err).Error("Adding EventBlock")
					}
				case SHUTDOWN:
					shutdownCh.in().read();
					return;
			}
		}
	}

	// lachesis is interrupted when a gossip function, launched asynchronously, changes
	// the state from Gossiping to CatchingUp, or when the node is shutdow
	// Otherwise, it processes RPC requests, periodicaly initiates gossip while there
	// is something to gossip about, or waits.
	public void lachesis(boolean gossip) {
		One2OneChannelInt returnCh = Channel.one2oneInt(); // TBD // make(chan struct{}, 100)
		while (true) {
//			select {
//			case rpc := <-netCh:
//				goFunc(public() {
//					rpcJobs.increment();
//					logger.debug("Processing RPC");
//					processRPC(rpc);
//					resetTimer();
//					rpcJobs.decrement();
//				})
//			case <-controlTimer.tickCh:
//				if (gossip && gossipJobs.get() < 1) {
//					Peer peer = peerSelector.Next();
//					goFunc(public() {
//						gossipJobs.increment();
//						gossip(peer.NetAddr, returnCh);
//						gossipJobs.decrement();
//					});
//					logger.debug("Gossip");
//				}
//				logStats();
//				resetTimer();
//			case <-returnCh:
//				return;
//			case <-shutdownCh:
//				return;
//			}

			final Alternative alt = new Alternative (new Guard[] {netCh.in(), controlTimer.tickCh.in(), returnCh.in(), shutdownCh.in()});
			final int NET = 0, TIME = 1, RETURN = 2, SHUTDOWN = 3;

			switch (alt.priSelect ()) {
				// fall through
				case NET:
					RPC rpc = netCh.in().read();
					goFunc(() -> {
						rpcJobs.incrementAndGet();
						logger.debug("Processing RPC");
						processRPC(rpc);
						resetTimer();
						rpcJobs.incrementAndGet();
					});
				case TIME:
					controlTimer.tickCh.in().read();
					if (gossip && gossipJobs.get() < 1) {
						Peer peer = peerSelector.Next();
						goFunc(() -> {
							gossipJobs.incrementAndGet();
							gossip(peer.GetNetAddr(), returnCh);
							gossipJobs.incrementAndGet();
						});
						logger.debug("Gossip");
					}
					logStats();
					resetTimer();
				case RETURN:
					Block block = commitCh.in().read();
					error err = commit(block);
					if  (err != null) {
//						logger.WithField("error", err).Error("Adding EventBlock")
					}
				case SHUTDOWN:
					shutdownCh.in().read();
					return;
			}
		}
	}

	public void processRPC(net.RPC rpc) {
		Object cmd = rpc.getCommand();

		Class<? extends Object> cmdClass = cmd.getClass();

		if (net.SyncRequest.class.isAssignableFrom(cmdClass)) {
			processSyncRequest(rpc, (net.SyncRequest) cmd);
		} else if (net.EagerSyncRequest.class.isAssignableFrom(cmdClass)) {
			processEagerSyncRequest(rpc, (EagerSyncRequest) cmd);
		} else if (net.FastForwardRequest.class.isAssignableFrom(cmdClass)) {
			processFastForwardRequest(rpc, (FastForwardRequest) cmd);
		} else {
//			logger.WithField("cmd", rpc.Command).Error("Unexpected RPC command")
			rpc.Respond(null, error.Errorf("unexpected command"));
		}
	}

	public void processSyncRequest(net.RPC rpc, net.SyncRequest cmd) {
//		logger.WithFields(logrus.Fields{
//			"from_id": cmd.FromID,
//			"known":   cmd.Known,
//		}).Debug("processSyncRequest(rpc net.RPC, cmd *net.SyncRequest)")

		SyncResponse resp = new net.SyncResponse (id);
		error respErr = null;

		// Check sync limit
		coreLock.lock();
		boolean overSyncLimit = core.OverSyncLimit(cmd.getKnown(), conf.SyncLimit);
		coreLock.unlock();
		if (overSyncLimit) {
//			logger.Debug("core.OverSyncLimit(cmd.Known, conf.SyncLimit)")
			resp.setSyncLimit(true);
		} else {
			// Compute Diff
			long start = System.nanoTime();
			coreLock.lock();
			RetResult<Event[]> eventDiffCall = core.EventDiff(cmd.getKnown());
			Event[] eventDiff = eventDiffCall.result;
			error err = eventDiffCall.err;
			coreLock.unlock();
			long elapsed = System.nanoTime() - start;
//			logger.WithField("Duration", elapsed.Nanoseconds()).Debug("core.EventBlockDiff(cmd.Known)")
			if (err != null) {
//				logger.WithField("Error", err).Error("core.EventBlockDiff(cmd.Known)")
				respErr = err;
			}

			// Convert to WireEvents
			RetResult<WireEvent[]> toWireCall = core.ToWire(eventDiff);
			WireEvent[] wireEvents = toWireCall.result;
			err = toWireCall.err;
			if (err != null) {
//				logger.WithField("error", err).Debug("core.TransportEventBlock(eventDiff)")
				respErr = err;
			} else {
				resp.setEvents(wireEvents);
			}
		}

		// Get Self Known
		coreLock.lock();
		Map<Long, Long> knownEvents = core.KnownEvents();
		coreLock.unlock();
		resp.setKnown(knownEvents);

//		logger.WithFields(logrus.Fields{
//			"events":     len(resp.Events),
//			"known":      resp.Known,
//			"sync_limit": resp.SyncLimit,
//			"error":      respErr,
//		}).Debug("SyncRequest Received")

		rpc.Respond(resp, respErr);
	}

	public void processEagerSyncRequest(net.RPC rpc, net.EagerSyncRequest cmd) {
//		logger.WithFields(logrus.Fields{
//			"from_id": cmd.FromID,
//			"events":  len(cmd.Events),
//		}).Debug("processEagerSyncRequest(rpc net.RPC, cmd *net.EagerSyncRequest)")

		boolean success = true;
		coreLock.lock();
		error err = sync(cmd.getEvents());
		coreLock.unlock();
		if (err != null) {
//			logger.WithField("error", err).Error("sync(cmd.Events)")
			success = false;
		}

		EagerSyncResponse resp = new net.EagerSyncResponse (id, success);
		rpc.Respond(resp, err);
	}

	public void processFastForwardRequest(net.RPC rpc, net.FastForwardRequest cmd) {
//		logger.WithFields(logrus.Fields{
//			"from": cmd.FromID,
//		}).Debug("processFastForwardRequest(rpc net.RPC, cmd *net.FastForwardRequest)")

		FastForwardResponse resp = new net.FastForwardResponse(id);
		error respErr = null;

		// Get latest Frame
		coreLock.lock();
		RetResult3<Block, Frame> getAnchorBlockWithFrame = core.GetAnchorBlockWithFrame();
		Block block = getAnchorBlockWithFrame.result1;
		Frame frame = getAnchorBlockWithFrame.result2;
		error err = getAnchorBlockWithFrame.err;
		coreLock.unlock();
		if (err != null) {
//			logger.WithField("error", err).Error("core.GetAnchorBlockWithFrame()")
			respErr = err;
		} else {
			resp.setBlock(block);
			resp.setFrame(frame);

			// Get snapshot
			RetResult<byte[]> getSnapshot = proxy.GetSnapshot(block.Index());
			byte[] snapshot = getSnapshot.result;
			err = getSnapshot.err;
			if (err != null) {
//				logger.WithField("error", err).Error("proxy.GetSnapshot(block.Index())")
				respErr = err;
			}
			resp.setSnapshot(snapshot);
		}

//		logger.WithFields(logrus.Fields{
//			"Events": len(resp.Frame.Events),
//			"Error":  respErr,
//		}).Debug("FastForwardRequest Received")
		rpc.Respond(resp, respErr);
	}

	// This function is usually called in a go-routine and needs to inform the
	// calling routine (usually the lachesis routine) when it is time to exit the
	// Gossiping state and retur
	public error gossip(String peerAddr, One2OneChannelInt parentReturnCh /* chan struct{} */)  {

		// pull
		RetResult3<Boolean, Map<Long, Long>> pullCall = pull(peerAddr);
		boolean syncLimit = pullCall.result1;
		Map<Long, Long> otherKnownEvents = pullCall.result2;
		error err = pullCall.err;
		if (err != null) {
			return err;
		}

		// check and handle syncLimit
		if (syncLimit) {
//			logger.WithField("from", peerAddr).Debug("SyncLimit")
			setState(NodeStates.CatchingUp);
			parentReturnCh.out().write(1); // <- struct{}{};
			return null;
		}

		// push
		err = push(peerAddr, otherKnownEvents);
		if (err != null) {
			return err;
		}

		// update peer selector
		selectorLock.lock();
		peerSelector.UpdateLast(peerAddr);
		selectorLock.unlock();

		return null;
	}

	public RetResult3<Boolean,Map<Long,Long>> pull(String peerAddr) {
	/* (boolean syncLimit, Map<Long,Long> otherKnownEvents, error err) { */
		// Compute Known
		coreLock.lock();
		Map<Long,Long> knownEvents = core.KnownEvents();
		coreLock.unlock();

		// Send SyncRequest
		long start = System.nanoTime();
		RetResult<net.SyncResponse> requestSyncCall = requestSync(peerAddr, knownEvents);
		net.SyncResponse resp = requestSyncCall.result;
		error err = requestSyncCall.err;
		long elapsed = System.nanoTime() - start;
//		logger.WithField("Duration", elapsed.Nanoseconds()).Debug("requestSync(peerAddr, knownEvents)")
		// FIXIT: should we catch io.EOF error here and how we process it?
		//	if err == io.EOF {
		//		return false, null, null
		//	}
		if (err != null) {
//			logger.WithField("Error", err).Error("requestSync(peerAddr, knownEvents)")
			return new RetResult3<Boolean,Map<Long,Long>>(false, null, err);
		}
//		logger.WithFields(logrus.Fields{
//			"from_id":     resp.FromID,
//			"sync_limit":  resp.SyncLimit,
//			"events":      len(resp.Events),
//			"known":       resp.Known,
//			"knownEvents": knownEvents,
//		}).Debug("SyncResponse");

		if (resp.isSyncLimit()) {
			return new RetResult3<Boolean,Map<Long,Long>>(true, null, null);
		}

		// Add Events to poset and create new Head if necessary
		coreLock.lock();
		err = sync(resp.getEvents());
		coreLock.unlock();
		if (err != null) {
//			logger.WithField("error", err).Error("sync(resp.Events)")
			return new RetResult3<Boolean,Map<Long,Long>>(false, null, err);
		}

		return new RetResult3<Boolean,Map<Long,Long>>(false, resp.getKnown(), null);
	}

	public error push(String peerAddr, Map<Long,Long> knownEvents)  {
		// Check SyncLimit
		coreLock.lock();
		boolean overSyncLimit = core.OverSyncLimit(knownEvents, conf.SyncLimit);
		coreLock.unlock();
		if (overSyncLimit) {
//			logger.Debug("core.OverSyncLimit(knownEvents, conf.SyncLimit)")
			return null;
		}

		// Compute Diff
		long start = System.nanoTime();
		coreLock.lock();
		RetResult<Event[]> eventDiffCall = core.EventDiff(knownEvents);
		Event[] eventDiff = eventDiffCall.result;
		error err = eventDiffCall.err;
		coreLock.unlock();
		long elapsed = System.nanoTime() - start;
//		logger.WithField("Duration", elapsed.Nanoseconds()).Debug("core.EventDiff(knownEvents)")
		if (err != null) {
//			logger.WithField("Error", err).Error("core.EventDiff(knownEvents)")
			return err;
		}

		if (eventDiff.length > 0) {
			// Convert to WireEvents
			RetResult<WireEvent[]> toWire = core.ToWire(eventDiff);
			WireEvent[] wireEvents = toWire.result;
			err = toWire.err;
			if (err != null) {
//				logger.WithField("Error", err).Debug("core.TransferEventBlock(eventDiff)")
				return err;
			}

			// Create and Send EagerSyncRequest
			start = System.nanoTime();
//			logger.WithField("wireEvents", wireEvents).Debug("Sending requestEagerSync.wireEvents")

			RetResult<EagerSyncResponse> requestEagerSync = requestEagerSync(peerAddr, wireEvents);
			EagerSyncResponse resp2 = requestEagerSync.result;
			err = requestEagerSync.err;
			elapsed = System.nanoTime() - start;
//			logger.WithField("Duration", elapsed.Nanoseconds()).Debug("requestEagerSync(peerAddr, wireEvents)")
			if (err != null) {
//				logger.WithField("Error", err).Error("requestEagerSync(peerAddr, wireEvents)")
				return err;
			}
//			logger.WithFields(logrus.Fields{
//				"from_id": resp2.FromID,
//				"success": resp2.Success,
//			}).Debug("EagerSyncResponse");
		}

		return null;
	}

	public error fastForward() {
//		logger.Debug("fastForward()");

		// wait until sync routines finish
		waitRoutines();

		// fastForwardRequest
		Peer peer = peerSelector.Next();
		long start = System.nanoTime();
		RetResult<net.FastForwardResponse> requestFastForwardCall = requestFastForward(peer.GetNetAddr());
		FastForwardResponse resp = requestFastForwardCall.result;
		error err = requestFastForwardCall.err;
		long elapsed = System.nanoTime() - start;
//		logger.WithField("Duration", elapsed.Nanoseconds()).Debug("requestFastForward(peer.NetAddr)")
		if (err != null) {
//			logger.WithField("Error", err).Error("requestFastForward(peer.NetAddr)")
			return err;
		}
//		logger.WithFields(logrus.Fields{
//			"from_id":              resp.FromID,
//			"block_index":          resp.Block.Index(),
//			"block_round_received": resp.Block.RoundReceived(),
//			"frame_events":         len(resp.Frame.Events),
//			"frame_roots":          resp.Frame.Roots,
//			"snapshot":             resp.Snapshot,
//		}).Debug("FastForwardResponse")

		// prepare core. ie: fresh poset
		coreLock.lock();
		err = core.FastForward(peer.GetPubKeyHex(), resp.getBlock(), resp.getFrame());
		coreLock.unlock();
		if (err != null) {
//			logger.WithField("Error", err).Error("core.FastForward(peer.PubKeyHex, resp.Block, resp.Frame)")
			return err;
		}

		// update app from snapshot
		err = proxy.Restore(resp.getSnapshot());
		if (err != null) {
//			logger.WithField("Error", err).Error("proxy.Restore(resp.Snapshot)")
			return err;
		}

		setState(NodeStates.Gossiping);

		return null;
	}

	public RetResult<net.SyncResponse> requestSync(String target, Map<Long,Long> known) {

		SyncRequest args = new SyncRequest(id, known);

		net.SyncResponse out = new net.SyncResponse();
		error err = trans.Sync(target, args, out);
		//logger.WithField("out", out).Debug("requestSync(target string, known map[int]int)")
		return new RetResult<net.SyncResponse>(out, err);
	}

	public RetResult<net.EagerSyncResponse> requestEagerSync(String target, poset.WireEvent[] events ) {
		EagerSyncRequest args = new net.EagerSyncRequest (id, events);

		net.EagerSyncResponse out = new net.EagerSyncResponse();
//		logger.WithFields(logrus.Fields{
//			"target": target,
//		}).Debug("requestEagerSync(target string, events []poset.WireEvent)")
		error err = trans.EagerSync(target, args, out);

		return new RetResult<net.EagerSyncResponse>(out, err);
	}

	public RetResult<net.FastForwardResponse> requestFastForward(String target) {
//		logger.WithFields(logrus.Fields{
//			"target": target,
//		}).Debug("requestFastForward(target string) (net.FastForwardResponse, error)")

		FastForwardRequest args = new net.FastForwardRequest(id);

		net.FastForwardResponse out = new net.FastForwardResponse();
		error err = trans.FastForward(target, args, out);

		return new RetResult<net.FastForwardResponse>(out, err);
	}

	public error sync(poset.WireEvent[] events ) {
		// Insert Events in Poset and create new Head if necessary
		long start = System.nanoTime();
		error err = core.Sync(events);
		long elapsed = System.nanoTime() - start;
//		logger.WithField("Duration", elapsed.Nanoseconds()).Debug("core.Sync(events)");
		if (err != null) {
			return err;
		}

		// Run consensus methods
		start = System.nanoTime();
		err = core.RunConsensus();
		elapsed = System.nanoTime() - start;
//		logger.WithField("Duration", elapsed.Nanoseconds()).Debug("core.RunConsensus()")
		if (err != null) {
			return err;
		}

		return null;
	}

	public error commit(poset.Block block ) {
		byte[] stateHash = new byte[]{0, 1, 2};
		error err = proxy.CommitBlock(block).err;
		if (err != null) {
//			logger.WithError(err).Debug("commit(block poset.Block)");
		}

//		logger.WithFields(logrus.Fields{
//			"block":      block.Index(),
//			"state_hash": fmt.Sprintf("%X", stateHash),
//			// "err":        err,
//		}).Debug("commit(eventBlock poset.EventBlock)")

		// XXX what do we do in case of error. Retry? This has to do with the
		// Lachesis <-> App interface. Think about it.

		// An error here could be that the endpoint is not configured, not all
		// nodes will be sending blocks to clients, in these cases -no_client can be
		// used, alternatively should check for the error here and handle it
		// appropriately

		// There is no point in using the stateHash if we know it is wrong
		// if (err == null) {
		if (true) {
			// inmem statehash would be different than proxy statehash
			// inmem is simply the hash of transactions
			// this requires a 1:1 relationship with nodes and clients
			// multiple nodes can't read from the same client

			block.setStateHash(stateHash);
			coreLock.lock();
			try {
				RetResult<BlockSignature> signBlockCall = core.SignBlock(block);
				BlockSignature sig = signBlockCall.result;
				err = signBlockCall.err;
				if (err != null) {
					return err;
				}
				core.AddBlockSignature(sig);
			} finally {
				coreLock.unlock();
			}
		}

		return null;
	}

	public void addTransaction(byte[] tx) {
		coreLock.lock();
		try {
			core.AddTransactions(new byte[][]{tx});
		} finally {
			coreLock.unlock();
		}
	}

	public void addInternalTransaction(poset.InternalTransaction tx) {
		coreLock.lock();
		try {
			core.AddInternalTransactions(new poset.InternalTransaction[]{tx});
		} finally {
			coreLock.unlock();
		}
	}

	public void Shutdown() {
		if (getState() != NodeStates.Shutdown) {
			// mqtt.FireEvent("Shutdown()", "/mq/lachesis/node")
//			logger.Debug("Shutdown()")

			// Exit any non-shutdown state immediately
			setState(NodeStates.Shutdown);

			// Stop and wait for concurrent operations
			ChannelUtils.close(shutdownCh);
			waitRoutines();

			// For some reason this needs to be called after closing the shutdownCh
			// Not entirely sure why...
			controlTimer.Shutdown();

			// transport and store should only be closed once all concurrent operations
			// are finished otherwise they will panic trying to use close objects
			trans.Close();
			core.poset.Store.Close();
		}
	}

	public Map<String,String> GetStats() {
//		toString := public (long i) string {
//			if i == null {
//				return "null"
//			}
//			return strconv.FormatInt(*i, 10)
//		}

		long timeElapsed = System.nanoTime() - start;
		double timeElapsedSeconds = timeElapsed/time.Second;

		long consensusEvents = core.GetConsensusEventsCount();
		double consensusEventsPerSecond = consensusEvents / timeElapsedSeconds;
		long consensusTransactions = core.GetConsensusTransactionsCount();
		double transactionsPerSecond = consensusTransactions / timeElapsedSeconds;

		long lastConsensusRound = core.GetLastConsensusRoundIndex();
		double consensusRoundsPerSecond = 0;
		if (lastConsensusRound >= 0) {
			consensusRoundsPerSecond = lastConsensusRound / timeElapsedSeconds;
		}

		Map<String,String> s = new HashMap<String,String>();
		s.put("last_consensus_round",    ""+lastConsensusRound);
		s.put("time_elapsed",            String.format("%.2f", timeElapsedSeconds));
		s.put("heartbeat",               String.format("%.2f", conf.HeartbeatTimeout.getSeconds()));
		s.put("node_current",            "" + System.currentTimeMillis() / 1000);
		s.put("node_start",              "" + start);
		s.put("last_block_index",        "" + core.GetLastBlockIndex());
		s.put("consensus_events",        "" + consensusEvents);
		s.put("sync_limit",              "" + conf.SyncLimit);
		s.put("consensus_transactions",  "" + consensusTransactions);
		s.put("undetermined_events",     "" + core.GetUndeterminedEvents().size());
		s.put("transaction_pool",        "" + core.transactionPool.length);
		s.put("num_peers",               "" + peerSelector.Peers().Len());
		s.put("sync_rate",               "" + String.format("%.2f",SyncRate()));
		s.put("transactions_per_second", String.format("%.2f",transactionsPerSecond));
		s.put("events_per_second",       String.format("%.2f",consensusEventsPerSecond));
		s.put("rounds_per_second",       String.format("%.2f", consensusRoundsPerSecond));
		s.put("round_events",            "" + core.GetLastCommittedRoundEventsCount());
		s.put("id",                      "" + id);
		s.put("state",                   getState().toString());
		// mqtt.FireEvent(s, "/mq/lachesis/stats")
		return s;
	}

	public void logStats() {
		Map<String,String> stats = GetStats();
//		logger.WithFields(logrus.Fields{
//			"last_consensus_round":   stats["last_consensus_round"],
//			"last_block_index":       stats["last_block_index"],
//			"consensus_events":       stats["consensus_events"],
//			"consensus_transactions": stats["consensus_transactions"],
//			"undetermined_events":    stats["undetermined_events"],
//			"transaction_pool":       stats["transaction_pool"],
//			"num_peers":              stats["num_peers"],
//			"sync_rate":              stats["sync_rate"],
//			"events/s":               stats["events_per_second"],
//			"t/s":                    stats["transactions_per_second"],
//			"rounds/s":               stats["rounds_per_second"],
//			"round_events":           stats["round_events"],
//			"id":                     stats["id"],
//			"state":                  stats["state"],
//			"z_gossipJobs":           gossipJobs.get(),
//			"z_rpcJobs":              rpcJobs.get(),
//			"addr":                   localAddr,
//		}).Warn("logStats()")
	}


	/*
	 * Diff tool interface implementation (tmp)
	 */
	public long GetLastBlockIndex() {
		return core.poset.Store.LastBlockIndex();
	}

	public String[] RoundWitnesses(long i) {
		return core.poset.Store.RoundWitnesses(i);
	}

	public RetResult<poset.Frame> GetFrame(long i) {
		return core.poset.Store.GetFrame(i);
	}

	/*
	 * Node's method candidates
	 */

	public void PushTx(byte[] tx) {
		coreLock.lock();
		try {
			core.AddTransactions(new byte[][]{tx});
			logger.debug( String.format( "PushTx('%s')", tx ));
		} finally {
			coreLock.unlock();
		}
	}

	public void Register() {
	}


	public double SyncRate() {
		double syncErrorRate = 0;
		if (syncRequests != 0) {
			syncErrorRate = syncErrors / ((double) syncRequests);
		}
		return 1 - syncErrorRate;
	}

	public RetResult<peers.Peers> GetParticipants() {
		return core.poset.Store.Participants();
	}

	public RetResult<poset.Event> GetEvent(String event) {
		return core.poset.Store.GetEvent(event);
	}

	public RetResult3<String,Boolean> GetLastEventFrom(String participant) {
		return core.poset.Store.LastEventFrom(participant);
	}

	public Map<Long,Long> GetKnownEvents() {
		return core.poset.Store.KnownEvents();
	}

	public RetResult<Map<Long,Long>> GetEvents()  {
		Map<Long, Long> res = core.KnownEvents();
		return new RetResult(res, null);
	}

	public String[] GetConsensusEvents() {
		return core.poset.Store.ConsensusEvents();
	}

	public long GetConsensusTransactionsCount() {
		return core.GetConsensusTransactionsCount();
	}

	public RetResult<poset.RoundInfo> GetRound(long roundIndex) {
		return core.poset.Store.GetRound(roundIndex);
	}

	public long GetLastRound() {
		return core.poset.Store.LastRound();
	}

	public String[] GetRoundWitnesses(long roundIndex) {
		return core.poset.Store.RoundWitnesses(roundIndex);
	}

	public int GetRoundEvents(long roundIndex) {
		return core.poset.Store.RoundEvents(roundIndex);
	}

	public RetResult<poset.Root> GetRoot(long rootIndex) {
		return core.poset.Store.GetRoot("" + rootIndex);
	}

	public RetResult<poset.Block> GetBlock(long blockIndex) {
		return core.poset.Store.GetBlock(blockIndex);
	}

	public long ID() {
		return id;
	}
}
