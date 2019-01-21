package proxy.proto;

import org.jcsp.lang.One2OneChannel;

import common.error;

// RestoreRequest provides a response mechanism.
public class RestoreRequest {
	byte[] Snapshot;
	One2OneChannel<RestoreResponse> RespChan; // chan<- RestoreResponse

	public RestoreRequest(byte[] snapshot, One2OneChannel<RestoreResponse> respChan) {
		super();
		Snapshot = snapshot;
		RespChan = respChan;
	}

	// Respond is used to respond with a response, error or both
	public void Respond(byte[] snapshot, error err) {
		RespChan.out().write(new RestoreResponse(snapshot, err));
	}

	public One2OneChannel<RestoreResponse> getRespChan() {
		return RespChan;
	}

	public byte[] getSnapshot() {
		return Snapshot;
	}
}