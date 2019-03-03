package net;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import autils.Logger;
import common.error;

public class NetConn {
	String target;
	SocketChannel conn;
	JsonDecoder dec;
	JsonEncoder enc;

	private static Logger logger = Logger.getLogger(NetConn.class);

	public NetConn(String target, SocketChannel conn) {
		super();
		this.target = target;
		this.conn = conn;

		// Setup encoder/decoders
		dec = new JsonDecoder(conn);
		enc = new JsonEncoder(conn);
	}

	public error release() {
		try {
			logger.field("conn", this).debug("release() close connection !!!");

			conn.close();
		} catch (IOException e) {
			return error.Errorf(e.getMessage());
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("netConn [target=").append(target).append(", conn=").append(conn).
				append(", dec=").append(dec).append(", enc=").append(enc).append("]");
		return builder.toString();
	}
}