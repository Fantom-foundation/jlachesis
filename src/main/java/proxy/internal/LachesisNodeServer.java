//package proxy.internal;
//
//import common.error;
//import io.grpc.Server;
//import io.grpc.internal.ServerStream;
//
//// LachesisNodeServer is the server API for LachesisNode service.
//public abstract class LachesisNodeServer  {
//	abstract error Connect(LachesisNode_ConnectServer cs);
//
//	public void RegisterLachesisNodeServer(Server s,  LachesisNodeServer srv) {
//		RegisterService(_LachesisNode_serviceDesc, srv);
//	}
//
//	public error_LachesisNode_Connect_Handler(Object srv,  ServerStream stream)  {
//		return ((LachesisNodeServer) srv).Connect( new LachesisNodeConnectServer(stream));
//	}
//}