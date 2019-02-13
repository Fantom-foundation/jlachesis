package net;

import common.error;

/**
 * RPCResponse captures both a response and a potential error.
 */
public class RPCResponse {
	ParsableMessage Response;
	error Error;
	public RPCResponse(ParsableMessage response, error error) {
		super();
		Response = response;
		Error = error;
	}
}