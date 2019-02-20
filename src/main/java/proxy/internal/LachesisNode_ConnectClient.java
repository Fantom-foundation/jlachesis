package proxy.internal;

import common.RResult;
import common.error;
import proxy.proto.ToClient;
import proxy.proto.ToServer;

public interface LachesisNode_ConnectClient {
	error Send(ToServer s);

	RResult<ToClient> Recv();

	RResult<LachesisNode_ConnectClient> Connect();

	void CloseSend();
}