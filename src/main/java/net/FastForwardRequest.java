package net;

import autils.JsonUtils;
import common.error;

public class FastForwardRequest implements ParsableMessage {
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

	@Override
	public error parseFrom(String s) {
		error err = null;
		try {
			FastForwardRequest o = JsonUtils.StringToObject(s, FastForwardRequest.class);
			this.FromID = o.FromID;
		} catch (Exception e) {
			err = error.Errorf(e.getMessage());
		}
		return err;
	}
}
