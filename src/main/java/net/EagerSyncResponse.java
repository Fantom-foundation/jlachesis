package net;

public class EagerSyncResponse {
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
}
