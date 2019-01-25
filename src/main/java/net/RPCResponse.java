package net;

import common.error;

/**
 * RPCResponse captures both a response and a potential error.
 */
public class RPCResponse {
	Object Response;
	error Error;
	public RPCResponse(Object response, error error) {
		super();
		Response = response;
		Error = error;
	}
}