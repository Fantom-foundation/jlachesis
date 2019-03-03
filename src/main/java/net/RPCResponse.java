package net;

import autils.JsonUtils;
import common.error;

/**
 * RPCResponse captures both a response and a potential error.
 */
public class RPCResponse {
	ParsableMessage response;
	error error;
	public RPCResponse(ParsableMessage response, error error) {
		super();
		this.response = response;
		this.error = error;
	}

	public error parseFrom(String s) {
		error err = null;
		try {
			RPCResponse o = JsonUtils.StringToObject(s, RPCResponse.class);
			this.response = o.response;
			this.error = o.error;
		} catch (Exception e) {
			err = error.Errorf(e.getMessage());
		}
		return err;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((error == null) ? 0 : error.hashCode());
		result = prime * result + ((response == null) ? 0 : response.hashCode());
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
		if (error == null) {
			if (other.error != null)
				return false;
		} else if (!error.equals(other.error))
			return false;
		if (response == null) {
			if (other.response != null)
				return false;
		} else if (!response.equals(other.response))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RPCResponse [Response=").append(response).append(", Error=").append(error).append("]");
		return builder.toString();
	}
}