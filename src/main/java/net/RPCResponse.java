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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Error == null) ? 0 : Error.hashCode());
		result = prime * result + ((Response == null) ? 0 : Response.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RPCResponse other = (RPCResponse) obj;
		if (Error == null) {
			if (other.Error != null)
				return false;
		} else if (!Error.equals(other.Error))
			return false;
		if (Response == null) {
			if (other.Response != null)
				return false;
		} else if (!Response.equals(other.Response))
			return false;
		return true;
	}



	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RPCResponse [Response=").append(Response).append(", Error=").append(Error).append("]");
		return builder.toString();
	}
}