package net;

import poset.WireEvent;

public class EagerSyncRequest {
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
}
