package proxy.internal;

import java.io.InputStream;
import java.net.ServerSocket;

import common.RetResult;
import common.error;
import io.grpc.Attributes;
//import common.RetResult;
import io.grpc.ClientCall;
import io.grpc.Compressor;
import io.grpc.Deadline;
import io.grpc.DecompressorRegistry;
import io.grpc.Status;
import io.grpc.internal.ClientStreamListener;
import proxy.proto.ToClient;
import proxy.proto.ToServer;

public class LachesisNodeClient implements LachesisNode_ConnectClient {
	ServerSocket cc;

	public LachesisNodeClient(ServerSocket cc)  {
		this.cc = cc;
	}

	public error Send(ToServer s) {
		// TODO Auto-generated method stub
		return null;
	}

	public RetResult<ToClient> Recv() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public error Send(ToServer s) {
		// TODO Auto-generated method stub
		return null;
	}

//	public RetResult<LachesisNode_ConnectClient> Connect(context.Context ctx , grpc.CallOption... opts ) {
//		stream, err := cc.NewStream(ctx, &_LachesisNode_serviceDesc.Streams[0], "/internal.LachesisNode/Connect", opts...);
//		if (err != null) {
//			return null, err;
//		}
//		x := new lachesisNodeConnectClient(stream);
//		return x, null;
//	}
}
