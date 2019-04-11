package proxy;

import org.jcsp.lang.One2OneChannel;

import common.error;

/**
 * LachesisProxy provides an interface for the application to submit
 * transactions to the lachesis node.
 */
public interface LachesisProxy {
	One2OneChannel<proxy.proto.Commit> CommitCh();

	One2OneChannel<proxy.proto.SnapshotRequest> SnapshotRequestCh();

	One2OneChannel<proxy.proto.RestoreRequest> RestoreCh();

	error SubmitTx(byte[] tx);
}