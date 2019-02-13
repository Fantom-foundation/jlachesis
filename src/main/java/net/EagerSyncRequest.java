package net;

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
}
