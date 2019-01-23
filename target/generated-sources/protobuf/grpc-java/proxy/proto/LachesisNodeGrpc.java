package proxy.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.18.0)",
    comments = "Source: proxy/grpc.proto")
public final class LachesisNodeGrpc {

  private LachesisNodeGrpc() {}

  public static final String SERVICE_NAME = "proxy.proto.LachesisNode";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<proxy.proto.ToServer,
      proxy.proto.ToClient> getConnectMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Connect",
      requestType = proxy.proto.ToServer.class,
      responseType = proxy.proto.ToClient.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<proxy.proto.ToServer,
      proxy.proto.ToClient> getConnectMethod() {
    io.grpc.MethodDescriptor<proxy.proto.ToServer, proxy.proto.ToClient> getConnectMethod;
    if ((getConnectMethod = LachesisNodeGrpc.getConnectMethod) == null) {
      synchronized (LachesisNodeGrpc.class) {
        if ((getConnectMethod = LachesisNodeGrpc.getConnectMethod) == null) {
          LachesisNodeGrpc.getConnectMethod = getConnectMethod = 
              io.grpc.MethodDescriptor.<proxy.proto.ToServer, proxy.proto.ToClient>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "proxy.proto.LachesisNode", "Connect"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proxy.proto.ToServer.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proxy.proto.ToClient.getDefaultInstance()))
                  .setSchemaDescriptor(new LachesisNodeMethodDescriptorSupplier("Connect"))
                  .build();
          }
        }
     }
     return getConnectMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static LachesisNodeStub newStub(io.grpc.Channel channel) {
    return new LachesisNodeStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static LachesisNodeBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new LachesisNodeBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static LachesisNodeFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new LachesisNodeFutureStub(channel);
  }

  /**
   */
  public static abstract class LachesisNodeImplBase implements io.grpc.BindableService {

    /**
     */
    public io.grpc.stub.StreamObserver<proxy.proto.ToServer> connect(
        io.grpc.stub.StreamObserver<proxy.proto.ToClient> responseObserver) {
      return asyncUnimplementedStreamingCall(getConnectMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getConnectMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                proxy.proto.ToServer,
                proxy.proto.ToClient>(
                  this, METHODID_CONNECT)))
          .build();
    }
  }

  /**
   */
  public static final class LachesisNodeStub extends io.grpc.stub.AbstractStub<LachesisNodeStub> {
    private LachesisNodeStub(io.grpc.Channel channel) {
      super(channel);
    }

    private LachesisNodeStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LachesisNodeStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new LachesisNodeStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<proxy.proto.ToServer> connect(
        io.grpc.stub.StreamObserver<proxy.proto.ToClient> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getConnectMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   */
  public static final class LachesisNodeBlockingStub extends io.grpc.stub.AbstractStub<LachesisNodeBlockingStub> {
    private LachesisNodeBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private LachesisNodeBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LachesisNodeBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new LachesisNodeBlockingStub(channel, callOptions);
    }
  }

  /**
   */
  public static final class LachesisNodeFutureStub extends io.grpc.stub.AbstractStub<LachesisNodeFutureStub> {
    private LachesisNodeFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private LachesisNodeFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LachesisNodeFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new LachesisNodeFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_CONNECT = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final LachesisNodeImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(LachesisNodeImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CONNECT:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.connect(
              (io.grpc.stub.StreamObserver<proxy.proto.ToClient>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class LachesisNodeBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    LachesisNodeBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return proxy.proto.Grpc.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("LachesisNode");
    }
  }

  private static final class LachesisNodeFileDescriptorSupplier
      extends LachesisNodeBaseDescriptorSupplier {
    LachesisNodeFileDescriptorSupplier() {}
  }

  private static final class LachesisNodeMethodDescriptorSupplier
      extends LachesisNodeBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    LachesisNodeMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (LachesisNodeGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new LachesisNodeFileDescriptorSupplier())
              .addMethod(getConnectMethod())
              .build();
        }
      }
    }
    return result;
  }
}
