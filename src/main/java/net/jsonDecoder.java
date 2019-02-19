package net;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import autils.JsonUtils;
import autils.Logger;
import common.RetResult;
import common.error;

public class jsonDecoder {
	private static Logger logger = Logger.getLogger(jsonDecoder.class);

	Reader r;
	final char[] buf = new char[8192];

	public jsonDecoder(Reader r) {
		this.r = r;
	}

	public RetResult<Integer> readRpc() {
		int rpcType = 0;
		error err = null;
		try {
			rpcType = r.read();
		} catch (IOException e) {
			err = error.Errorf(e.getMessage());
		}
		return new RetResult<>(rpcType, err);
	}

	public error Decode(error rpcError) {
		logger.field("rpcError", rpcError).debug("Decode()");

		RetResult<String> readString = read();
	    String s = readString.result;
	    error err = readString.err;

	    if (err != null) {
	    	return err;
	    }

	    error parsedErr = JsonUtils.StringToObject(s, error.class);
	    logger.field("parsedErr", parsedErr).debug("Decode()");
	    rpcError.setErrMessage(parsedErr.Error());
		return null;
	}

	public <T extends ParsableMessage> error Decode(T resp) {
		logger.field("resp", resp).debug("Decode()");

		RetResult<String> readString = read();
	    String s = readString.result;
	    error err = readString.err;
	    logger.field("s", s).debug("Decode()");
	    resp.parseFrom(s);

	    new Exception().getStackTrace();

	    logger.field("s", s).field("resp", resp).debug("Decode()");

	    if (err != null) {
	    	return err;
	    }

		err = resp.parseFrom(s);
		return err;
	}

	private RetResult<String> read() {
		Writer writer = new StringWriter();
		error err = null;
		int nrBytes;
	    try {
			while ((nrBytes = r.read(buf)) != -1) {
				writer.write(buf, 0, nrBytes);
			}
		} catch (IOException e) {
			err = error.Errorf(e.getMessage());
		} finally {
			try {
			    writer.flush();
				writer.close();
			} catch (IOException e) {
				err = error.Errorf(e.getMessage());
			}
        }

	    String s = writer.toString();
	    return new RetResult<>(s, err);
	}
}
