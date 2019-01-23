package proxy.internal;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import common.RetResult;
import common.error;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import proxy.proto.LachesisNodeGrpc;
import proxy.proto.LachesisNodeGrpc.LachesisNodeStub;
import proxy.proto.ToClient;
import proxy.proto.ToServer;

/**
 * A LachesisNodeClient
 */
public class LachesisNodeClient implements LachesisNode_ConnectClient {

	private static final Logger logger = Logger.getLogger(LachesisNodeClient.class.getName());

	private ManagedChannel channel;
	private LachesisNodeStub nodeStub;

	String host;
	int port;

	/**
	 * Constructor Client connecting to HelloServer server at {@code host:port}.
	 */
	public LachesisNodeClient(String host, int port) {
		this.host = host;
		this.port = port;
		this.channel = ManagedChannelBuilder.forAddress(host, port).maxInboundMessageSize(Integer.MAX_VALUE)
				.maxInboundMetadataSize(Integer.MAX_VALUE)
				.usePlaintext().build();
		this.nodeStub = LachesisNodeGrpc.newStub(channel);
	}

	public void shutdown() throws InterruptedException {
		channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
	}

	public error Send(ToServer msg) {
		// TODO Auto-generated method stub

		StreamObserver<ToServer> collect = nodeStub.connect(new StreamObserver<ToClient>() {
			@Override
			public void onNext(ToClient m) {
				System.out.println("ToClient m: " + m.toString());

			}

			@Override
			public void onError(Throwable t) {

			}

			@Override
			public void onCompleted() {

			}
		});

//		    Stream.of(1L, 2L, 3L, 4L).map(l -> ToServer.newBuilder().build())
//		        .forEach(collect::onNext);
		collect.onCompleted();

		return null;
	}

	public RetResult<ToClient> Recv() {

		StreamObserver<ToServer> collect = nodeStub.connect(new StreamObserver<ToClient>() {
			@Override
			public void onNext(ToClient m) {
				logger.debug("ToClient m: " + m.toString());

			}

			@Override
			public void onError(Throwable t) {
				logger.debug("onError t: " + t);
			}

			@Override
			public void onCompleted() {
				logger.debug("onCompleted() ");
			}
		});

//		    Stream.of(1L, 2L, 3L, 4L).map(l -> StreamingExample.Metric.newBuilder().setMetric(l).build())
//		        .forEach(collect::onNext);
		collect.onCompleted();

		return null;
	}

	public RetResult<LachesisNode_ConnectClient> Connect() {
		if (this.channel != null) {
			this.channel.shutdownNow();
			this.channel = null;
			this.nodeStub = null;
		}

		this.channel = ManagedChannelBuilder.forAddress(this.host, port)
				.maxInboundMessageSize(Integer.MAX_VALUE)
				.maxInboundMetadataSize(Integer.MAX_VALUE)
				.usePlaintext().build();
		this.nodeStub = LachesisNodeGrpc.newStub(channel);

//		err := cc.NewStream(ctx, &_LachesisNode_serviceDesc.Streams[0], "/internal.LachesisNode/Connect", opts...);
		return new RetResult<>(this, null);
	}

	@Override
	public void CloseSend() {
		// TODO Auto-generated method stub
		logger.debug("CloseSend() TBD");
	}
}
