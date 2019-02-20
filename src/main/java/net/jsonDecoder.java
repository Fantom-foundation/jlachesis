package net;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import autils.JsonUtils;
import autils.Logger;
import common.RResult;
import common.error;

public class jsonDecoder {
	private static Logger logger = Logger.getLogger(jsonDecoder.class);

	Reader r;
	final char[] buf = new char[8192];

	public jsonDecoder(Reader r) {
		this.r = r;
	}

	public RResult<Integer> readRpc() {
		int rpcType = 0;
		error err = null;
		try {
			rpcType = r.read();
		} catch (IOException e) {
			err = error.Errorf(e.getMessage());
		}
		return new RResult<>(rpcType, err);
	}

	public error Decode(error rpcError) {
		logger.field("rpcError", rpcError).debug("Decode(err) starts");
		try {
			String s = readError();
			error parsedErr = JsonUtils.StringToObject(s, error.class);
		    logger.field("parsedErr", parsedErr).debug("Decode(err)");
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

	public <T extends ParsableMessage> error Decode(T resp) {
		logger.field("resp", resp).debug("Decode(T) starts");

		RResult<String> readString = read();
	    String s = readString.result;
	    error err = readString.err;
	    logger.field("s", s).field("err", err).debug("Decode(T)");
	    resp.parseFrom(s);

	    logger.field("s", s).field("resp", resp).debug("Decode(T)");

	    if (err != null) {
	    	return err;
	    }

		err = resp.parseFrom(s);
		return err;
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

	private RResult<String> read() {
		Writer writer = new StringWriter();
		error err = null;
		int nrBytes;
	    try {
			while ((nrBytes = r.read(buf)) != -1) {
				writer.write(buf, 0, nrBytes);
			}
		} catch (IOException e) {
			e.printStackTrace();
			err = error.Errorf(e.getMessage());
		} finally {
			try {
			    writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
				err = error.Errorf(e.getMessage());
			}
        }

	    String s = writer.toString();
	    return new RResult<>(s, err);
	}
}
