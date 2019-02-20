package proxy.internal;

import common.RResult;
import common.error;
import proxy.proto.ToClient;
import proxy.proto.ToServer;

public interface LachesisNode_ConnectServer {
	error Send(ToClient tc);

	RResult<ToServer> Recv();

	error Connect(LachesisNode_ConnectServer cs);
}