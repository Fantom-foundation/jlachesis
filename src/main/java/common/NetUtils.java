package common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;

public class NetUtils {

	/**
	 * Creates a server socket and binds to the specified address
	 * @param bindAddr a bind address in the form of ":9000"
	 * @return
	 */
	public static RResult<ServerSocketChannel> bind(String bindAddr) {
		try {
			int port = parsePort(bindAddr);
			String addr = parseAddress(bindAddr);
			ServerSocketChannel serverSocket = ServerSocketChannel.open();
			serverSocket.configureBlocking(false);
	        serverSocket.socket().bind(new InetSocketAddress(addr, port));
	        return new RResult<>(serverSocket, null);
		} catch (Exception e) {
			return new RResult<>(null, error.Errorf(e.getMessage()));
		}
	}

	public static int parsePort(String bindAddr) {
		int port = 0;
		String[] tokens = bindAddr.split(":");
		if (tokens.length == 2) {
			port = Integer.parseInt(tokens[1]);
		}
		return port;
	}

	public static String parseAddress(String bindAddr) {
		String[] tokens = bindAddr.split(":");
		if (tokens.length == 0) {
			return "";
		}
		if (tokens.length == 2) {
			return tokens[0];
		}
		return bindAddr;
	}

	public static String getUnusedNetAddr() {
		ServerSocket server = null;
		try {
			server= new ServerSocket(0, 50);
			return String.format("127.0.0.1:%d", server.getLocalPort());
		} catch (Exception e) {
			// If there's an error it likely means no ports available
			System.err.println("No port available. Something is wrong");
			System.exit(1);
		} finally {
			try {
				if (server != null)
					server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
