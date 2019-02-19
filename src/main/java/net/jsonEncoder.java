package net;

import java.io.IOException;
import java.io.Writer;

import autils.JsonUtils;
import autils.Logger;
import common.error;

public class jsonEncoder {
	private static Logger logger = Logger.getLogger(jsonEncoder.class);

	Writer w;

	public jsonEncoder(Writer w) {
		this.w = w;
	}

	public error Encode(error respErr) {
		logger.field("respErr", respErr).debug("Encode()");
		try {
			w.write(JsonUtils.ObjectToString(respErr));
		} catch (IOException e) {
			return error.Errorf(e.getMessage());
		}
		return null;
	}

	public error Encode(ParsableMessage o) {
		logger.field("o", o).debug("Encode()");

		try {
			String s = o.getString();
			logger.field("s", s).debug("encoded result");
			w.write(s);
		} catch (IOException e) {
			e.printStackTrace();

			return error.Errorf("Encode error=" + e.getMessage());
		}
		return null;
	}
}
