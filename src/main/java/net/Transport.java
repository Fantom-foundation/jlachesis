package net;

import org.jcsp.lang.One2OneChannel;

import common.error;

/**
 * Transport provides an interface for network transports
 * to allow a node to communicate with other nodes.
 */
public interface Transport {
	/**
	 * Consumer returns a channel that can be used to
	 * consume and respond to RPC requests.
	 */
	One2OneChannel<RPC> getConsumer(); // <-chan RPC

	/** LocalAddr is used to return our local address to distinguish from our peers. */
	String localAddr();

	/** Sync sends the appropriate RPC to the target node. */
	error sync(String target, SyncRequest args, SyncResponse resp);

	error eagerSync(String target, EagerSyncRequest args, EagerSyncResponse resp);

	error fastForward(String target, FastForwardRequest args, FastForwardResponse resp );

	/** Close permanently a transport, stopping
	 * any associated goroutines and freeing other resources.
	 */
	error close();
}
