package net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.Socket;
import java.net.SocketException;
import java.time.Duration;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jcsp.lang.Alternative;
import org.jcsp.lang.CSTimer;
import org.jcsp.lang.Channel;
import org.jcsp.lang.Guard;
import org.jcsp.lang.One2OneChannel;
import org.jcsp.lang.One2OneChannelInt;

import autils.Logger;
import channel.ChannelUtils;
import channel.ExecService;
import common.RResult;
import common.error;

/**
 * NetworkTransport provides a network based transport that can be used to
 * communicate with lachesis on remote machines. It requires an underlying
 * stream layer to provide a stream abstraction, which can be simple TCP, TLS,
 * etc.
 *
 * This transport is very simple and lightweight. Each RPC request is framed by
 * sending a byte that indicates the message type, followed by the json encoded
 * request. The response is an error string followed by the response object,
 * both are encoded using msgpack
 */
public class NetworkTransport implements Transport {

	public static final error ErrTransportShutdown = error.Errorf("transport shutdown");

	Logger logger;

	ConcurrentMap<String, Stack<NetConn>> connPool; // map[string][]*netConn
	int maxPool;

	One2OneChannel<RPC> consumeCh; // chan RPC

	boolean shutdown;
	One2OneChannelInt shutdownCh; // chan struct{}
	Lock shutdownLock;

	StreamLayer stream;

	Duration timeout;

	/**
	 *  Creates a new network transport with the given dialer
	 *  and listener. The maxPool controls how many connections we will pool (per
	 *  target). The is used to apply I/O deadlines.
	 * @param stream
	 * @param maxPool
	 * @param timeout
	 * @param logger
	 */
	public NetworkTransport(StreamLayer stream, int maxPool, Duration timeout, Logger logger) {
		//if (logger == null) {
			logger = Logger.getLogger(this.getClass());
		//}
		this.connPool = new ConcurrentHashMap<String, Stack<NetConn>>();
		this.consumeCh = Channel.one2one(); // make(chan RPC),
		this.logger = logger;
		this.maxPool = maxPool;
		this.shutdownCh = Channel.one2oneInt();
		this.shutdownLock = new ReentrantLock();
		this.stream = stream;
		this.timeout = timeout;
		this.logger = logger;

		logger.debug("NetworkTransport()");
		ExecService.go(() -> listen());
	}

	// Close is used to stop the network transport.
	public error close() {
		shutdownLock.lock();
		try {
			if (!shutdown) {
				ChannelUtils.close(shutdownCh);
				stream.close();
				shutdown = true;
			}
			return null;
		} finally {
			shutdownLock.unlock();
		}
	}

	// Consumer implements the Transport interface.
	public One2OneChannel<RPC> getConsumer() {
		return consumeCh;
	}

	// LocalAddr implements the Transport interface.
	public String localAddr() {
		return stream.addr().getHostAddress();
	}

	/**
	 * IsShutdown is used to check if the transport is shutdown.
	 * @return
	 */
	public boolean IsShutdown() {
//		select {
//		case <-shutdownCh:
//			return true;
//		default:
//			return false;
//		}
		//logger.debug("isShutdown() start");

		CSTimer tim = new CSTimer();
		final Alternative alt = new Alternative (new Guard[] {shutdownCh.in(), tim});
		final int SHUTDOWN = 0, TIM = 1;

		switch (alt.priSelect()) {
		case SHUTDOWN:
			int read = shutdownCh.in().read();
			logger.field("read", read).debug("isShutdown() ends");
			return true;
		case TIM:
			tim.setAlarm (tim.read() + timeout.toMillis());
			//logger.debug("isShutdown() ends timeout ");
			return false;
		}
		return false;
	}


	/**
	 * Grabs a pooled connection.
	 * @param target
	 * @return
	 */
	public NetConn getPooledConn(String target) {
		Stack<NetConn> conns = connPool.get(target);
		if (conns == null || conns.size() == 0) {
			connPool.put(target, new Stack<>());
			return null;
		}

		NetConn conn = conns.pop();
		connPool.put(target, conns);
		return conn;
	}

	/**
	 * getConn is used to get a connection from the pool.
	 * @param target
	 * @param timeout
	 * @return
	 */
	public RResult<NetConn> getConn(String target, Duration timeout) {
		// Check for a pooled conn
		NetConn conn = getPooledConn(target);
		if (conn != null) {
			return new RResult<NetConn>(conn, null);
		}
		logger.field("conn", conn).debug("after pooled connection");

		// Dial a new connection
		logger.field("target", target)
			.field("timeout", timeout.toMillis()).debug("Dialing");


		RResult<Socket> dialCall = stream.dial(target, timeout);
		Socket conn2 = dialCall.result;
		error err = dialCall.err;
		if (err != null) {
			return new RResult<NetConn>(null, err);
		}

		// Wrap the conn
		NetConn netConn;
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
			PrintWriter w = new PrintWriter(conn2.getOutputStream(), true);
			netConn = new NetConn(target, conn2, r, w);

			// Setup encoder/decoders
			netConn.dec = new JsonDecoder(r);
			netConn.enc = new JsonEncoder(w);
		} catch (IOException e) {
			return new RResult<NetConn>(null, error.Errorf(e.getMessage()));
		}

