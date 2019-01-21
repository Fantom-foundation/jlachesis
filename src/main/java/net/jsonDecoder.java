package net;

import java.io.Reader;

import autils.JsonUtils;
import common.error;

public class jsonDecoder {

	public jsonDecoder(Reader r) {
		// TODO Auto-generated constructor stub
	}

	public error Decode(String rpcError) {
		// TODO Auto-generated method stub
		error err = null;
		return err;
	}

	public <T> error Decode(T resp) {
		JsonUtils.StringToObject(resp.toString(), resp.getClass());
		// TODO Auto-generated method stub
		error err = null;
		return err;
	}

}
