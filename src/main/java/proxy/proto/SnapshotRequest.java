package proxy.proto;

import org.jcsp.lang.One2OneChannel;

import common.error;

// SnapshotRequest provides a response mechanism.
public class SnapshotRequest {
	long BlockIndex;
	One2OneChannel<SnapshotResponse> RespChan; //   chan<- SnapshotResponse;

	public SnapshotRequest(long blockIndex, One2OneChannel<SnapshotResponse> respChan) {
		super();
		BlockIndex = blockIndex;
		RespChan = respChan;
	}


	// Respond is used to respond with a response, error or both
	public void Respond(byte[] snapshot, error err) {
		RespChan.out().write(new SnapshotResponse(snapshot, err));
	}


	public long getBlockIndex() {
		return BlockIndex;
	}
}