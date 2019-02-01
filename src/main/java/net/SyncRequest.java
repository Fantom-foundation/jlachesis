package net;

import java.util.HashMap;
import java.util.Map;

public class SyncRequest {
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
}
