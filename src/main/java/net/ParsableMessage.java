package net;

import autils.JsonUtils;
import common.error;

public interface ParsableMessage {
	/**
	 *
	 * @param s
	 */
	error parseFrom(String s);

	default public String getString() {
		return JsonUtils.ObjectToString(this);
	}
}
