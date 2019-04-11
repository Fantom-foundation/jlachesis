package proxy;

import java.util.Arrays;

import org.apache.log4j.Level;
import org.jcsp.lang.Channel;
import org.jcsp.lang.One2OneChannel;

import autils.Logger;
import common.RResult;
import common.error;
import poset.InternalTransaction;

/**
 * InmemAppProxy implements the AppProxy interface natively
 */
public class InmemAppProxy implements AppProxy {
	Logger logger;
	ProxyHandler handler;
	One2OneChannel<byte[]> submitCh;
	One2OneChannel<poset.InternalTransaction> submitInternalCh;

	/**
	 * Constructor instantiates an InmemProxy without a handler.
	 * The handler should be set afterwards before use
	 *
	 * @param handler
	 * @param logger
	 */
	public InmemAppProxy(Logger logger) {
		super();

		if (logger == null) {
			logger = Logger.getLogger(InmemAppProxy.class);
			logger.setLevel(Level.DEBUG);
		}

		this.logger = logger;
		this.submitCh = Channel.one2one();// make(chan []byte);
		this.submitInternalCh = Channel.one2one(); // make(chan poset.InternalTransaction);
	}

	/**
	 * Constructor instantiates an InmemProxy from a set of handlers
	 *
	 * @param handler
	 * @param logger
	 */
	public InmemAppProxy(ProxyHandler handler, Logger logger) {
		this(logger);
		this.handler = handler;
	}

	/**
	 * Sets a proxy handler
	 * @param handler
	 */
	public void setHandler(ProxyHandler handler) {
		this.handler = handler;
	}

	/*
	 * inmem interface: AppProxy implementation
	 */
	// SubmitCh implements AppProxy interface method
	public One2OneChannel<byte[]> SubmitCh() {
		return submitCh;
	}

	public void ProposePeerAdd(peers.Peer peer) {
		submitInternalCh.out().write(new InternalTransaction(poset.TransactionType.PEER_ADD, peer));
	}

	public void ProposePeerRemove(peers.Peer peer) {
		submitInternalCh.out().write(new InternalTransaction(poset.TransactionType.PEER_REMOVE, peer));
	}

	/**
	 * SubmitCh returns the channel of raw transactions
	 */
	public One2OneChannel<poset.InternalTransaction> SubmitInternalCh() {
		return submitInternalCh;
	}

	// CommitBlock implements AppProxy interface method, calls handler
	public RResult<byte[]> CommitBlock(poset.Block block) {
		RResult<byte[]> commitHandler = handler.CommitHandler(block);
		byte[] stateHash = commitHandler.result;
		error err = commitHandler.err;

		logger.field("round_received", block.roundReceived())
			.field("txs",           block.transactions().length)
			.field("state_hash",     stateHash)
			.field("err",            err)
			.debug("InmemAppProxy.CommitBlock");
		return new RResult<byte[]>(stateHash, err);
	}

	// GetSnapshot implements AppProxy interface method, calls handler
	public RResult<byte[]> GetSnapshot(long blockIndex) {
		RResult<byte[]> snapshotHandler = handler.SnapshotHandler(blockIndex);
		byte[] snapshot = snapshotHandler.result;
		error err = snapshotHandler.err;
		logger.field("block",    blockIndex)
			.field("snapshot", snapshot)
			.field("err",      err)
			.debug("InmemAppProxy.GetSnapshot");
		return new RResult<byte[]>(snapshot, err);
	}

	/**
	 * Restore implements AppProxy interface method, calls handler
	 */
	public error Restore(byte[] snapshot) {
		RResult<byte[]> restoreHandler = handler.RestoreHandler(snapshot);
		byte[] stateHash = restoreHandler.result;
		error err = restoreHandler.err;
		logger.field("state_hash", stateHash)
			.field("err", err)
			.debug("InmemAppProxy.Restore");
		return err;
	}

	/**
	 * SubmitTx is called by the App to submit a transaction to Lachesis
	 *
	 * @param tx
	 */
	public void SubmitTx(byte[] tx) {
		// have to make a copy, or the tx will be garbage collected and weird stuff
		// happens in transaction pool
		byte[] t = Arrays.copyOf(tx, tx.length);
		submitCh.out().write(t); // <- t;
	}
}