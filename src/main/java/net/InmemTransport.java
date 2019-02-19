package net;

import java.io.Reader;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jcsp.lang.Alternative;
import org.jcsp.lang.CSTimer;
import org.jcsp.lang.Channel;
import org.jcsp.lang.Guard;
import org.jcsp.lang.One2OneChannel;

import common.RetResult;
import common.error;

/**
 * InmemTransport implements the Transport interface, to allow lachesis to be
 * tested in-memory without going over a network.
 */
public class InmemTransport implements Transport {

	One2OneChannel<RPC> consumerCh; // chan RPC;
	String localAddr;
	Duration timeout;

	static Map<String, InmemTransport> inmemMedium = new HashMap<String,InmemTransport>();

	//	sync.RWMutex inmemMediumSync;
	static ReadWriteLock inmemMediumSync = new ReentrantReadWriteLock();

	// NewInmemAddr returns a new in-memory addr with
	// a randomly generate UUID as the ID.
	public String NewInmemAddr() {
		return UUID.randomUUID().toString();
	}

	public InmemTransport() {

	}

	public InmemTransport(One2OneChannel<RPC> consumerCh, String localAddr, Duration timeout) {
		super();
		this.consumerCh = consumerCh;
		this.localAddr = localAddr;
		this.timeout = timeout;
	}

	/**
	 * Constructs an InmemTransport used to initialize a new transport
	 * @param addr
	 */
	public InmemTransport(String addr) {
		if (addr.isEmpty()) {
			addr = NewInmemAddr();
		}

		consumerCh = Channel.one2one(); // make(chan RPC, 16)
		localAddr =	addr;
		timeout = Duration.ofSeconds(50);

		inmemMediumSync.writeLock().lock();
		inmemMedium.put(addr, this);
		inmemMediumSync.writeLock().unlock();
	}

	public One2OneChannel<RPC> getConsumerCh() {
		return consumerCh;
	}

	public Map<String, InmemTransport> getInmemMedium() {
		return inmemMedium;
	}

	// Consumer implements the Transport interface.
	public One2OneChannel<RPC> getConsumer() /* <-chan RPC */ {
		return consumerCh;
	}

	// LocalAddr implements the Transport interface.
	public String localAddr() {
		return localAddr;
	}

	// Sync implements the Transport interface.
	public error sync(String target, SyncRequest args, SyncResponse resp)  {
		RetResult<RPCResponse> makeRPC = makeRPC(target, args, null, timeout);
		RPCResponse rpcResp = makeRPC.result;
		error err = makeRPC.err;
		if (err != null) {
			return err;
		}

		// Copy the result back
		resp.copy((SyncResponse) rpcResp.Response);
		return null;
	}

	// Sync implements the Transport interface.
	public error eagerSync(String target, EagerSyncRequest args, EagerSyncResponse resp) {
		RetResult<RPCResponse> makeRPC = makeRPC(target, args, null, timeout);
		RPCResponse rpcResp = makeRPC.result;
		error err = makeRPC.err;

		if (err != null) {
			return err;
		}

		// Copy the result back
		resp.copy((EagerSyncResponse) rpcResp.Response);
		return null;
	}

	// FastForward implements the Transport interface.
	public error fastForward(String target, FastForwardRequest args, FastForwardResponse res)  {
		RetResult<RPCResponse> makeRPC = makeRPC(target, args, null, timeout);
		RPCResponse rpcResp = makeRPC.result;
		error err = makeRPC.err;

		if (err != null) {
			return err;
		}

		// Copy the result back
		res.copy((FastForwardResponse) rpcResp.Response);
		return null;
	}

	public <T> RetResult<RPCResponse> makeRPC(String target, T args, Reader r, Duration timeout ) {
		inmemMediumSync.readLock().lock();
		InmemTransport peer= inmemMedium.get(target);
		boolean ok = peer != null;
		inmemMediumSync.readLock().unlock();
		error err = null;
		if (!ok) {
			err = error.Errorf(String.format("failed to connect to peer: %v", target));
			return new RetResult<RPCResponse>(null, err);
		}

		// Send the RPC over
		One2OneChannel<RPCResponse> respCh = Channel.one2one(); // make(chan RPCResponse);
		peer.consumerCh.out().write(new RPC(args, r, respCh));

		// Wait for a response
//		select {
//		case rpcResp =respCh.in().read(): // <-respCh:
//			if (rpcResp.Error != null) {
//				err = rpcResp.Error;
//			}
//		case <-time.After(timeout):
//			err = error.Errorf("command timed out");
//		}

		RPCResponse rpcResp = null;
		final CSTimer tim = new CSTimer ();
		final Alternative alt = new Alternative (new Guard[] {respCh.in(), tim});
		final int EVENT = 0, TIM = 1;

		switch (alt.priSelect ()) {
			case EVENT:
				rpcResp =respCh.in().read(); // <-respCh:
				if (rpcResp.Error != null) {
					err = rpcResp.Error;
				}
			// fall through
			case TIM:
				tim.setAlarm(tim.read() + timeout.toMillis());
				err = error.Errorf("command timed out");
				break;
		}

		return new RetResult<RPCResponse>(rpcResp, err);
	}

	// Close is used to permanently disable the transport
	public error close() {
		inmemMediumSync.writeLock().lock();
		inmemMedium.remove(localAddr);
		inmemMediumSync.writeLock().unlock();
		return null;
	}
}