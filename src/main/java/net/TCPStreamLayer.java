package net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.Duration;

import autils.Logger;
import common.RResult;
import common.error;

/**
 * TCPStreamLayer implements StreamLayer interface for plain TCP.
 */
class TCPStreamLayer implements StreamLayer {
	InetAddress advertise;
	ServerSocketChannel listener;
	Selector selector;

	private static Logger logger = Logger.getLogger(TCPStreamLayer.class);

	public TCPStreamLayer(InetAddress advertise, ServerSocketChannel listener) {
		this.advertise = advertise;
		this.listener = listener;

		try {
			selector = Selector.open();
	        listener.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public RResult<SocketChannel> dial(String address, Duration timeout) {
		logger.field("address", address).field("timeout", timeout.toMillis()).debug("Dial");

		SocketChannel socket;
		try {
			logger.field("listener", listener).debug("Connecting to " + address + " on port " + listener.socket().getLocalPort());
			socket = SocketChannel.open(new InetSocketAddress(address, listener.socket().getLocalPort()));
			logger.field("client socket", socket).debug("Just connected to " + socket.socket().getRemoteSocketAddress());
			socket.configureBlocking(false);
			socket.socket().setKeepAlive(true);
			socket.setOption(java.net.StandardSocketOptions.TCP_NODELAY, true);
			//socket.socket().setSoTimeout((int) timeout.toMillis());
		} catch (IOException e) {
			e.printStackTrace();
			return new RResult<>(null, error.Errorf(e.getMessage()));
		}

		logger.field("socket", socket).debug("Dial()");
		return new RResult<>(socket, null);
	}

	public RResult<SocketChannel> accept() {
		//logger.debug("Accept()");
		SocketChannel client;

		try {
        	client = listener.accept();
			logger.field("accept", client).debug("Accept()");
			client.configureBlocking(false);
			client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		} catch (IOException e) {
			return new RResult<>(null, error.Errorf(e.getMessage()));
		}
		return new RResult<>(client, null);
	}

	public Selector selector() {
		return selector;
	}

	public error close()  {
		try {
			listener.close();
			return null;
		} catch (IOException e) {
			return error.Errorf(e.getMessage());
		}
	}

	public InetAddress addr() {
		// Use an advertise addr if provided
		if (advertise != null) {
			return advertise;
		}
		return listener.socket().getInetAddress();
	}
}