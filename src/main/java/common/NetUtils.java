package common;

import java.io.IOException;
import java.net.ServerSocket;

public class NetUtils {

	/**
	 * Creates a server socket and binds to the specified address
	 * @param bindAddr a bind address in the form of ":9000"
	 * @return
	 */
	public static RResult<ServerSocket> bind(String bindAddr) {
		try {
			int port = parsePort(bindAddr);
			ServerSocket list = new ServerSocket(port, 50);
			return new RResult<>(list, null);
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
