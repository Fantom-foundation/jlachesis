package dummy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import autils.Appender;
import autils.Logger;
import common.RResult;
import common.error;
import proxy.ProxyHandler;


/*
 * The dummy App is used for testing and as an example for building Lachesis
 * applications. Here, we define the dummy's state which doesn't really do
 * anything useful. It saves and logs block transactions. The state hash is
 * computed by cumulatively hashing transactions together as they come in.
 * Snapshots correspond to the state hash resulting from executing a the block's
 * transactions.
 */
/**
 * State implements ProxyHandler
 */
public class State implements ProxyHandler {
	Logger logger;
	byte[][] committedTxs;
	byte[] stateHash;
	Map<Long, byte[]> snapshots;

	public State(Logger logger) {
		this.logger =       logger;
		this.committedTxs = new byte[][] {};
		this.stateHash=   new byte[] {};
		this.snapshots=  new HashMap<Long, byte[]>();
		logger.info("Init Dummy State");
	}

	public RResult<byte[]> CommitHandler(poset.Block block ) {
		logger.field("block", block).debug("CommitBlock");

		error err = commit(block);
		if (err != null) {
			return new RResult<byte[]>(null, err);
		}
		logger.field("stateHash", stateHash).debug("CommitBlock Answer");
		return new RResult<byte[]>(stateHash, null);
	}

	public RResult<byte[]> SnapshotHandler(long blockIndex) {
		logger.field("block", blockIndex).debug("GetSnapshot");

		byte[] snapshot = snapshots.get(blockIndex);;
		boolean ok = snapshot != null;
		if (!ok){
			return new RResult<byte[]>(null, error.Errorf(String.format("snapshot %d not found", blockIndex)));
		}

		return new RResult<byte[]>(snapshot, null);
	}

	public RResult<byte[]> RestoreHandler(byte[] snapshot) {
		//TODO do something smart here
		stateHash = snapshot;
		return new RResult<byte[]>(stateHash, null);
	}

	public byte[][] GetCommittedTransactions() {
		return committedTxs;
	}

	public error commit(poset.Block block)  {
		committedTxs = Appender.append(committedTxs, block.transactions());
		// log tx and update state hash
		byte[] hash = stateHash;
		for (byte[] tx : block.transactions()) {
			logger.info(Arrays.toString(tx));
			hash = crypto.hash.SimpleHashFromTwoHashes(hash, crypto.hash.SHA256(tx));
		}
		snapshots.put(block.Index(), hash);
		stateHash = hash;
		return null;
	}
}