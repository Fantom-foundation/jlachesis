package net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;

import common.RetResult;
import common.error;

// TCPStreamLayer implements StreamLayer interface for plain TCP.
class TCPStreamLayer implements StreamLayer {
	InetAddress advertise;
	ServerSocket listener;
//	net.TCPListener listener;

	public TCPStreamLayer(InetAddress advertise, ServerSocket listener) {
		this.advertise = advertise;
		this.listener = listener;
	}

	// Dial implements the StreamLayer interface.
	public RetResult<Socket> Dial(String address, Duration timeout) {
//		ServerSocket list;
		Socket socket;
		try {
//			list = new ServerSocket(0, 50, InetAddress.getByName(address));
//			list.setSoTimeout((int) timeout.toMillis());

			socket = new Socket();
			socket.connect(new InetSocketAddress(address, 0), (int) timeout.toMillis());
		} catch (IOException e) {
			return new RetResult<Socket>(null, error.Errorf(e.getMessage()));
		}
		return new RetResult<Socket>(socket, null);

//		return net.DialTimeout("tcp", address, timeout);
	}

	// Accept implements the net.Listener interface.
	public RetResult<Socket> Accept() {
		Socket accept;
		try {
			accept = listener.accept();
		} catch (IOException e) {
			return new RetResult<Socket>(null, error.Errorf(e.getMessage()));
		}
		return new RetResult<Socket>(accept, null);
	}

	// Close implements the net.Listener interface.
	public error Close()  {
		try {
			listener.close();
			return null;
		} catch (IOException e) {
			return error.Errorf(e.getMessage());
		}
	}

	// Addr implements the net.Listener interface.
	public InetAddress Addr() {
		// Use an advertise addr if provided
		if (advertise != null) {
			return advertise;
		}
		return listener.getInetAddress();//.Addr();
	}
}