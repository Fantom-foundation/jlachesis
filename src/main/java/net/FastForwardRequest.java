package net;

import autils.JsonUtils;
import common.error;

public class FastForwardRequest implements ParsableMessage {
	long FromID;

	public FastForwardRequest() {
		super();
		FromID = -1;
	}

	public FastForwardRequest(long fromID) {
		super();
		FromID = fromID;
	}

	public long getFromID() {
		return FromID;
	}

	public void setFromID(long fromID) {
		FromID = fromID;
	}

	@Override
	public error parseFrom(String s) {
		error err = null;
		try {
			FastForwardRequest o = JsonUtils.StringToObject(s, FastForwardRequest.class);
			this.FromID = o.FromID;
		} catch (Exception e) {
			err = error.Errorf(e.getMessage());
		}
		return err;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (FromID ^ (FromID >>> 32));
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
		FastForwardRequest other = (FastForwardRequest) obj;
		if (FromID != other.FromID)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FastForwardRequest [FromID=").append(FromID).append("]");
		return builder.toString();
	}
}
