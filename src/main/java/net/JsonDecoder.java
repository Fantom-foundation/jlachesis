package net;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.SocketTimeoutException;

import autils.JsonUtils;
import autils.Logger;
import common.RResult;
import common.error;

public class JsonDecoder {
	private static Logger logger = Logger.getLogger(JsonDecoder.class);

	Reader r;
	final char[] buf = new char[8192];

	public JsonDecoder(Reader r) {
		this.r = r;
	}

	public RResult<Integer> readRpc() {
		int rpcType = 0;
		error err = null;
		try {
			rpcType = r.read();
			logger.field("rpcType", rpcType).debug("readRpc()");

		} catch (IOException e) {
			err = error.Errorf(e.getMessage());
		}
		return new RResult<>(rpcType, err);
	}

	public error decode(error rpcError) {
		logger.field("rpcError", rpcError).debug("decode(err) starts");
		try {
			String s = readError();
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
			int length = r.read();
			String s = read(length);
			logger.field("length",  length).field("s", s).debug("decode(T) reads length and s");
		    resp.parseFrom(s);
		    logger.field("resp", resp).debug("decode(T) parsed resp");
			error err = resp.parseFrom(s);
			return err;
		} catch (IOException e) {
			e.printStackTrace();
			return error.Errorf(e.getMessage());
		}
	}

	private String readError() throws IOException, SocketTimeoutException {
		Writer writer = new StringWriter();
		int nrBytes;
		while ((nrBytes = r.read(buf)) != -1) {
			writer.write(buf, 0, nrBytes);
		}
	    writer.flush();
		writer.close();
	    return writer.toString();
	}

//	private RResult<String> read() {
//		Writer writer = new StringWriter();
//		error err = null;
//		int nrBytes;
//	    try {
//			while ((nrBytes = r.read(buf)) != -1) {
//				writer.write(buf, 0, nrBytes);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//			err = error.Errorf(e.getMessage());
//		} finally {
//			try {
//			    writer.flush();
//				writer.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//				err = error.Errorf(e.getMessage());
//			}
//        }
//
//	    String s = writer.toString();
//	    return new RResult<>(s, err);
//	}

	private String read(int bytes) throws IOException {
		Writer writer = new StringWriter();
		int nrBytes;
		char[] buf = new char[bytes];
		while ((nrBytes = r.read(buf)) != -1) {
			writer.write(buf, 0, nrBytes);
		}
	    writer.flush();
		writer.close();
	    return writer.toString();
	}
}
