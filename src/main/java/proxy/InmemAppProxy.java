package proxy;

import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jcsp.lang.Channel;
import org.jcsp.lang.One2OneChannel;

import common.RetResult;
import common.error;
import poset.InternalTransaction;

/**
 * InmemAppProxy implements the AppProxy interface natively
 */
public class InmemAppProxy implements AppProxy {
	Logger logger;
	ProxyHandler handler;
	One2OneChannel<byte[]> submitCh; //         chan []byte
	One2OneChannel<poset.InternalTransaction> submitInternalCh; // chan poset.InternalTransaction

	/**
	 * Constructor instantiates an InmemProxy from a set of handlers
	 * @param handler
	 * @param logger
	 */
	public InmemAppProxy(ProxyHandler handler , Logger logger) {
		super();

		if (logger == null) {
			logger = Logger.getLogger(InmemAppProxy.class);
			logger.setLevel(Level.DEBUG);
		}

		this.logger =           logger;
		this.handler =          handler;
		this.submitCh =         Channel.one2one();// make(chan []byte);
		this.submitInternalCh = Channel.one2one(); //make(chan poset.InternalTransaction);
	}

	/*
	 * inmem interface: AppProxy implementation
	 */

	// SubmitCh implements AppProxy interface method
	public One2OneChannel<byte[]> SubmitCh() /* chan []byte */ {
		return submitCh;
	}
	public void ProposePeerAdd(peers.Peer peer ) {
		submitInternalCh.out().write( new  InternalTransaction(poset.TransactionType.PEER_ADD, peer)); //<- poset.NewInternalTransaction(poset.TransactionType.PEER_ADD, peer);
	}
	public void ProposePeerRemove(peers.Peer peer) {
		submitInternalCh.out().write(new InternalTransaction(poset.TransactionType.PEER_REMOVE, peer)); // <- poset.NewInternalTransaction(poset.TransactionType.PEER_REMOVE, peer);
	}

	/**
	 * SubmitCh returns the channel of raw transactions
	 */
	public One2OneChannel<poset.InternalTransaction> SubmitInternalCh() /*chan poset.InternalTransaction*/ {
		return submitInternalCh;
	}

	// CommitBlock implements AppProxy interface method, calls handler
	public RetResult<byte[]> CommitBlock(poset.Block block ) {
		RetResult<byte[]> commitHandler = handler.CommitHandler(block);
		byte[] stateHash = commitHandler.result;
		error err = commitHandler.err;

//		logger.WithFields(logrus.Fields{
//			"round_received": block.RoundReceived(),
//			"txs":            len(block.Transactions()),
//			"state_hash":     stateHash,
//			"err":            err,
//		}).Debug("InmemAppProxy.CommitBlock");
		return new RetResult<byte[]>(stateHash, err);
	}

	// GetSnapshot implements AppProxy interface method, calls handler
	public RetResult<byte[]> GetSnapshot(long blockIndex) {
		RetResult<byte[]> snapshotHandler = handler.SnapshotHandler(blockIndex);
		byte[] snapshot = snapshotHandler.result;
		error err = snapshotHandler.err;
//		logger.WithFields(logrus.Fields{
//			"block":    blockIndex,
//			"snapshot": snapshot,
//			"err":      err,
//		}).Debug("InmemAppProxy.GetSnapshot");
		return new RetResult<byte[]>(snapshot, err);
	}

	/**
	 * Restore implements AppProxy interface method, calls handler
	 */
	public error Restore(byte[] snapshot) {
		RetResult<byte[]> restoreHandler = handler.RestoreHandler(snapshot);
		byte[] stateHash = restoreHandler.result;
		error err = restoreHandler.err;
//		logger.WithFields(logrus.Fields{
//			"state_hash": stateHash,
//			"err":        err,
//		}).Debug("InmemAppProxy.Restore");
		return err;
	}

	/**
	 * SubmitTx is called by the App to submit a transaction to Lachesis
	 * @param tx
	 */
	public void SubmitTx(byte[] tx) {
		//have to make a copy, or the tx will be garbage collected and weird stuff
		//happens in transaction pool
		byte[] t = Arrays.copyOf(tx,  tx.length);
		submitCh.out().write(t); // <- t;
	}
}