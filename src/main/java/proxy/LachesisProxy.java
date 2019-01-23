package proxy;

import org.jcsp.lang.One2OneChannel;

import common.error;

/**
 * LachesisProxy provides an interface for the application to submit
 * transactions to the lachesis node.
 */
public interface LachesisProxy {
	One2OneChannel<proxy.proto.Commit> CommitCh(); // chan proto.Commit;

	One2OneChannel<proxy.proto.SnapshotRequest> SnapshotRequestCh(); // chan proto.SnapshotRequest;

	One2OneChannel<proxy.proto.RestoreRequest> RestoreCh(); // chan proto.RestoreRequest;

	error SubmitTx(byte[] tx);
}