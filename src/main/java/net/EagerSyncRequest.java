package net;

import java.util.Arrays;

import autils.JsonUtils;
import common.error;
import poset.WireEvent;

public class EagerSyncRequest implements ParsableMessage {
	long FromID;
	poset.WireEvent[] Events;


	public EagerSyncRequest(long fromID, WireEvent[] events) {
		super();
		FromID = fromID;
		Events = events;
	}

	public EagerSyncRequest() {
		FromID = -1;
		Events = null;
	}

	public long getFromID() {
		return FromID;
	}

	public void setFromID(long fromID) {
		FromID = fromID;
	}

	public poset.WireEvent[] getEvents() {
		return Events;
	}
	public void setEvents(poset.WireEvent[] events) {
		Events = events;
	}

	@Override
	public error parseFrom(String s) {
		error err = null;
		try {
			EagerSyncRequest o = JsonUtils.StringToObject(s, EagerSyncRequest.class);
			this.FromID = o.FromID;
			this.Events = o.Events;
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
		EagerSyncRequest other = (EagerSyncRequest) obj;
		if (!Arrays.equals(Events, other.Events))
			return false;
		if (FromID != other.FromID)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EagerSyncRequest [FromID=").append(FromID).append(", Events=").append(Arrays.toString(Events))
				.append("]");
		return builder.toString();
	}
}
