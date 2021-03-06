package net;

import java.util.Arrays;
import java.util.Map;

import autils.JsonUtils;
import common.error;
import poset.WireEvent;

public class SyncResponse implements ParsableMessage {
	long FromID;
	private boolean SyncLimit;
	private poset.WireEvent[] Events;
	private Map<Long,Long> Known;

	public SyncResponse() {
		FromID = -1;
		SyncLimit = false;
		Events = null;
		Known = null;
	}

	public SyncResponse(long fromID, boolean syncLimit, WireEvent[] events, Map<Long, Long> known) {
		FromID = fromID;
		SyncLimit = syncLimit;
		Events = events;
		Known = known;
	}

	public SyncResponse(long id) {
		FromID = id;
		SyncLimit = false;
		Events = null;
		Known = null;
	}

	public SyncResponse(long id, WireEvent[] events, Map<Long, Long> known) {
		FromID = id;
		SyncLimit = false;
		Events = events;
		Known = known;
	}

	public SyncResponse(long id, boolean syncLimit) {
		FromID = id;
		SyncLimit = syncLimit;
		Events = null;
		Known = null;
	}

	public void copy(SyncResponse r) {
		if (r != null) {
			FromID = r.FromID;
			SyncLimit = r.SyncLimit;
			Events = r.Events;
			Known = r.Known;
		}
	}

	public boolean isSyncLimit() {
		return SyncLimit;
	}

	public void setSyncLimit(boolean syncLimit) {
		SyncLimit = syncLimit;
	}

	public poset.WireEvent[] getEvents() {
		return Events;
	}

	public void setEvents(poset.WireEvent[] events) {
		Events = events;
	}

	public Map<Long,Long> getKnown() {
		return Known;
	}

	public void setKnown(Map<Long,Long> known) {
		Known = known;
	}

	public long getFromID() {
		return FromID;
	}

	@Override
	public error parseFrom(String s) {
		error err = null;
		try {
			SyncResponse o = JsonUtils.StringToObject(s, SyncResponse.class);
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
		result = prime * result + Arrays.hashCode(Events);
		result = prime * result + (int) (FromID ^ (FromID >>> 32));
		result = prime * result + ((Known == null) ? 0 : Known.hashCode());
		result = prime * result + (SyncLimit ? 1231 : 1237);
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
		SyncResponse other = (SyncResponse) obj;
		if (!Arrays.equals(Events, other.Events))
			return false;
		if (FromID != other.FromID)
			return false;
		if (Known == null) {
			if (other.Known != null)
				return false;
		} else if (!Known.equals(other.Known))
			return false;
		if (SyncLimit != other.SyncLimit)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SyncResponse [FromID=").append(FromID).append(", SyncLimit=").append(SyncLimit)
				.append(", Events=").append(Arrays.toString(Events)).append(", Known=").append(Known).append("]");
		return builder.toString();
	}
}
