package proxy.proto;

import common.error;

// SnapshotResponse captures both a response and a potential error.
public class SnapshotResponse {
	public byte[] Snapshot;
	public error Error;

	public SnapshotResponse(byte[] snapshot, error error) {
		super();
		Snapshot = snapshot;
		Error = error;
	}
}