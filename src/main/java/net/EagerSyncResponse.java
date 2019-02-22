package net;

import autils.JsonUtils;
import common.error;

public class EagerSyncResponse implements ParsableMessage {
	long FromID;
	boolean Success;

	public EagerSyncResponse() {
		FromID = -1;
		Success = false;
	}

	public EagerSyncResponse(long fromID, boolean success) {
		FromID = fromID;
		Success = success;
	}

	public void copy (EagerSyncResponse r) {
		FromID = r.FromID;
		Success = r.Success;
	}

	public long getFromID() {
		return FromID;
	}
	public void setFromID(long fromID) {
		FromID = fromID;
	}
	public boolean isSuccess() {
		return Success;
	}
	public void setSuccess(boolean success) {
		Success = success;
	}

	@Override
	public error parseFrom(String s) {
		error err = null;
		try {
			EagerSyncResponse o = JsonUtils.StringToObject(s, EagerSyncResponse.class);
			copy(o);
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
		result = prime * result + (Success ? 1231 : 1237);
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
		EagerSyncResponse other = (EagerSyncResponse) obj;
		if (FromID != other.FromID)
			return false;
		if (Success != other.Success)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EagerSyncResponse [FromID=").append(FromID).append(", Success=").append(Success).append("]");
		return builder.toString();
	}
}
