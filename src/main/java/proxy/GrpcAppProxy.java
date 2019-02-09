package proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.log4j.Level;
import org.jcsp.lang.CSTimer;
import org.jcsp.lang.Channel;
import org.jcsp.lang.One2OneChannel;

import com.google.protobuf.ByteString;

import autils.Appender;
import autils.Logger;
import channel.ChannelUtils;
import channel.ExecService;
import common.NetUtils;
import common.RetResult;
import common.UuidUtils;
import common.error;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import proxy.internal.LachesisNodeServer;
import proxy.internal.LachesisNode_ConnectServer;
import proxy.proto.LachesisNodeGrpc;
import proxy.proto.ToClient;
import proxy.proto.ToServer;
import proxy.proto.ToServer.Answer;
import proxy.proto.ToServer.Tx;

/**
 * GrpcAppProxy implements the AppProxy interface
 */
public class GrpcAppProxy implements AppProxy, LachesisNodeServer {

	static final error ErrNoAnswers = error.Errorf("no answers");

	Logger logger;
	ServerSocket listener; // net.Listener
	io.grpc.Server server;

	Duration timeout;

	// typedef ClientStream LachesisNode_ConnectServer
	One2OneChannel<LachesisNode_ConnectServer> new_clients; // chan ClientStream;
	Map<UUID, One2OneChannel<ToServer.Answer>> askings; // map[UUID]chan *internal.ToServer.Answer;
	ReadWriteLock askings_sync;

	One2OneChannel<byte[]> event4server; // chan []byte;
	One2OneChannel<ToClient> event4clients; // chan *internal.ToClient;

	/**
	 * Constructor instantiates a joined AppProxy-interface listen to remote apps
	 *
	 * @param bind_addr
	 * @param timeout
	 * @param logger
	 */
	public GrpcAppProxy(String bind_addr, Duration timeout, Logger logger) {
		if (logger == null) {
			logger = Logger.getLogger(GrpcAppProxy.class);
			logger.setLevel(Level.DEBUG);
		}

		this.logger = logger;
		this.timeout = timeout;
		this.new_clients = Channel.one2one(); // make(chan ClientStream, 100);
		// TODO: make chans buffered?
		this.askings = new HashMap<UUID, One2OneChannel<ToServer.Answer>>(); // make(map[UUID]chan
																				// *internal.ToServer.Answer),
		this.event4server = Channel.one2one(); // make(chan []byte),
		this.event4clients = Channel.one2one(); // make(chan *internal.ToClient),

//		p.listener, err = net.Listen("tcp", bind_addr);
		int parsePort = NetUtils.parsePort(bind_addr);
		try {
//			this.listener = NetUtils.bind(bind_addr).result;
//			this.listener = new ServerSocket(0, 50, InetAddress.getByName(bind_addr));

			// TODO
//			server = new io.grpc.Server(grpc.MaxRecvMsgSize(Integer.MAX_VALUE),grpc.MaxSendMsgSize(Integer.MAX_VALUE));
			this.server = ServerBuilder.forPort(parsePort)
					.addService(new LachesisNodeGrpcImpl())
					.maxInboundMessageSize(Integer.MAX_VALUE)
					.maxInboundMetadataSize(Integer.MAX_VALUE)
					.build().start();
			logger.info("grpc server started");
		} catch (IOException e1) {
			logger.error("Error starting grpc server at port " + parsePort + " , err " + e1.getMessage());
			e1.printStackTrace();
		}

		// TODO: dont need these anymore. Already handled?
//		RegisterLachesisNodeServer(server, this);
//		ExecService.go(() -> server.Serve(listener));

		ExecService.go(() -> send_events4clients());
	}

	static class LachesisNodeGrpcImpl extends LachesisNodeGrpc.LachesisNodeImplBase {
		final Logger logger = Logger.getLogger(LachesisNodeGrpcImpl.class);

