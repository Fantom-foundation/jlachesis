package net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;

import autils.Logger;
import common.RResult;
import common.error;

/**
 * TCPStreamLayer implements StreamLayer interface for plain TCP.
 */
class TCPStreamLayer implements StreamLayer {
	InetAddress advertise;
	ServerSocket listener;

	private static Logger logger = Logger.getLogger(TCPStreamLayer.class);

	public TCPStreamLayer(InetAddress advertise, ServerSocket listener) {
		this.advertise = advertise;
		this.listener = listener;
	}

	public RResult<Socket> dial(String address, Duration timeout) {
		logger.field("address", address).field("timeout", timeout.toMillis()).debug("Dial");

		Socket socket;
		try {
			logger.field("listener", listener).debug("Connecting to " + address + " on port " + listener.getLocalPort());
			socket = new Socket(address, listener.getLocalPort());
			logger.field("client socket", socket).debug("Just connected to " + socket.getRemoteSocketAddress());
			socket.setSoTimeout((int) timeout.toMillis());
		} catch (IOException e) {
			e.printStackTrace();
			return new RResult<Socket>(null, error.Errorf(e.getMessage()));
		}

		logger.field("socket", socket).debug("Dial()");
		return new RResult<Socket>(socket, null);
	}

	public RResult<Socket> accept() {
		//logger.debug("Accept()");
		Socket accept;
		try {
			accept = listener.accept();
			//logger.field("accept", accept).debug("Accept()");
		} catch (IOException e) {
			return new RResult<Socket>(null, error.Errorf(e.getMessage()));
		}
		return new RResult<Socket>(accept, null);
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
		return listener.getInetAddress();
	}
}