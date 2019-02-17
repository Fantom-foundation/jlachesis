package node;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.jcsp.lang.One2OneChannel;

import autils.Appender;
import autils.Logger;
import autils.time;
import common.RetResult;
import common.RetResult3;
import common.error;
import peers.Peer;
import poset.BlockSignature;
import poset.Event;
import poset.EventComparatorByTopologicalOrder;
import poset.Poset;
import poset.Root;
import poset.Utils;

public class Core {
	long id;
	KeyPair key;
	byte[] pubKey;
	String hexID;
	poset.Poset poset;

	Map<String,Long> inDegrees;

	peers.Peers participants; // [PubKey] => id
	String head;
	long Seq;

	byte[][] transactionPool;
	poset.InternalTransaction[] internalTransactionPool;
	poset.BlockSignature[] blockSignaturePool;

	Logger logger;

	int maxTransactionsInEvent;

	public Core(long id, KeyPair key, peers.Peers participants,
			poset.Store store, One2OneChannel<poset.Block>commitCh /**chan **/ , Logger logger) {

		if (logger == null) {
			logger = Logger.getLogger(Core.class);
			logger.setLevel(Level.DEBUG);
//			lachesis_log.NewLocal(logger, logger.Level.String());
		}
		logger = logger.field("id", id);

		Map<String,Long> inDegrees = new HashMap<String,Long>();
		for (String pubKey : participants.getByPubKey().keySet()) {
			inDegrees.put(pubKey, (long) 0);
		}

		Poset p2 = new poset.Poset(participants, store, commitCh, logger);
		this.id = id;
		this.key = key;
		this.poset= p2;
		this.inDegrees=               inDegrees;
		this.participants=            participants;
		this.transactionPool=         new byte[][] {};
		this.internalTransactionPool= new poset.InternalTransaction[]{};
		this.blockSignaturePool=      new poset.BlockSignature[] {};
		this.logger=                  logger;
		this.head=  "";
		this.Seq=   -1;
			// MaxReceiveMessageSize limitation in grpc: https://github.com/grpc/grpc-go/blob/master/clientconn.go#L96
			// default value is 4 * 1024 * 1024 bytes
			// we use transactions of 120 bytes in tester, thus rounding it down to 16384
		this.maxTransactionsInEvent= 16384;

		p2.SetCore(this);
	}

	public long ID() {
		return id;
	}

	public byte[] PubKey() {
		if (pubKey == null) {
			pubKey = crypto.Utils.FromECDSAPub(key.getPublic());
		}
		return pubKey;
	}

	public String HexID() {
		if (hexID == null || hexID.isEmpty()) {
			pubKey = PubKey();
//			hexID = String.format("0x%X", pubKey);
			hexID = crypto.Utils.toHexString(pubKey);
		}
		return hexID;
	}

	public String Head() {
		return head;
	}

	// Heights returns map with heights for each participants
	public Map<String,Long> Heights() {
		HashMap<String, Long> heights = new HashMap<String,Long>();
		for (String pubKey : participants.getByPubKey().keySet()) {
			RetResult<String[]> participantEventsCre = poset.Store.ParticipantEvents(pubKey, -1);
			String[] participantEvents = participantEventsCre.result;
			error err = participantEventsCre.err;

			if (err == null) {
				heights.put(pubKey, (long) participantEvents.length);
			} else {
				heights.put(pubKey, (long) 0);
			}
		}
		return heights;
	}

	public Map<String,Long> InDegrees() {
		return inDegrees;
	}

	public error SetHeadAndSeq() {
		String head;
		long seq;

		RetResult3<String, Boolean> lastEventFrom = poset.Store.LastEventFrom(HexID());
		String last = lastEventFrom.result1;
		Boolean isRoot = lastEventFrom.result2;
		error err = lastEventFrom.err;
		if (err != null) {
			return err;
		}

		if (isRoot) {
			RetResult<Root> getRoot = poset.Store.GetRoot(HexID());
			Root root = getRoot.result;
			err = getRoot.err;
			if (err != null) {
				return err;
			}
			head = root.GetSelfParent().GetHash();
			seq = root.GetSelfParent().GetIndex();
		} else {
			RetResult<Event> getEvent = GetEvent(last);
			Event lastEvent = getEvent.result;
			err = getEvent.err;
			if (err != null) {
				return err;
			}
			head = last;
			seq = lastEvent.Index();
		}

		this.head = head;
		this.Seq = seq;

		logger.field("core.head", head).field("core.Seq", Seq)
		.field("is_root", isRoot).debugf("SetHeadAndSeq()");

		return null;
	}

