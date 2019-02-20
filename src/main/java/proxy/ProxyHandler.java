package proxy;

import common.RResult;

/**
 * ProxyHandler provides an interface for the application to set handlers for
 * commiting, retreiving and restoring state and transactions, to and from the
 * DAG
 */
public interface ProxyHandler {
	/**
	 * CommitHandler is called when Lachesis commits a block to the DAG. It returns
	 * the state hash resulting from applying the block's transactions to the state.
	 * 
	 * @param block
	 * @return
	 */
	RResult<byte[]> CommitHandler(poset.Block block); // return (stateHash []byte, err error)

	/**
	 * SnapshotHandler is called by Lachesis to retrieve a snapshot corresponding to
	 * a particular block.
	 * 
	 * @param blockIndex
	 * @return
	 */
	RResult<byte[]> SnapshotHandler(long blockIndex); // return (snapshot []byte, err error)

	/**
	 * RestoreHandler is called by Lachesis to restore the application to a specific
	 * 
	 * @param snapshot
	 * @return
	 */
	RResult<byte[]> RestoreHandler(byte[] snapshot); // return (stateHash []byte, err error)
}
