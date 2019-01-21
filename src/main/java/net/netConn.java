package net;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;

import common.error;

public class netConn {
	String target;
	Socket conn;
	Reader r;
	Writer w;
	jsonDecoder dec;
	jsonEncoder enc;

	public netConn(String target, Socket conn, Reader r, Writer w) {
		super();
		this.target = target;
		this.conn = conn;
		this.r = r;
		this.w = w;
	}

	public error Release() {
		try {
			conn.close();
		} catch (IOException e) {
			return error.Errorf(e.getMessage());
		}
		return null;
	}
}