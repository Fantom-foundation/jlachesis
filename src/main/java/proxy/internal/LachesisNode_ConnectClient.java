package proxy.internal;

import common.RetResult;
import common.error;
import proxy.proto.ToClient;
import proxy.proto.ToServer;

public interface LachesisNode_ConnectClient {
	error Send(ToServer s);

	RetResult<ToClient> Recv();

	RetResult<LachesisNode_ConnectClient> Connect();

	void CloseSend();
}