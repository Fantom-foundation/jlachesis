package proxy.internal;

import common.RetResult;
import common.error;
import io.grpc.internal.ClientStream;
import proxy.proto.ToClient;
import proxy.proto.ToServer;

public interface LachesisNode_ConnectClient extends ClientStream {
	error Send(ToServer s);
	RetResult<ToClient> Recv();
}