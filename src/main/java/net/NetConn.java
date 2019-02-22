package net;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;

import common.error;

public class NetConn {
	String target;
	Socket conn;
	Reader r;
	Writer w;
	JsonDecoder dec;
	JsonEncoder enc;

	public NetConn(String target, Socket conn, Reader r, Writer w) {
		super();
		this.target = target;
		this.conn = conn;
		this.r = r;
		this.w = w;
	}

	public error release() {
		try {
			conn.close();
		} catch (IOException e) {
			return error.Errorf(e.getMessage());
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("netConn [target=").append(target).append(", conn=").append(conn).append(", r=").append(r)
				.append(", w=").append(w).append(", dec=").append(dec).append(", enc=").append(enc).append("]");
		return builder.toString();
	}
}