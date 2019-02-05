package common;

import java.net.ServerSocket;

public class NetUtils {

	/**
	 *
	 * @param bindAddr a bind address in the form of ":9000"
	 * @return
	 */
	public static RetResult<ServerSocket> bind(String bindAddr) {
		try {
			int port = parsePort(bindAddr);
			ServerSocket list = new ServerSocket(port, 50);
			return new RetResult<>(list, null);
		} catch (Exception e) {
			return new RetResult<>(null, error.Errorf(e.getMessage()));
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
}
