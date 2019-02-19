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
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RPCResponse [Response=").append(Response).append(", Error=").append(Error).append("]");
		return builder.toString();
	}
}