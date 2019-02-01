package net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.Socket;
import java.net.SocketException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Level;
import org.jcsp.lang.Alternative;
import org.jcsp.lang.Channel;
import org.jcsp.lang.Guard;
import org.jcsp.lang.One2OneChannel;

import autils.Appender;
import autils.Logger;
import channel.ChannelUtils;
import channel.ExecService;
import common.RetResult;
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

	Map<String, netConn[]> connPool; // map[string][]*netConn
	Lock connPoolLock;
	int maxPool;

	One2OneChannel<RPC> consumeCh; // chan RPC

	boolean shutdown;
	One2OneChannel<Object> shutdownCh; // chan struct{}
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
		if (logger == null) {
			logger = Logger.getLogger(this.getClass());
			logger.setLevel(Level.DEBUG);
//			lachesis_log.NewLocal(logger, logger.Level.String())
		}
		this.connPool = new HashMap<String, netConn[]>();
		this.consumeCh = Channel.one2one(); // make(chan RPC),
		this.logger = logger;
		this.maxPool = maxPool;
		this.shutdownCh = Channel.one2one();
		this.stream = stream;
		this.timeout = timeout;

//		go listen();
		ExecService.go(() -> listen());
	}

	// Close is used to stop the network transport.
	public error Close() {
		shutdownLock.lock();
		try {
			if (!shutdown) {
				ChannelUtils.close(shutdownCh);
				stream.Close();
				shutdown = true;
			}
			return null;
		} finally {
			shutdownLock.unlock();
		}
	}

	// Consumer implements the Transport interface.
	public One2OneChannel<RPC> Consumer() {
		return consumeCh;
	}

	// LocalAddr implements the Transport interface.
	public String LocalAddr() {
		return stream.Addr().getHostAddress();
	}

	// IsShutdown is used to check if the transport is shutdown.
	public boolean IsShutdown() {
//		select {
//		case <-shutdownCh:
//			return true;
//		default:
//			return false;
//		}

		final Alternative alt = new Alternative(new Guard[] { shutdownCh.in() });
		final int SHUTDOWN = 0;

		switch (alt.priSelect()) {
		case SHUTDOWN:
			shutdownCh.in().read();
		default:
			return false;
		}
	}

	// getPooledConn is used to grab a pooled connection.
	public netConn getPooledConn(String target) {
		connPoolLock.lock();
		try {
			netConn[] conns = connPool.get(target);
			boolean ok = conns != null;
			if (!ok || conns.length == 0) {
				return null;
			}

			int num = conns.length;
			netConn conn = conns[num - 1];
			conns[num - 1] = null;
			connPool.put(target, Arrays.copyOf(conns, num - 1));
			return conn;
		} finally {
			connPoolLock.unlock();
		}
	}

	// getConn is used to get a connection from the pool.
	public RetResult<netConn> getConn(String target, Duration timeout) {
		// Check for a pooled conn
		netConn conn = getPooledConn(target);
		if (conn != null) {
			return new RetResult<netConn>(conn, null);
		}

		// Dial a new connection
		logger.field("target", target)
				.field("timeout", timeout)
				.info("Dialing");

		RetResult<Socket> dialCall = stream.Dial(target, timeout);
		Socket conn2 = dialCall.result;
		error err = dialCall.err;
		if (err != null) {
			return new RetResult<netConn>(null, err);
		}

		// Wrap the conn
		netConn netConn;
		try {
			netConn = new netConn(target, conn2, new BufferedReader(new InputStreamReader(conn2.getInputStream())),
					new PrintWriter(conn2.getOutputStream(), true));
		} catch (IOException e) {
			return new RetResult<netConn>(null, error.Errorf(e.getMessage()));
		}

		// Setup encoder/decoders
		netConn.dec = new jsonDecoder(netConn.r);
		netConn.enc = new jsonEncoder(netConn.w);

		// Done
		return new RetResult<netConn>(netConn, null);
	}

	// returnConn returns a connection back to the pool.
	public void returnConn(netConn conn) {
		connPoolLock.lock();
		try {
			String key = conn.target;
			netConn[] conns = connPool.get(key);

			if (!IsShutdown() && conns.length < maxPool) {
				connPool.put(key, Appender.append(conns, conn));
			} else {
				conn.Release();
			}
		} finally {
			connPoolLock.unlock();
		}
	}

	// Sync implements the Transport interface.
	public error Sync(String target, SyncRequest args, SyncResponse resp) {
		return genericRPC(target, NetworkTransportType.rpcSync.ordinal(), args, resp);
	}

	// EagerSync implements the Transport interface.
	public error EagerSync(String target, EagerSyncRequest args, EagerSyncResponse resp) {
		return genericRPC(target, NetworkTransportType.rpcEagerSync.ordinal(), args, resp);
	}

	// FastForward implements the Transport interface.
	public error FastForward(String target, FastForwardRequest args, FastForwardResponse resp) {
		return genericRPC(target, NetworkTransportType.rpcFastForward.ordinal(), args, resp);
	}

	// genericRPC handles a simple request/response RPC.
	public error genericRPC(String target, int rpcType, Object args, Object resp) {
		// Get a conn
		RetResult<netConn> connCall = getConn(target, timeout);
		netConn conn = connCall.result;
		error err = connCall.err;
		if (err != null) {
			return err;
		}

		// Set a deadline
		if (timeout.getSeconds() > 0) {
//			conn.conn..SetDeadline(time.Now().Add(timeout));
			try {
				conn.conn.setSoTimeout((int) (System.currentTimeMillis() + timeout.toMillis()));
			} catch (SocketException e) {
				return error.Errorf(e.getMessage());
			}
		}

		// Send the RPC
		err = sendRPC(conn, rpcType, args);
		if (err != null) {
			return err;
		}

		// Decode the response
		RetResult<Boolean> decodeResponse = decodeResponse(conn, resp);
		boolean canReturn = decodeResponse.result;
		err = decodeResponse.err;
		if (canReturn) {
			returnConn(conn);
		}
		return err;
	}

	// sendRPC is used to encode and send the RPC.
	public error sendRPC(netConn conn, int rpcType, Object args) {
		// Write the request type
		try {
			conn.w.write(rpcType);
		} catch (IOException e) {
			conn.Release();
			return error.Errorf(e.getMessage());
		}

		// Send the request
		error err = conn.enc.Encode(args);
		if (err != null) {
			conn.Release();
			return err;
		}

		// Flush
		try {
			conn.w.flush();
		} catch (IOException e) {
			conn.Release();
			return error.Errorf(e.getMessage());
		}
		return null;
	}

	// decodeResponse is used to decode an RPC response and reports whether
	// the connection can be reused.
	public RetResult<Boolean> decodeResponse(netConn conn, Object resp) {
		// Decode the error if any
		String rpcError = null;
		error err = conn.dec.Decode(rpcError);
		if (err != null) {
			conn.Release();
			return new RetResult<Boolean>(false, err);
		}

		// Decode the response
		err = conn.dec.Decode(resp);
		if (err != null) {
			conn.Release();
			return new RetResult<Boolean>(false, err);
		}

		// Format an error if any
		if (!rpcError.isEmpty()) {
			return new RetResult<Boolean>(true, error.Errorf(rpcError));
		}
		return new RetResult<Boolean>(true, null);
	}

	// listen is used to handling incoming connections.
	public void listen() {
		logger.field("addr", LocalAddr()).info("Listening");

		while (true) {
			// Accept incoming connections
			RetResult<Socket> accept = stream.Accept();
			Socket conn = accept.result;
			error err = accept.err;
			if (err != null) {
				if (IsShutdown()) {
					return;
				}
				logger.field("error", err).error("Failed to accept connection");
				continue;
			}
			logger.field("node", conn.getLocalAddress())
					.field("from", conn.getRemoteSocketAddress())
			.info("accepted connection");

			// Handle the connection in dedicated routine
			ExecService.go(() -> handleConn(conn));
		}
	}

	// handleConn is used to handle an inbound connection for its lifespan.
	public void handleConn(Socket conn) {
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			PrintWriter w = new PrintWriter(conn.getOutputStream(), true);
			jsonDecoder dec = new jsonDecoder(r);
			jsonEncoder enc = new jsonEncoder(w);

			while (true) {
				error err = handleCommand(r, dec, enc);
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
				if (err != null) {
					 logger.field("error", err).error("Failed to flush response");
					return;
				}
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

	// handleCommand is used to decode and dispatch a single command.
	public error handleCommand(Reader r, jsonDecoder dec, jsonEncoder enc) {
		// Get the rpc type
//		rpcType, err := r.ReadByte();
		int rpcType;
		error err;
		try {
			rpcType = r.read();
		} catch (IOException e) {
			err = error.Errorf(e.getMessage());
			return err;
		}

		// Create the RPC object
		One2OneChannel<RPCResponse> respCh = Channel.one2one(); // make(chan RPCResponse, 1);
		RPC rpc = new RPC(respCh);

		NetworkTransportType retrievedRpc = NetworkTransportType.values[rpcType];
		// Decode the command
		switch (retrievedRpc) {
		case rpcSync:
			SyncRequest sreq = new SyncRequest();
			err = dec.Decode(sreq);
			if (err != null) {
				return err;
			}
			rpc.setCommand(sreq);
			break;
		case rpcEagerSync:
			EagerSyncRequest esreq = new EagerSyncRequest();
			err = dec.Decode(esreq);
			if (err != null) {
				return err;
			}
			rpc.setCommand(esreq);
			break;
		case rpcFastForward:
			FastForwardRequest ffreq = new FastForwardRequest();
			err = dec.Decode(ffreq);
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

		final Alternative alt = new Alternative(new Guard[] { shutdownCh.in() });
		final int CONSUME = 0, SHUTDOWN = 1;

		switch (alt.priSelect()) {
		default:
			consumeCh.out().write(rpc);
			break;
		// fall through
		case SHUTDOWN:
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

		final Alternative alt2 = new Alternative(new Guard[] { respCh.in(), shutdownCh.in() });
		final int RESPONSE2 = 0, SHUTDOWN2 = 1;

		RPCResponse resp;
		error respErr;
		switch (alt2.priSelect()) {
		case RESPONSE2:
			resp = respCh.in().read();
			// Send the error first
			respErr = error.Errorf("");
			if (resp.Error != null) {
				respErr = resp.Error;
			}
			err = enc.Encode(respErr);
			if (err != null) {
				return err;
			}

			// Send the response
			err = enc.Encode(resp.Response);
			if (err != null) {
				return err;
			}
			// fall through
		case SHUTDOWN2:
			shutdownCh.in().read();
			return ErrTransportShutdown;
		}

		return null;
	}
}