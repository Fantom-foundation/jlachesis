package proxy.internal;

import common.RetResult;
import common.error;
import io.grpc.internal.ServerStream;
import proxy.proto.Grpc.ToClient;
import proxy.proto.Grpc.ToServer;

public interface LachesisNode_ConnectServer extends ServerStream {
	error Send(ToClient tc);
	RetResult<ToServer> Recv();
}