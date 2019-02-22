package net;

import java.io.IOException;
import java.io.Writer;

import autils.JsonUtils;
import autils.Logger;
import common.error;

public class JsonEncoder {
	//private static Logger logger = Logger.getLogger(jsonEncoder.class);

	Writer w;

	public JsonEncoder(Writer w) {
		this.w = w;
	}

	public error encode(error respErr) {
		//logger.field("respErr", respErr).debug("Encode(err) starts");
		try {
			w.write(JsonUtils.ObjectToString(respErr));
			w.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return error.Errorf("Encode(err) error=" + e.getMessage());
		}
		return null;
	}

	public error encode(ParsableMessage o) {
		//logger.field("o", o).debug("Encode(o) starts");
		try {
			String s = o.getString();
			//logger.field("s", s).debug("Encode(o) encoded result");
			w.write(s);
			w.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return error.Errorf("Encode(o) error=" + e.getMessage());
		}
		return null;
	}
}
