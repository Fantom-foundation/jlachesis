package net;

import autils.JsonUtils;
import common.error;

public interface ParsableMessage {
	/**
	 * Parse the message from an input string
	 * @param s
	 */
	error parseFrom(String s);

	default public String getString() {
		return JsonUtils.ObjectToString(this);
	}
}
