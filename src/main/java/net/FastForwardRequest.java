package net;

public class FastForwardRequest {
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
}
