package net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.time.Duration;

import autils.Logger;
import common.NetUtils;
import common.RetResult;
import common.error;

public class TCPTransport {

	public static final error errNotAdvertisable = error.Errorf("local bind address is not advertisable");
	public static final error errNotTCP = error.Errorf("local address is not a TCP address");

	interface TCPTransportCreator {
		public NetworkTransport transportCreator(StreamLayer stream);
	}

	/**
	 * Creates a new TCPTransport
	 * @param bindAddr
	 * @param advertise
	 * @param maxPool
	 * @param timeout
	 * @param logger
	 * @return a NetworkTransport that is built on top of
	 * a TCP streaming transport layer, with log output going to the supplied Logger
	 */
	public static RetResult<NetworkTransport> NewTCPTransport(String bindAddr, InetAddress advertise, int maxPool,
			Duration timeout, Logger logger) {
		return newTCPTransport(bindAddr, advertise, maxPool, timeout, new TCPTransportCreator() {
			public NetworkTransport transportCreator(StreamLayer stream) {
				return new NetworkTransport(stream, maxPool, timeout, logger);
			}
		});
	}

	public static RetResult<NetworkTransport> newTCPTransport(String bindAddr, InetAddress advertise, int maxPool,
			Duration timeout, TCPTransportCreator transportCreator) {
		// Try to bind
//		list, err := net.Listen("tcp", bindAddr);

		RetResult<ServerSocket> bind = NetUtils.bind(bindAddr);
		ServerSocket list = bind.result;
		error err = bind.err;
		if (err != null) {
			return new RetResult<NetworkTransport>(null, err);
		}

		// Create stream
		TCPStreamLayer stream = new TCPStreamLayer(advertise, list);

		// Verify that we have a usable advertise address
		InetAddress addr = stream.addr();

		boolean ok = addr != null;
		try {
			if (!ok) {
				list.close();
				return new RetResult<NetworkTransport>(null, errNotTCP);
			}
			if (addr.getHostAddress().isEmpty()) {// .IP.IsUnspecified()) {
				list.close();
				return new RetResult<NetworkTransport>(null, errNotAdvertisable);
			}
		} catch (IOException e) {
			return new RetResult<NetworkTransport>(null, error.Errorf(e.getMessage()));
		}
		// Create the network transport
		NetworkTransport trans = transportCreator.transportCreator(stream);
		return new RetResult<NetworkTransport>(trans, null);
	}
}