		public io.grpc.stub.StreamObserver<proxy.proto.ToServer> connect(
				io.grpc.stub.StreamObserver<proxy.proto.ToClient> responseObserver) {

			return new StreamObserver<proxy.proto.ToServer>() {
				ToServer m;

				@Override
				public void onNext(proxy.proto.ToServer value) {
					logger.debug("onNext value: " + value);
					m = value;
				}

				@Override
				public void onError(Throwable t) {
					logger.debug("onError t: " + t);
					responseObserver.onError(t);
				}

				@Override
				public void onCompleted() {
					logger.debug("onCompleted");

					// TODO put actual value into ToClient builder
					responseObserver.onNext(ToClient.newBuilder().setRestore(ToClient.Restore.newBuilder().build())
							.setQuery(ToClient.Query.newBuilder().build()).setBlock(ToClient.Block.newBuilder().build())
							.build());
				}
			};
		}
	}

	public error Close() {
		server.shutdown();
		logger.debug("server is shutting down");

		try {
			listener.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ChannelUtils.close(event4server);
		ChannelUtils.close(event4clients);
		return null;
	}

	/**
	 * network interface:
	 */

	/**
	 * Connect implements gRPC-server interface: LachesisNodeServer
	 * @param stream
	 * @return
	 */
	public error Connect(LachesisNode_ConnectServer stream) {
		// save client's stream for writing
		this.new_clients.out().write(stream); // <- stream;
		logger.debug("client connected");
		// read from stream
		while (true) {
			RetResult<ToServer> recv = stream.Recv();
			ToServer req = recv.result;
			error err = recv.err;
			if (err != null) {
//				if (err != io.EOF) {
				logger.debugf("client received error: %s", err);
//				} else {
//					logger.debug("client disconnected well");
//				}
				return err;
			}
			Tx tx = req.getTx();
			if (tx != null) {
				event4server.out().write(tx.getData().toByteArray()); // <- tx.getData();
				continue;
			}
			Answer answer = req.getAnswer();
			if (answer != null) {
				route_answer(answer);
				continue;
			}
		}
	}

	public void send_events4clients() {

		logger.debug("send_events4clients()");

		error err;
		LachesisNode_ConnectServer[] connected = null;
		LachesisNode_ConnectServer[] alive = null;
		LachesisNode_ConnectServer stream;

		event4clients.in().startRead();

		while (true) {
			ToClient event = event4clients.in().read();
			if (event == null) {
				break;
			}

			// TODO
			while (true) {
				stream = new_clients.in().read();
				if (stream == null) {
					break;
				}
				connected = Appender.append(connected, stream);
			}

			for (LachesisNode_ConnectServer stre : connected) {
				err = stre.Send(event);
				if (err == null) {
					alive = Appender.append(alive, stre);
				}
			}
			connected = alive;
			alive = null;

			// read another one
			event = event4clients.in().read();
		}
	}

	/*
	 * inmem interface: AppProxy implementation
	 */

	// SubmitCh implements AppProxy interface method
	public One2OneChannel<byte[]> SubmitCh() {
		return event4server;
	}

	// SubmitCh implements AppProxy interface method
	// TODO: Incorrect implementation, just adding to the interface so long
	public One2OneChannel<poset.InternalTransaction> SubmitInternalCh() {
		return null;
	}

	// CommitBlock implements AppProxy interface method
	public RetResult<byte[]> CommitBlock(poset.Block block) {
		RetResult<byte[]> protoMarshal = block.marshaller().protoMarshal();
		byte[] data = protoMarshal.result;
		error err = protoMarshal.err;
		if (err != null) {
			return new RetResult<byte[]>(null, err);
		}

		One2OneChannel<Answer> answerCh = push_block(data);
		Answer answer = answerCh.in().read();
		boolean ok = answer != null;
		if (!ok) {
			return new RetResult<byte[]>(null, ErrNoAnswers);
		}
		String err_msg = answer.getError();
		if (!err_msg.isEmpty()) {
			return new RetResult<byte[]>(null, error.Errorf(err_msg));
		}
		return new RetResult<byte[]>(answer.getData().toByteArray(), null);
	}

	// GetSnapshot implements AppProxy interface method
	public RetResult<byte[]> GetSnapshot(long blockIndex) {
		One2OneChannel<Answer> push_query = push_query(blockIndex);
		Answer answer = push_query.in().read();
		boolean ok = answer != null;
		if (!ok) {
			return new RetResult<byte[]>(null, ErrNoAnswers);
		}
		String err_msg = answer.getError();
		if (!err_msg.isEmpty()) {
			return new RetResult<byte[]>(null, error.Errorf(err_msg));
		}
		return new RetResult<byte[]>(answer.getData().toByteArray(), null);
	}

	/**
	 * Restore implements AppProxy interface method
	 */
	public error Restore(byte[] snapshot) {
		One2OneChannel<Answer> push_restore = push_restore(snapshot);
		Answer answer = push_restore.in().read();
		boolean ok = answer != null;
		if (!ok) {
			return ErrNoAnswers;
		}
		String err_msg = answer.getError();
		if (err_msg != null && !err_msg.isEmpty()) {
			return error.Errorf(err_msg);
		}
		return null;
	}

	public void route_answer(ToServer.Answer hash) {
//		uuid, err := xid.FromBytes(hash.GetUid());
		UUID uuid = UUID.nameUUIDFromBytes(hash.getUid().toByteArray());
		if (uuid == null) {
			// TODO: log invalid uuid
			return;
		}
		askings_sync.readLock().lock();
		One2OneChannel<ToServer.Answer> ch = askings.get(uuid);
		boolean ok = ch != null;
		if (ok) {
			ch.out().write(hash);
		}
		askings_sync.readLock().unlock();
	}

	public One2OneChannel<ToServer.Answer> push_block(byte[] block) {
		UUID uuid = UUID.randomUUID();
		ByteString uuidBytes = ByteString.copyFrom(uuid.toString().getBytes());

//		event := &internal.ToClient{
//			Event: &internal.ToClient_Block_{
//				Block: &internal.ToClient_Block{
//					Uid:  uuid[:],
//					Data: block,
//				},
//			},
//		}

		ToClient.Block b = ToClient.Block.newBuilder().setData(ByteString.copyFrom(block)).setUid(uuidBytes).build();
		ToClient event = ToClient.newBuilder().setBlock(b).build();

		One2OneChannel<ToServer.Answer> answer = subscribe4answer(uuid);
		event4clients.out().write(event);
		return answer;
	}

	public One2OneChannel<ToServer.Answer> push_query(long index) {
		UUID uuid = UUID.randomUUID();

//		event := &internal.ToClient{
//			Event: &internal.ToClient_Query_{
//				Query: &internal.ToClient_Query{
//					Uid:   uuid[:],
//					Index: index,
//				},
//			},
//		}

		ToClient.Query query = ToClient.Query.newBuilder().setUid(UuidUtils.asByteString(uuid)).setIndex(index).build();
		ToClient event = ToClient.newBuilder().setQuery(query).build();

		One2OneChannel<ToServer.Answer> answer = subscribe4answer(uuid);
		event4clients.out().write(event);
		return answer;
	}

	public One2OneChannel<ToServer.Answer> push_restore(byte[] snapshot) {
		UUID uuid = UUID.randomUUID();

		ToClient.Restore query = ToClient.Restore.newBuilder().setUid(UuidUtils.asByteString(uuid))
				.setData(ByteString.copyFrom(snapshot)).build();
		ToClient event = ToClient.newBuilder().setRestore(query).build();

//		event := new internal.ToClient(
//			Event: &internal.ToClient_Restore_{
//				Restore: &internal.ToClient_Restore{
//					Uid:  uuid[:],
//					Data: snapshot,
//				},
//			},
//		);

		One2OneChannel<ToServer.Answer> answer = subscribe4answer(uuid);
		event4clients.out().write(event);
		return answer;
	}

	public One2OneChannel<ToServer.Answer> subscribe4answer(UUID uuid) {
		One2OneChannel<ToServer.Answer> ch = Channel.one2one(); // make(chan *internal.ToServer.Answer);
		askings_sync.writeLock().lock();
		askings.put(uuid, ch);
		askings_sync.writeLock().unlock();

		// timeout
		ExecService.go(() -> {
			CSTimer tim = new CSTimer();
			tim.after(tim.read() + timeout.toMillis());
//			<-time.After(timeout);
			askings_sync.writeLock().lock();
			askings.remove(uuid);
			askings_sync.writeLock().unlock();
			ChannelUtils.close(ch);
		});

		return ch;
	}
}
