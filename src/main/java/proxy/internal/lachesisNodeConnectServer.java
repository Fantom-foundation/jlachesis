//package proxy.internal;
//
//import java.io.InputStream;
//
//import common.RetResult;
//import common.error;
//import io.grpc.Attributes;
//import io.grpc.Compressor;
//import io.grpc.Decompressor;
//import io.grpc.Metadata;
//import io.grpc.Status;
//import io.grpc.internal.AbstractServerStream;
//import io.grpc.internal.ServerStream;
//import io.grpc.internal.ServerStreamListener;
//import io.grpc.internal.StatsTraceContext;
//import proxy.internal.Grpc.ToClient;
//
//public class lachesisNodeConnectServer extends AbstractServerStream implements LachesisNode_ConnectServer {
//
//	public error Send(ToClient m) {
//		return ServerStream.SendMsg(m);
//	}
//
//	public RetResult<Grpc.ToServer> Recv() {
//		m = new Grpc.ToServer.;
//		err := InProcessServerStream.RecvMsg(m);
//		if (err != null) {
//			return null, err;
//		}
//		return m, null;
//	}
//
//	var _LachesisNode_serviceDesc = grpc.ServiceDesc {
//		ServiceName: "internal.LachesisNode",
//		HandlerType: (*LachesisNodeServer)(null),
//		Methods:     []grpc.MethodDesc{},
//		Streams: []grpc.StreamDesc{
//			{
//				StreamName:    "Connect",
//				Handler:       _LachesisNode_Connect_Handler,
//				ServerStreams: true,
//				ClientStreams: true,
//			},
//		},
//		Metadata: "grpc.proto",
//	}
//}