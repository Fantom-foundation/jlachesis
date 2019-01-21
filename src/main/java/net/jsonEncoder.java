package net;

import java.io.Writer;

import autils.JsonUtils;
import common.error;

public class jsonEncoder {

	public jsonEncoder(Writer w) {
		// TODO Auto-generated constructor stub
	}

	public error Encode(Object o) {
		// TBD : error checking
		JsonUtils.ObjectToString(o);
		return null;
	}

}
