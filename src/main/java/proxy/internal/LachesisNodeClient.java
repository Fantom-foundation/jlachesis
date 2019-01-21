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
import proxy.proto.Grpc.ToClient;
import proxy.proto.Grpc.ToServer;

public class LachesisNodeClient implements LachesisNode_ConnectClient {
	ServerSocket cc;

	public LachesisNodeClient(ServerSocket cc)  {
		this.cc = cc;
	}

	public void cancel(Status arg0) {
		// TODO Auto-generated method stub

	}

	public Attributes getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	public void halfClose() {
		// TODO Auto-generated method stub

	}

	public void setAuthority(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setDeadline(Deadline arg0) {
		// TODO Auto-generated method stub

	}

	public void setDecompressorRegistry(DecompressorRegistry arg0) {
		// TODO Auto-generated method stub

	}

	public void setFullStreamDecompression(boolean arg0) {
		// TODO Auto-generated method stub

	}

	public void setMaxInboundMessageSize(int arg0) {
		// TODO Auto-generated method stub

	}

	public void setMaxOutboundMessageSize(int arg0) {
		// TODO Auto-generated method stub

	}

	public void start(ClientStreamListener arg0) {
		// TODO Auto-generated method stub

	}

	public void flush() {
		// TODO Auto-generated method stub

	}

	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	public void request(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCompressor(Compressor arg0) {
		// TODO Auto-generated method stub

	}

	public void setMessageCompression(boolean arg0) {
		// TODO Auto-generated method stub

	}

	public void writeMessage(InputStream arg0) {
		// TODO Auto-generated method stub

	}

	public error Send(ToServer s) {
		// TODO Auto-generated method stub
		return null;
	}

	public RetResult<ToClient> Recv() {
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
