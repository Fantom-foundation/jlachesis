package dummy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import autils.Appender;
import common.RetResult;
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

// State implements ProxyHandler
public class State implements ProxyHandler {
	Logger logger;
	byte[][] committedTxs;
	byte[] stateHash;
	Map<Long, byte[]> snapshots;

	public State(Logger logger) {
			this.logger =       logger;
			this.committedTxs= new byte[][] {};
			this.stateHash=   new byte[] {};
			this.snapshots=  new HashMap<Long, byte[]>();
		logger.info("Init Dummy State");
	}

	/*
	 * inmem interface: ProxyHandler implementation
	 */

	public RetResult<byte[]> CommitHandler(poset.Block block ) {
//		s.logger.WithField("block", block).Debug("CommitBlock")

		error err = commit(block);
		if (err != null) {
			return new RetResult<byte[]>(null, err);
		}
//		logger.WithField("stateHash", stateHash).Debug("CommitBlock Answer")
		return new RetResult<byte[]>(stateHash, null);
	}

	public RetResult<byte[]> SnapshotHandler(long blockIndex) {
//		logger.WithField("block", blockIndex).Debug("GetSnapshot")

		byte[] snapshot = snapshots.get(blockIndex);;
		boolean ok = snapshot != null;
		if (!ok){
			return new RetResult<byte[]>(null, error.Errorf(String.format("snapshot %d not found", blockIndex)));
		}

		return new RetResult<byte[]>(snapshot, null);
	}

	public RetResult<byte[]> RestoreHandler(byte[] snapshot) {
		//XXX do something smart here
		stateHash = snapshot;
		return new RetResult<byte[]>(stateHash, null);
	}

	/*
	 * staff:
	 */
	public byte[][] GetCommittedTransactions() {
		return committedTxs;
	}

	public error commit(poset.Block block)  {
		committedTxs = Appender.append(committedTxs, block.Transactions());
		// log tx and update state hash
		byte[] hash = stateHash;
		for (byte[] tx : block.Transactions()) {
			logger.info(Arrays.toString(tx));
			hash = crypto.hash.SimpleHashFromTwoHashes(hash, crypto.hash.SHA256(tx));
		}
		snapshots.put(block.Index(), hash);
		stateHash = hash;
		return null;
	}
}