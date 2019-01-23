package proxy.internal;

import common.RetResult;
import common.error;
import proxy.proto.ToClient;
import proxy.proto.ToServer;

public interface LachesisNode_ConnectServer {
	error Send(ToClient tc);

	RetResult<ToServer> Recv();

	error Connect(LachesisNode_ConnectServer cs);
}