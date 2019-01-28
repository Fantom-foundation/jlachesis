package proxy;

import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jcsp.lang.Alternative;
import org.jcsp.lang.Channel;
import org.jcsp.lang.Guard;
import org.jcsp.lang.One2OneChannel;

import com.google.protobuf.ByteString;

import channel.ChannelUtils;
import channel.ExecService;
import common.RetResult;
import common.UuidUtils;
import common.error;
import proxy.internal.LachesisNodeClient;
import proxy.internal.LachesisNode_ConnectClient;
import proxy.proto.Commit;
import proxy.proto.CommitResponse;
import proxy.proto.RestoreRequest;
import proxy.proto.RestoreResponse;
import proxy.proto.SnapshotRequest;
import proxy.proto.SnapshotResponse;
import proxy.proto.ToClient;
import proxy.proto.ToClient.Block;
import proxy.proto.ToClient.Query;
import proxy.proto.ToClient.Restore;
import proxy.proto.ToServer;
import proxy.proto.ToServer.Answer;

public class GrpcLachesisProxy implements proxy.LachesisProxy {

//    ZeroTime = time.Date(0, time.January, 0, 0, 0, 0, 0, time.Local)
	static final Instant ZeroTime;
	static {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.MONTH, Calendar.JANUARY);
		calendar.set(Calendar.YEAR, 0);
		ZeroTime = calendar.toInstant();
	}

	error ErrNeedReconnect = error.Errorf("try to reconnect");
	error ErrConnShutdown = error.Errorf("client disconnected");

	Logger logger;
	One2OneChannel<proxy.proto.Commit> commitCh; // chan proto.Commit
	One2OneChannel<proxy.proto.SnapshotRequest> queryCh; // chan proto.SnapshotRequest
	One2OneChannel<proxy.proto.RestoreRequest> restoreCh; // chan proto.RestoreRequest

	Duration reconn_timeout;
	String addr;
	One2OneChannel<Object> shutdown; // chan struct{}
	One2OneChannel<Instant> reconnect_ticket; // chan time.Time
