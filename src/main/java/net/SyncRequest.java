package net;

import java.util.HashMap;
import java.util.Map;

import autils.JsonUtils;
import common.error;

public class SyncRequest implements ParsableMessage {
	long FromID;
	private Map<Long,Long> Known;

	public SyncRequest(long fromID, Map<Long, Long> known) {
		super();
		FromID = fromID;
		Known = known;
	}

	public SyncRequest() {
		FromID = -1;
		Known = new HashMap<Long,Long>();
	}

	public Map<Long,Long> getKnown() {
		return Known;
	}

	public long getFromID() {
		return FromID;
	}

	@Override
	public error parseFrom(String s) {
		error err = null;
		try {
			SyncRequest o = JsonUtils.StringToObject(s, SyncRequest.class);
			this.FromID = o.FromID;
			this.Known = o.Known;
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
		result = prime * result + ((Known == null) ? 0 : Known.hashCode());
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
		SyncRequest other = (SyncRequest) obj;
		if (FromID != other.FromID)
			return false;
		if (Known == null) {
			if (other.Known != null)
				return false;
		} else if (!Known.equals(other.Known))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SyncRequest [FromID=").append(FromID).append(", Known=").append(Known).append("]");
		return builder.toString();
	}
}
