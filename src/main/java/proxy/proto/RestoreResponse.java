package proxy.proto;

import common.error;

/**
 * RestoreResponse captures both an error.
 */
public class RestoreResponse {
	public byte[] StateHash;
	public error Error;

	public RestoreResponse(byte[] stateHash, error error) {
		super();
		StateHash = stateHash;
		Error = error;
	}
}