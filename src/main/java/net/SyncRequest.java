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
}
