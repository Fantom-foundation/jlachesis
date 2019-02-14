package net;

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

	public void copy(SyncResponse r) {
		FromID = r.FromID;
		SyncLimit = r.SyncLimit;
		Events = r.Events;
		Known = r.Known;
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
}
