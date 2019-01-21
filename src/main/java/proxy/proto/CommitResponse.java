package proxy.proto;

import common.error;

// CommitResponse captures both a response and a potential error.
public class CommitResponse {
	public byte[] StateHash;
	public error Error;
	public CommitResponse(byte[] stateHash, error error) {
		super();
		StateHash = stateHash;
		Error = error;
	}
}