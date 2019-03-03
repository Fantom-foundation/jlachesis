package net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import autils.JsonUtils;
import autils.Logger;
import common.error;

public class JsonEncoder {
	private static Logger logger = Logger.getLogger(JsonEncoder.class);
	SocketChannel w;

	public JsonEncoder(SocketChannel w) {
		this.w = w;
	}

	int writeInt(int i) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(4).putInt(i);
		buffer.flip();
		int wBytes = w.write(buffer);
		return wBytes;
	}

	public error encode(int rpcType) {
		try {
			logger.field("rpcType", rpcType).debug("Encode(rpc) starts");
			writeInt(rpcType);
		} catch (IOException e) {
			e.printStackTrace();
			return error.Errorf(e.getMessage());
		}
		return null;
	}

	public error encode(error respErr) {
		logger.field("respErr", respErr).debug("Encode(err) starts");
		try {
			if (respErr == null) {
				return null;
			}

			String s = JsonUtils.ObjectToString(respErr);
			w.write(ByteBuffer.wrap(s.getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
			return error.Errorf("Encode(err) error=" + e.getMessage());
		}
		return null;
	}

	public error encode(ParsableMessage o) {
		logger.field("o", o).debug("Encode(o) starts");
		try {
			String s = o.getString();
			logger.field("s", s).debug("Encode(o) encoded result");
			w.write(ByteBuffer.wrap(s.getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
			return error.Errorf("Encode(o) error=" + e.getMessage());
		}
		return null;
	}
}
