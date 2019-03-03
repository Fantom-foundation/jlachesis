package net;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import autils.JsonUtils;
import autils.Logger;
import common.RResult;
import common.error;

public class JsonDecoder {
	private static Logger logger = Logger.getLogger(JsonDecoder.class);

	SocketChannel r;
	private static ByteBuffer buffer;

	public JsonDecoder(SocketChannel r) {
		this.r = r;
		buffer = ByteBuffer.allocate(9256);
	}

	int readInt() throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(4);
		r.read(buf);
		return buf.getInt();
	}

	public RResult<Integer> readRpc() {
		int rpcType = 0;
		error err = null;
		try {
			rpcType = readInt();
			logger.field("rpcType", rpcType).debug("readRpc()");

		} catch (IOException e) {
			e.printStackTrace();
			err = error.Errorf(e.getMessage());
		}
		return new RResult<>(rpcType, err);
	}

	public error decode(error rpcError) {
		logger.field("rpcError", rpcError).debug("decode(err) starts");
		try {
			String s = readError();
			logger.field("s", s).debug("decode(err) read raw error msg");
			if (s == null || s.isEmpty()) {
				return null;
			}
			error parsedErr = JsonUtils.StringToObject(s, error.class);
		    logger.field("parsedErr", parsedErr).debug("decode(err)");
		    if (parsedErr != null) {
		    	rpcError.setErrMessage(parsedErr.Error());
		    }
		} catch (SocketTimeoutException e) {
			e.getStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return error.Errorf(e.getMessage());
		}

		return null;
	}

	public <T extends ParsableMessage> error decode(T resp) {
		logger.field("resp", resp).debug("decode(T) starts");

		try {
			String s = read();
			logger.field("s", s)
				.debug("decode(T) parsed resp");

		    error err = resp.parseFrom(s);
		    logger.field("resp", resp).debug("decode(T) parsed resp");

			return err;
		} catch (IOException e) {
			e.printStackTrace();
			return error.Errorf(e.getMessage());
		}
	}

	private String readError() throws IOException {
		buffer.clear();
		int nrBytes = r.read(buffer);
		if (nrBytes > 0) {
			byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			return new String(bytes);
		}
		return null;
	}

//	private String read(int bytes) throws IOException {
//		Writer writer = new StringWriter();
//		int totalReadBytes = 0;
//		int nrBytes;;
//        while ((nrBytes = r.read(buffer)) != -1 && totalReadBytes < bytes) {
//            if (totalReadBytes + nrBytes >= bytes) {
//            	buffer.clear();
//                writer.write(new String(buffer.array()));
//                totalReadBytes += nrBytes;
//                buffer.compact();
//            	break;
//            }
//        }
//	    writer.flush();
//		writer.close();
//	    return writer.toString();
//	}

	private String read() throws IOException {
		Writer writer = new StringWriter();
        if (r.read(buffer) != -1) {
        	buffer.clear();
            writer.write(new String(buffer.array()));
            buffer.compact();
        }
	    writer.flush();
		writer.close();
	    return writer.toString();
	}
}