//	ServerSocket conn; // grpc.ClientConn conn;
	LachesisNode_ConnectClient client;
	AtomicReference<LachesisNode_ConnectClient> stream;

	/**
	 * Constructor instantiates a LachesisProxy to connect to remote node
	 *
	 * @param addr
	 * @param logger
	 */
	public GrpcLachesisProxy(String addr, Logger logger) {
		if (logger == null) {
			logger = Logger.getLogger(GrpcLachesisProxy.class);
			logger.setLevel(Level.DEBUG);
		}

		logger.info("connecting address =" + addr);

		this.reconn_timeout = Duration.ofSeconds(2); // 2 * time.Second;
		this.addr = addr;
		this.shutdown = Channel.one2one(); // make(chan struct{}),
		this.reconnect_ticket = Channel.one2one(); // make(chan time.Time, 1),
		this.logger = logger;
		this.commitCh = Channel.one2one(); // make(chan proto.Commit),
		this.queryCh = Channel.one2one(); // make(chan proto.SnapshotRequest),
		this.restoreCh = Channel.one2one(); // make(chan proto.RestoreRequest),

//		this.conn = grpc.Dial(this.addr,
//			grpc.WithInsecure(),
//			grpc.WithBackoffMaxDelay(this.reconn_timeout));

//		try {
//			this.conn = new ServerSocket(0, 50, InetAddress.getByName(addr));
//			this.conn.setSoTimeout((int) this.reconn_timeout.toMillis());
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

//		this.client = new LachesisNodeClient(this.conn);
		if (addr.contains(":")) {
			String ip = addr.split(":")[0];
			int port = Integer.parseInt(addr.split(":")[1]);
			this.client = new LachesisNodeClient(ip, port);
		}
		else {
			this.client = new LachesisNodeClient(addr, 0);
		}
		this.reconnect_ticket.out().write(Instant.now()); // <- time.Now();

		ExecService.go(() -> listen_events());
	}

	public error Close() {
		ChannelUtils.close(shutdown);
		return null;
	}

	// CommitCh implements LachesisProxy interface method
	public One2OneChannel<proxy.proto.Commit> CommitCh() {
		return commitCh;
	}

	// SnapshotRequestCh implements LachesisProxy interface method
	public One2OneChannel<proxy.proto.SnapshotRequest> SnapshotRequestCh() {
		return queryCh;
	}

	// RestoreCh implements LachesisProxy interface method
	public One2OneChannel<proxy.proto.RestoreRequest> RestoreCh() {
		return restoreCh;
	}

	// SubmitTx implements LachesisProxy interface method
	public error SubmitTx(byte[] tx) {
//		r := new proxy.internal.ToServer(
//			Event: &internal.ToServer_Tx_{
//				Tx: &internal.ToServer_Tx{
//					Data: tx,
//				},
//			},
//		);

		proxy.proto.ToServer.Tx tx1 = proxy.proto.ToServer.Tx.newBuilder().setData(ByteString.copyFrom(tx)).build();
		proxy.proto.ToServer r = proxy.proto.ToServer.newBuilder().setTx(tx1).build();

		error err = sendToServer(r);
		return err;
	}

	/*
	 * network:
	 */

	public error sendToServer(ToServer data) {
		while (true) {
			error err = streamSend(data);
			if (err == null) {
				return err;
			}
			logger.warn(String.format("send to server err: %s", err));

			err = reConnect();
			if (err == ErrConnShutdown) {
				return err;
			}
		}
	}

	public RetResult<ToClient> recvFromServer() {
		while (true) {
			RetResult<ToClient> streamRecv = streamRecv();
			ToClient data = streamRecv.result;
			error err = streamRecv.err;
			if (err == null) {
				return new RetResult<ToClient>(null, err);
			}
			logger.warn(String.format("recv from server err: %s", err));

			err = reConnect();
			if (err == ErrConnShutdown) {
				return new RetResult<ToClient>(null, err);
			}
		}
	}

	public error reConnect() {
		Instant disconn_time = Instant.now();
		Instant connect_time = reconnect_ticket.in().read();

		if (connect_time.equals(ZeroTime)) {
			reconnect_ticket.out().write(ZeroTime);
			return ErrConnShutdown;
		}

		if (disconn_time.isBefore(connect_time)) {
			reconnect_ticket.out().write(connect_time);
			return null;
		}

//		select {
//		case <-shutdown:
//			closeStream();
//			conn.Close();
//			close(commitCh);
//			close(queryCh);
//			close(restoreCh);
//			reconnect_ticket.out().write(ZeroTime);
//			return ErrConnShutdown;
//		default:
//			// see code below
//		}

		final Alternative alt = new Alternative(new Guard[] { shutdown.in() });
		final int SHUTDOWN = 0;

		switch (alt.priSelect()) {
		case SHUTDOWN:
			shutdown.in().read();
			closeStream();
//			conn.close();
			ChannelUtils.close(commitCh);
			ChannelUtils.close(queryCh);
			ChannelUtils.close(restoreCh);
			reconnect_ticket.out().write(ZeroTime);
			return ErrConnShutdown;
		default:
			// see code below
		}


		// TODO
//		stream = client.Connect(context.TODO(), grpc.MaxCallRecvMsgSize(Integer.MAX_VALUE),
//				grpc.MaxCallSendMsgSize(Integer.MAX_VALUE));
		RetResult<LachesisNode_ConnectClient> connect = client.Connect();
		LachesisNode_ConnectClient stream = connect.result;
		error err = connect.err;
		if (err != null) {
			logger.warn(String.format("rpc Connect() err: %s", err));
			reconnect_ticket.out().write(connect_time); // <- connect_time
			return err;
		}

		logger.debug("rpc Connect() done");

		setStream(stream);

		reconnect_ticket.out().write(Instant.now()); // <- time.Now()
		return err;
	}

	public void listen_events() {
		ToClient event;
		error err;
		UUID uuid;
		while (true) {
			RetResult<ToClient> recvFromServer = recvFromServer();
			event = recvFromServer.result;
			err = recvFromServer.err;
			if (err != null) {
				logger.debug(String.format("recv err: %s", err));

//				if (err == io.EOF) {
//					logger.debug(String.format("recv EOF: %s", err));
//				}
				break;
			}
			// block commit event
			Block b = event.getBlock();
			if (b != null) {
				poset.Block pb = new poset.Block();
				RetResult<poset.Block> protoUnmarshal = pb.ProtoUnmarshal(b.getData().toByteArray());
				pb = protoUnmarshal.result;
				err = protoUnmarshal.err;

				if (err != null) {
					continue;
				}
				uuid = UuidUtils.asUuid(b.getUid().toByteArray());
				if (err == null) {
					commitCh.out().write(new Commit(pb, newCommitResponseCh(uuid)));
				}
				continue;
			}
			// get snapshot query
			Query q = event.getQuery();
			if (q != null) {
				uuid = UuidUtils.asUuid(q.getUid().toByteArray());
				if (err == null) {
					queryCh.out().write(new SnapshotRequest(q.getIndex(), newSnapshotResponseCh(uuid)));
				}
				continue;
			}
			// restore event
			Restore r = event.getRestore();
			if (r != null) {
				uuid = UuidUtils.asUuid(r.getUid().toByteArray());
				if (err == null) {
					restoreCh.out().write(new RestoreRequest(r.getData().toByteArray(), newRestoreResponseCh(uuid)));
				}
				continue;
			}
		}
	}

	/*
	 * staff:
	 */

	public One2OneChannel<proxy.proto.CommitResponse> newCommitResponseCh(UUID uuid) {
		One2OneChannel<proxy.proto.CommitResponse> respCh = Channel.one2one();
		ExecService.go(() -> {
			ToServer answer = null;
			CommitResponse resp = respCh.in().read();
			boolean ok = resp != null;
			if (ok) {
				ToServer.Answer.newBuilder().setUid(UuidUtils.asByteString(uuid))
						.setData(ByteString.copyFrom(resp.StateHash)).setError(resp.Error.Error());
//				answer = new ToServer(uuid[:], resp.StateHash, resp.Error);
			}
			sendToServer(answer);
		});
		return respCh;
	}

	public One2OneChannel<proxy.proto.SnapshotResponse> newSnapshotResponseCh(UUID uuid) {
		One2OneChannel<proxy.proto.SnapshotResponse> respCh = Channel.one2one();
		ExecService.go(() -> {
			ToServer answer = null;
			SnapshotResponse resp = respCh.in().read();
			boolean ok = resp != null;
			if (ok) {
				answer = newAnswer(UuidUtils.asBytes(uuid), resp.Snapshot, resp.Error);
			}
			sendToServer(answer);
		});
		return respCh;
	}

	public One2OneChannel<proxy.proto.RestoreResponse> newRestoreResponseCh(UUID uuid) {
		One2OneChannel<proxy.proto.RestoreResponse> respCh = Channel.one2one();
		ExecService.go(() -> {
			ToServer answer = null;
			RestoreResponse resp = respCh.in().read();
			boolean ok = resp != null;
//			resp, ok := <-respCh;
			if (ok) {
				answer = newAnswer(UuidUtils.asBytes(uuid), resp.StateHash, resp.Error);
			}
			sendToServer(answer);
		});
		return respCh;
	}

	public ToServer newAnswer(byte[] uuid, byte[] data, error err) {
		if (err != null) {
//			return new ToServer (
//				Event: &internal.ToServer_Answer_{
//					Answer: &internal.ToServer_Answer{
//						Uid: uuid,
//						Payload: &internal.ToServer_Answer_Error{
//							Error: err.Error(),
//						},
//					},
//				},
//			);

			Answer answer = ToServer.Answer.newBuilder().setUid(ByteString.copyFrom(uuid)).setError(err.Error())
					.build();
			ToServer server = ToServer.newBuilder().setAnswer(answer).build();
			return server;
		}

//		return &internal.ToServer{
//			Event: &internal.ToServer_Answer_{
//				Answer: &internal.ToServer_Answer{
//					Uid: uuid,
//					Payload: &internal.ToServer_Answer_Data{
//						Data: data,
//					},
//				},
//			},
//		}

		Answer answer = ToServer.Answer.newBuilder().setUid(ByteString.copyFrom(uuid))
				.setData(ByteString.copyFrom(data)).build();
		ToServer server = ToServer.newBuilder().setAnswer(answer).build();

		return server;
	}

	public error streamSend(ToServer data) {
		Object v = stream.get(); // stream.Load();
		if (v == null) {
			return ErrNeedReconnect;
		}

		boolean ok = v instanceof LachesisNode_ConnectClient;
		if (ok) {
			stream.set((LachesisNode_ConnectClient) v);
		}
		if (!ok || stream == null) {
			return ErrNeedReconnect;
		}
		return stream.get().Send(data);
	}

	public RetResult<ToClient> streamRecv() {
		LachesisNode_ConnectClient v = stream.get();// stream.Load();
		if (v == null) {
			return new RetResult<ToClient>(null, ErrNeedReconnect);
		}

		boolean ok = (v instanceof LachesisNode_ConnectClient);
		if (ok) {
			stream.set(v);
		}
		if (!ok || stream == null) {
			return new RetResult<ToClient>(null, ErrNeedReconnect);
		}
		return stream.get().Recv();
	}

	public void setStream(LachesisNode_ConnectClient stream) {
		this.stream.set(stream); // .Store(stream);
	}

	public void closeStream() {
		Object v = stream.get(); // .Load();
		if (v != null) {
			boolean ok = v instanceof LachesisNode_ConnectClient;
			if (ok) {
				stream.set((LachesisNode_ConnectClient) v);
			}
			if (ok && stream != null) {
				stream.get().CloseSend();
			}
		}
	}
}