		// Done
		return new RResult<NetConn>(netConn, null);
	}

	/**
	 * Returns a connection back to the pool.
	 * @param conn
	 */
	public void returnConn(NetConn conn) {
		String key = conn.target;
		Stack<NetConn> conns = connPool.get(key);

		if (!IsShutdown() && conns != null && conns.size() < maxPool) {
			conns.add(conn);
		} else {
			conn.release();
		}
	}

	// Sync implements the Transport interface.
	public error sync(String target, SyncRequest args, SyncResponse resp) {
		return genericRPC(target, NetworkTransportType.rpcSync.ordinal(), args, resp);
	}

	// EagerSync implements the Transport interface.
	public error eagerSync(String target, EagerSyncRequest args, EagerSyncResponse resp) {
		return genericRPC(target, NetworkTransportType.rpcEagerSync.ordinal(), args, resp);
	}

	// FastForward implements the Transport interface.
	public error fastForward(String target, FastForwardRequest args, FastForwardResponse resp) {
		return genericRPC(target, NetworkTransportType.rpcFastForward.ordinal(), args, resp);
	}

	/**
	 * Handles a simple request/response RPC.
	 * @param target
	 * @param rpcType
	 * @param args
	 * @param resp
	 * @return
	 */
	public error genericRPC(String target, int rpcType, ParsableMessage args, ParsableMessage resp) {
		logger.field("target", target).field("rpcType", rpcType).debug("genericRPC");

		// Get a conn
		RResult<NetConn> connCall = getConn(target, timeout);
		NetConn conn = connCall.result;
		error err = connCall.err;
		if (err != null) {
			return err;
		}

		// Set a deadline
		if (timeout.getSeconds() > 0) {
			logger.field("timeout", timeout.toMillis()).debug("SetSoTimeout()");
			try {
				conn.conn.setSoTimeout((int) timeout.toMillis());
			} catch (SocketException e) {
				e.printStackTrace();
				return error.Errorf(e.getMessage());
			}
		}

		// Send the RPC
		err = sendRPC(conn, rpcType, args);
		logger.field("err", err).debug("sendRPC finished");

		if (err != null) {
			return err;
		}

		// Decode the response
		logger.field("conn", conn).debug("sendRPC decoding response from conn");
		RResult<Boolean> decodeResponse = decodeResponse(conn, resp);
		boolean canReturn = decodeResponse.result;
		err = decodeResponse.err;
		logger.field("err", err).field("canReturn", canReturn).debug("decodeResponse finished");

		if (canReturn) {
			returnConn(conn);
		}

		return err;
	}

	/**
	 * Encode and send the RPC.
	 * @param conn
	 * @param rpcType
	 * @param args
	 * @return
	 */
	public error sendRPC(NetConn conn, int rpcType, ParsableMessage args) {
		logger.field("conn", conn)
			.field("rpcType", rpcType)
			.field("args", args).debug("sendRPC()");

		// Write the request type
		error err = conn.enc.encode(rpcType);
		logger.field("err", err).debug("sendRPC() encoding rpctype");
		if (err != null) {
			conn.release();
			return err;
		}

		// Send the request
		err = conn.enc.encode(args);
		logger.field("err", err).debug("sendRPC() Encoding finished");

		if (err != null) {
			conn.release();
			return err;
		}

		// Flush
		try {
			logger.field("err", err).debug("sendRPC() flushing");
			conn.w.flush();
		} catch (IOException e) {
			logger.field("err", err).debug("sendRPC() flushing error");
			e.printStackTrace();
			conn.release();
			return error.Errorf(e.getMessage());
		}
		return null;
	}

	/**
	 * Decode an RPC response and reports whether the connection can be reused.
	 * @param conn
	 * @param resp
	 * @return
	 */
	public RResult<Boolean> decodeResponse(NetConn conn, ParsableMessage resp) {
		logger.field("resp", resp).debug("decodeResponse() start");

		// Decode the error if any
		error rpcError = new error(null);
		error err = conn.dec.decode(rpcError);

		logger.field("rpcError", rpcError)
			.field("err", err).debug("decodeResponse() decoded the error");

		if (err != null) {
			conn.release();
			return new RResult<Boolean>(false, err);
		}

		// Decode the response
		err = conn.dec.decode(resp);
		logger.field("resp", resp)
			.field("err", err).debug("decodeResponse() decoded resp");

		if (err != null) {
			conn.release();
			return new RResult<Boolean>(false, err);
		}

		// Format an error if any
		if (rpcError.Error() != null) {
			return new RResult<Boolean>(false, rpcError);
		}

		return new RResult<Boolean>(true, null);
	}

	/**
	 * Listening and handling incoming connections.
	 */
	public void listen() {
		logger.field("addr", localAddr()).info("Listening");

		while (true) {
			// Accept incoming connections
			RResult<Socket> accept = stream.accept();
			Socket conn = accept.result;
			error err = accept.err;
			if (err != null) {
				if (IsShutdown()) {
					return;
				}
				//logger.field("error", err).error("Failed to accept connection");
				continue;
			}
			logger.field("node", conn.getLocalAddress())
					.field("from", conn.getRemoteSocketAddress())
					.field("conn", conn)
					.info("connection accepted. server socket");

			// Handle the connection in dedicated routine
			ExecService.go(() -> handleConn(conn));
		}
	}

	/**
	 * Handle an inbound connection for its lifespan.
	 * @param conn
	 */
	public void handleConn(Socket conn) {
		logger.error("handleConn() conn=" + conn);
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			PrintWriter w = new PrintWriter(conn.getOutputStream(), true);
			JsonDecoder dec = new JsonDecoder(r);
			JsonEncoder enc = new JsonEncoder(w);

			while (true) {
				logger.error("handleConn() LOOPING conn=" + conn);
				error err = handleCommand(r, dec, enc);
				logger.field("err", err).error("handleConn() handleCommand finished");

				if (err != null) {
					// FIXIT: should we check for ErrTransportShutdown here as well?
					// TODO how to convert go's EOF
					// if (err != io.EOF && err != ErrTransportShutdown) {
					if (err != ErrTransportShutdown) {
						 logger.field("error", err).error("Failed to decode incoming command");
					}
					return;
				}
				w.flush();
//				if (err != null) {
//					logger.field("error", err).error("Failed to flush response");
//					return;
//				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("handleConnect error " + e);
		} finally {
			try {
				conn.close();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("Connection close error " + e);
				return;
			}
		}
	}

	/**
	 * Handles a command by decoding and dispatching a single command.
	 * @param r
	 * @param dec
	 * @param enc
	 * @return
	 */
	public error handleCommand(Reader r, JsonDecoder dec, JsonEncoder enc) {
		// Get the rpc type
		RResult<Integer> readRpc = dec.readRpc();
		int rpcType = readRpc.result;
		error err = readRpc.err;
		if (err != null) {
			return err;
		}

		// Create the RPC object
		One2OneChannel<RPCResponse> respCh = Channel.one2one(); // make(chan RPCResponse, 1);
		RPC rpc = new RPC(respCh);

		NetworkTransportType retrievedRpc = NetworkTransportType.values[rpcType];
		logger.field("retrievedRpc", retrievedRpc).debug("handleCommand()");

		// Decode the command
		switch (retrievedRpc) {
		case rpcSync:
			SyncRequest sreq = new SyncRequest();
			err = dec.decode(sreq);
			if (err != null) {
				return err;
			}
			rpc.setCommand(sreq);
			break;
		case rpcEagerSync:
			EagerSyncRequest esreq = new EagerSyncRequest();
			err = dec.decode(esreq);
			if (err != null) {
				return err;
			}
			rpc.setCommand(esreq);
			break;
		case rpcFastForward:
			FastForwardRequest ffreq = new FastForwardRequest();
			err = dec.decode(ffreq);
			if (err != null) {
				return err;
			}
			rpc.setCommand(ffreq);
			break;
		default:
			return error.Errorf(String.format("unknown rpc type %d", rpcType));
		}

//		 Dispatch the RPC
//		 TODO semantics the same?
//		select {
//		case consumeCh <- rpc:
//		case <-shutdownCh:
//			return ErrTransportShutdown;
//		}

		logger.debug("handleCommand() dispatching the RPC");

		final Alternative alt = new Alternative(new Guard[] {consumeCh.in(), shutdownCh.in()});
		final int CONSUME = 0, SHUTDOWN = 1;

		switch (alt.priSelect()) {
		default:
			logger.debug("handleCommand() consuming");
			consumeCh.out().write(rpc);
			break;
		case SHUTDOWN:
			logger.debug("handleCommand() shutdown case");
			shutdownCh.in().read();
			return ErrTransportShutdown;
		}

//		// Wait for response
//		select {
//			case resp := <-respCh:
//				// Send the error first
//				respErr = error.Errorf("");
//				if (resp.Error != null) {
//					respErr = resp.Error.Error();
//				}
//				err = enc.Encode(respErr);
//				if ( err != null) {
//					return err;
//				}
//
//				// Send the response
//				err = enc.Encode(resp.Response);
//				if  (err != null) {
//					return err;
//				}
//			case <-shutdownCh:
//				return ErrTransportShutdown;
//		}

		logger.debug("Wait for a response");

		final Alternative alt2 = new Alternative(new Guard[] { respCh.in(), shutdownCh.in()});
		final int RESPONSE = 0, SHUTDOWN2 = 1;

		RPCResponse resp;
		error respErr;
		switch (alt2.priSelect()) {
		case RESPONSE:
			logger.debug("Reading response channel");
			resp = respCh.in().read();
			// Send the error first
			respErr = null;
			if (resp.Error != null) {
				respErr = resp.Error;
			}
			if (respErr != null) {
				err = enc.encode(respErr);
				if (err != null) {
					return err;
				}
			}

			// Send the response
			err = enc.encode(resp.Response);
			if (err != null) {
				return err;
			}
		case SHUTDOWN2:
			shutdownCh.in().read();
			return ErrTransportShutdown;
		}

		return null;
	}
}