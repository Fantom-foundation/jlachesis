package proxy.proto;

import org.jcsp.lang.One2OneChannel;

import common.error;

// Commit provides a response mechanism.
public class Commit {
	poset.Block Block;
	One2OneChannel<CommitResponse> RespChan; // chan<- CommitResponse;

	public Commit(poset.Block block, One2OneChannel<CommitResponse> respChan) {
		super();
		Block = block;
		RespChan = respChan;
	}

	// Respond is used to respond with a response, error or both
	public void Respond(byte[] stateHash, error err) {
		RespChan.out().write(new CommitResponse(stateHash, err));
	}

	public poset.Block getBlock() {
		return Block;
	}

	public One2OneChannel<CommitResponse> getRespChan() {
		return RespChan;
	}
}