	public error Bootstrap() {
		error err = poset.Bootstrap();
		if  (err != null) {
			return err;
		}
		bootstrapInDegrees();
		return null;
	}

	public void bootstrapInDegrees() {
		for (String pubKey : participants.getByPubKey().keySet()) {
			inDegrees.put(pubKey, (long) 0);
			RetResult3<String, Boolean> lastEventFrom = poset.Store.LastEventFrom(pubKey);
			String eventHash = lastEventFrom.result1;
			error err = lastEventFrom.err;
			if (err != null) {
				continue;
			}
			for (String otherPubKey : participants.getByPubKey().keySet()) {
				if (otherPubKey.equals(pubKey)) {
					continue;
				}
				RetResult<String[]> participantEventsCr = poset.Store.ParticipantEvents(otherPubKey, -1);
				String[] events = participantEventsCr.result;
				err = participantEventsCr.err;
				if (err != null) {
					continue;
				}
				for (String eh : events) {
					RetResult<Event> getEvent = poset.Store.GetEvent(eh);
					Event event = getEvent.result;
					err = getEvent.err;
					if (err != null) {
						continue;
					}
					if (event.OtherParent().equals(eventHash)) {
						inDegrees.put(pubKey, inDegrees.get(pubKey)+1);
					}
				}
			}
		}
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	public error SignAndInsertSelfEvent( poset.Event event) {
		error err = poset.SetWireInfoAndSign(event, key.getPrivate());
		if (err != null){
			return err;
		}

		return InsertEvent(event, true);
	}

	public error InsertEvent(poset.Event event, boolean setWireInfo ) {

		logger.field("event", event).field("creator", event.Creator())
		.field("selfParent", event.SelfParent()).field("index", event.Index())
		.field("hex", event.Hex()).debugf("InsertEvent(event poset.Event, setWireInfo bool)");

		error err = poset.InsertEvent(event, setWireInfo);
		if (err != null) {
			return err;
		}

		if (event.Creator().equals(HexID())) {
			head = event.Hex();
			Seq = event.Index();
		}

		inDegrees.put(event.Creator(), (long) 0);
		RetResult<Event> getEvent = poset.Store.GetEvent(event.OtherParent());
		Event otherEvent = getEvent.result;
		err = getEvent.err;
		if  (err == null) {
			inDegrees.put(otherEvent.Creator(),
					inDegrees.get(otherEvent.Creator()) + 1);
		}
		return null;
	}

	public Map<Long,Long> KnownEvents() {
		return poset.Store.KnownEvents();
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	public RetResult<poset.BlockSignature> SignBlock(poset.Block block) {
		RetResult<BlockSignature> signCall = block.Sign(key);
		BlockSignature sig = signCall.result;
		error err = signCall.err;
		if (err != null) {
			return new RetResult<poset.BlockSignature>(new poset.BlockSignature(), err);
		}
		err = block.SetSignature(sig);
		if  (err != null) {
			return new RetResult<poset.BlockSignature>(new poset.BlockSignature(), err);
		}
		return new RetResult<poset.BlockSignature>(sig, poset.Store.SetBlock(block));
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	public boolean OverSyncLimit(Map<Long,Long> knownEvents, long syncLimit) {
		int totUnknown = 0;
		Map<Long, Long> myKnownEvents = KnownEvents();
		for (long i : myKnownEvents.keySet()) {
			long li = myKnownEvents.get(i);
			if (li > knownEvents.get(i)) {
				totUnknown += li - knownEvents.get(i);
			}
		}
		if (totUnknown > syncLimit) {
			return true;
		}
		return false;
	}

	public RetResult3<poset.Block, poset.Frame> GetAnchorBlockWithFrame() {
		return poset.GetAnchorBlockWithFrame();
	}

	// returns events that c knows about and are not in 'known'
	public RetResult<poset.Event[]> EventDiff(Map<Long,Long> known) {
		poset.Event[] unknown = new poset.Event[0];
		// known represents the index of the last event known for every participant
		// compare this to our view of events and fill unknown with events that we know of
		// and the other doesn't
		for (long id: known.keySet()) {
			long ct = known.get(id);
			Peer peer = participants.ById(id);
			if (peer == null) {
				// unknown peer detected.
				// TODO: we should handle this nicely
				continue;
			}
			// get participant Events with index > ct
			RetResult<String[]> ParticipantEventsCall = poset.Store.ParticipantEvents(peer.GetPubKeyHex(), ct);
			String[] participantEvents = ParticipantEventsCall.result;
			error err = ParticipantEventsCall.err;
			if (err != null) {
				return new RetResult<poset.Event[]> ( new poset.Event[] {}, err);
			}
			for (String e : participantEvents) {
				RetResult<Event> getEvent = poset.Store.GetEvent(e);
				Event ev = getEvent.result;
				err = getEvent.err;
				if (err != null) {
					return new RetResult<poset.Event[]>(new poset.Event[] {}, err);
				}
				logger.field("event", ev).field("creator", ev.Creator())
				.field("selfParent", ev.SelfParent())
				.field("index", ev.Index()).field("hex", ev.Hex())
				.debugf("Sending Unknown Event");
				unknown = Appender.append(unknown,  ev);
			}
		}

//		sort.Stable(poset.ByTopologicalOrder(unknown));
		Arrays.sort(unknown, new EventComparatorByTopologicalOrder());


		return new RetResult<poset.Event[]>(unknown, null);
	}

	public error Sync(poset.WireEvent[] unknownEvents)  {

		logger.field("unknown_events", unknownEvents)
		.field("transaction_pool", transactionPool.length)
		.field("internal_transaction_pool", internalTransactionPool.length)
		.field("block_signature_pool", blockSignaturePool.length)
		.field("poset.PendingLoadedEvents", poset.getPendingLoadedEvents())
		.debug("Sync(unknownEventBlocks []poset.EventBlock)");

		Map<Long, Long> myKnownEvents = KnownEvents();
		String otherHead = "";
		// add unknown events
		for (int k = 0; k < unknownEvents.length; ++k) {
			poset.WireEvent we = unknownEvents[k];
			logger.field("we", we).error("Sync");

			RetResult<Event> readWireInfo = poset.ReadWireInfo(we);
			Event ev = readWireInfo.result;
			logger.field("ev", ev).error("Sync");
			error err = readWireInfo.err;
			if (err != null) {
				return err;
			}

//			if (!myKnownEvents.containsKey(ev.CreatorID())) {
//				logger.field("ev.CreatorID()", ev.CreatorID()).error("Sync");
//				return error.Errorf("ev.CreatorID() not known");
//			}
			if (ev.Index() > myKnownEvents.get(ev.CreatorID())) {
				err = InsertEvent(ev, false);
				if (err != null) {
					return err;
				}
			}

			// assume last event corresponds to other-head
			if (k == unknownEvents.length-1) {
				otherHead = ev.Hex();
			}
		}

		// create new event with self head and other head only if there are pending
		// loaded events or the pools are not empty
		if (poset.getPendingLoadedEvents() > 0 ||
			transactionPool.length > 0 ||
			internalTransactionPool.length > 0 ||
			blockSignaturePool.length > 0) {
			return AddSelfEventBlock(otherHead);
		}
		return null;
	}

	public error FastForward(String peer, poset.Block block, poset.Frame frame) {

		logger.field("peer", peer).debug("FastForward()");

		// Check Block Signatures
		error err = poset.CheckBlock(block);
		if (err != null) {
			return err;
		}

		// Check Frame Hash
		RetResult<byte[]> hashCall = frame.Hash();
		byte[] frameHash = hashCall.result;
		err = hashCall.err;

		logger.field("err1", err).debug("FastForward()");

		if (err != null) {
			return err;
		}

		if (!Utils.bytesEquals(block.GetFrameHash(), frameHash)) {

			logger.field("err2", err).debug("FastForward()");

			return error.Errorf("invalid Frame Hash");
		}

		logger.debug("FastForward() here");

		err = poset.Reset(block, frame);
		if (err != null) {
			return err;
		}

		err = SetHeadAndSeq();
		if (err != null) {
			return err;
		}

		err = RunConsensus();
		if (err != null) {
			return err;
		}

		return null;
	}

	// TBD: remove. Use Math.min
//	public int min(int a, int b) {
//		if (a < b) {
//			return a;
//		}
//		return b;
//	}

	public error AddSelfEventBlock(String otherHead) {
		RetResult<Event> getEvent = poset.Store.GetEvent(head);
		// Get flag tables from parents
		Event parentEvent = getEvent.result;
		error errSelf = getEvent.err;
		if (errSelf != null) {
			logger.warn(String.format("failed to get parent: %s", errSelf));
		}
		RetResult<Event> getEventOtherCall = poset.Store.GetEvent(otherHead);
		Event otherParentEvent = getEventOtherCall.result;
		error errOther = getEventOtherCall.err;
		if (errOther != null) {
			logger.warn(String.format("failed to get other parent: %s", errOther));
		}

		Map<String,Long> flagTable;
		error err;

		if (errSelf != null) {
			flagTable = new HashMap<String,Long>();
			flagTable.put(head, (long) 1);
		} else {
			RetResult<Map<String, Long>> getFlagTable = parentEvent.GetFlagTable();
			flagTable = getFlagTable.result;
			err = getFlagTable.err;
			if (err != null) {
				return error.Errorf(String.format("failed to get self flag table: %s", err));
			}
		}

		if (errOther == null) {
			RetResult<Map<String, Long>> mergeFlagTableCall = otherParentEvent.MergeFlagTable(flagTable);
			flagTable = mergeFlagTableCall.result;
			err = mergeFlagTableCall.err;
			if (err != null) {
				return error.Errorf(String.format("failed to marge flag tables: %s", err));
			}
		}

		// create new event with self head and empty other parent
		// empty transaction pool in its payload
		byte[][] batch;
		int nTxs = Math.min(transactionPool.length, maxTransactionsInEvent);
		batch = Appender.slice(transactionPool, 0, nTxs); //transactionPool[0:nTxs:nTxs];
		Event newHead = new poset.Event(batch,
			internalTransactionPool,
			blockSignaturePool,
			new String[]{head, otherHead}, PubKey(), Seq+1, flagTable);

		err = SignAndInsertSelfEvent(newHead);
		if ( err != null) {
			return error.Errorf(String.format("newHead := poset.NewEventBlock: %s", err));
		}
		logger
			.field("transactions",          transactionPool.length)
			.field("internal_transactions", internalTransactionPool.length)
			.field("block_signatures",      blockSignaturePool.length)
			.debug("newHead := poset.NewEventBlock");

		transactionPool = Appender.slice(transactionPool, nTxs, transactionPool.length); //transactionPool[nTxs:]; //[][]byte{}
		internalTransactionPool = new poset.InternalTransaction[]{};
		// retain blockSignaturePool until transactionPool is empty
		// FIXIT: is there any better strategy?
		if (transactionPool.length == 0) {
			blockSignaturePool = new poset.BlockSignature[]{};
		}

		return null;
	}

	public RetResult<poset.Event[]> FromWire(poset.WireEvent[] wireEvents)  {
		poset.Event[] events = new poset.Event[wireEvents.length];
		for (int i = 0; i < wireEvents.length; ++i) {
			RetResult<Event> readWireInfo = poset.ReadWireInfo(wireEvents[i]);
			Event ev = readWireInfo.result;
			error err = readWireInfo.err;
			if (err != null) {
				return new RetResult<poset.Event[]>(null, err);
			}
			events[i] = new Event(ev);
		}
		return new RetResult<poset.Event[]>(events, null);
	}

	public RetResult<poset.WireEvent[]> ToWire(poset.Event[] events) {
		poset.WireEvent[] wireEvents = new poset.WireEvent[events.length];
		for (int i = 0; i < events.length; ++i) {
			wireEvents[i] = events[i].ToWire();
		}
		return new RetResult<poset.WireEvent[]>(wireEvents, null);
	}

	public error RunConsensus()  {

		long start = System.nanoTime();
		error err = poset.DivideRounds();
		logger.field("Duration", time.Since(start)).debug("poset.DivideAtropos()");
		if (err != null) {
			logger.field("Error", err).error("poset.DivideAtropos()");
			return err;
		}

		start = System.nanoTime();
		err = poset.DecideFame();
		logger.field("Duration", time.Since(start)).debug("poset.DecideClotho()");
		if (err != null) {
			logger.field("Error", err).error("poset.DecideClotho()");
			return err;
		}

		start = System.nanoTime();
		err = poset.DecideRoundReceived();
		logger.field("Duration", time.Since(start)).debug("poset.DecideAtroposRoundReceived()");
		if (err != null) {
			logger.field("Error", err).error("poset.DecideAtroposRoundReceived()");
			return err;
		}

		start = System.nanoTime();
		err = poset.ProcessDecidedRounds();
		logger.field("Duration", time.Since(start)).debug("poset.ProcessAtroposRounds()");
		if (err != null) {
			logger.field("Error", err).error("poset.ProcessAtroposRounds()");
			return err;
		}

		start = System.nanoTime();
		err = poset.ProcessSigPool();
		logger.field("Duration", time.Since(start)).debug("poset.ProcessSigPool()");
		if (err != null) {
			logger.field("Error", err).error("poset.ProcessSigPool()");
			return err;
		}

		logger.field("transaction_pool", transactionPool.length)
			.field("block_signature_pool", blockSignaturePool.length)
			.field("poset.PendingLoadedEvents", poset.getPendingLoadedEvents())
			.debug("RunConsensus()");

		return null;
	}

	public void AddTransactions(byte[][] txs) {
		transactionPool = Appender.append(transactionPool, txs);
	}

	public void AddInternalTransactions(poset.InternalTransaction[] txs) {
		internalTransactionPool = Appender.append(internalTransactionPool, txs);
	}

	public void AddBlockSignature(poset.BlockSignature bs) {
		blockSignaturePool = Appender.append(blockSignaturePool, bs);
	}

	public RetResult<poset.Event> GetHead() {
		return poset.Store.GetEvent(head);
	}

	public RetResult<poset.Event> GetEvent(String hash) {
		return poset.Store.GetEvent(hash);
	}

	public RetResult<byte[][]> GetEventTransactions(String hash){
		byte[][] txs = null;
		RetResult<Event> getEvent = GetEvent(hash);
		Event ex = getEvent.result;
		error err = getEvent.err;
		if (err != null) {
			return new RetResult<byte[][]>(txs, err);
		}
		txs = ex.Transactions();
		return new RetResult<byte[][]>(txs, null);
	}

	public String[] GetConsensusEvents() {
		return poset.Store.ConsensusEvents();
	}

	public long GetConsensusEventsCount() {
		return poset.Store.ConsensusEventsCount();
	}

	public List<String> GetUndeterminedEvents() {
		return poset.getUndeterminedEvents();
	}

	public int GetPendingLoadedEvents() {
		return poset.getPendingLoadedEvents();
	}

	public RetResult<byte[][]> GetConsensusTransactions() {
		byte[][] txs = null;
		for (String e : GetConsensusEvents()) {
			RetResult<byte[][]> getTrans = GetEventTransactions(e);
			byte[][] eTxs = getTrans.result;
			error err = getTrans.err;
			if (err != null) {
				return new RetResult<byte[][]>(txs, error.Errorf(
						String.format("GetConsensusTransactions(): %s", e)));
			}
			txs = Appender.append(txs, eTxs);
		}
		return new RetResult<byte[][]>(txs, null);
	}

	public long GetLastConsensusRoundIndex() {
		return poset.getLastConsensusRound();
	}

	public long GetConsensusTransactionsCount() {
		return poset.getConsensusTransactions();
	}

	public int GetLastCommittedRoundEventsCount() {
		return poset.getLastCommitedRoundEvents();
	}

	public long GetLastBlockIndex() {
		return poset.Store.LastBlockIndex();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Core [id=").append(id).append(", key=").append(key)
				.append(", hexID=").append(hexID).append(", poset=").append(poset.hashCode())
				.append(", inDegrees=").append(inDegrees).append(", participants=").append(participants)
				.append(", head=").append(head).append(", Seq=").append(Seq).append(", transactionPool=")
				.append(Arrays.toString(transactionPool)).append(", internalTransactionPool=")
				.append(Arrays.toString(internalTransactionPool)).append(", blockSignaturePool=")
				.append(Arrays.toString(blockSignaturePool)).append(", logger=").append(logger)
				.append(", maxTransactionsInEvent=").append(maxTransactionsInEvent).append("]");
		return builder.toString();
	}
}
