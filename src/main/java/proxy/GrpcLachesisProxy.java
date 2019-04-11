package proxy;

import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Level;
import org.jcsp.lang.Alternative;
import org.jcsp.lang.Channel;
import org.jcsp.lang.Guard;
import org.jcsp.lang.One2OneChannel;

import com.google.protobuf.ByteString;

import autils.Logger;
import channel.ChannelUtils;
import channel.ExecService;
import common.RResult;
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
import proxy.proto.ToServer.Answer.Builder;

public class GrpcLachesisProxy implements proxy.LachesisProxy {

	// ZeroTime = time.Date(0, time.January, 0, 0, 0, 0, 0, time.Local)
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
	One2OneChannel<proxy.proto.Commit> commitCh;
	One2OneChannel<proxy.proto.SnapshotRequest> queryCh;
	One2OneChannel<proxy.proto.RestoreRequest> restoreCh;

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
		this.stream = new AtomicReference<LachesisNode_ConnectClient>();

//		this.conn = grpc.Dial(this.addr,
//			grpc.WithInsecure(),
//			grpc.WithBackoffMaxDelay(this.reconn_timeout));

//		try {
//			this.conn = new ServerSocket(0, 50, InetAddress.getByName(addr));
//			this.conn.setSoTimeout((int) this.reconn_timeout.toMillis());
//		} catch (UnknownHostException | IOException e) {
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

		stream.set(this.client);
		logger.field("addr", addr).field("stream", stream).field("stream1", stream.get()).debug("after creating client");

		ExecService.go(() -> {
			this.logger.info("reconnect_ticket " + Instant.now());
			this.reconnect_ticket.out().write(Instant.now()); // <- time.Now();
			this.logger.info("after reconnect_ticket " + Instant.now());
		});

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
		logger.debug("SubmitTx: " + new String(tx));

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
			logger.warnf("send to server err: %s", err);

			err = reConnect();
			if (err == ErrConnShutdown) {
				return err;
			}
		}
	}

	public RResult<ToClient> recvFromServer() {
		while (true) {
			RResult<ToClient> streamRecv = streamRecv();
			ToClient data = streamRecv.result;
			error err = streamRecv.err;
			if (err == null) {
				return new RResult<ToClient>(data, err);
			}
			logger.warnf("recv from server err: %s", err);

			err = reConnect();
			if (err == ErrConnShutdown) {
				return new RResult<ToClient>(data, err);
			}
		}
	}

	public error reConnect() {
		Instant disconn_time = Instant.now();

		logger.debug("reConnect() disconn_time =" + disconn_time);
		Instant connect_time = reconnect_ticket.in().read();
		logger.debug("reConnect() connect_time =" + connect_time);

		if (ZeroTime.equals(connect_time)) {
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
		logger
			.field("stream", this.stream)
			.field("client = ", this.client)
			.debug("reConnect() setting stream again");
		this.stream = new AtomicReference<LachesisNode_ConnectClient>();

		RResult<LachesisNode_ConnectClient> connect = client.Connect();
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
			RResult<ToClient> recvFromServer = recvFromServer();
			event = recvFromServer.result;
			err = recvFromServer.err;
			if (err != null) {
				logger.debugf("recv err: %s", err);

//				if (err == io.EOF) {
//					logger.debug(String.format("recv EOF: %s", err));
//				}
				break;
			}
			// block commit event
			Block b = event.getBlock();
			if (b != null) {
				poset.Block pb = new poset.Block();
				err = pb.marshaller().protoUnmarshal(b.getData().toByteArray());
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

	public One2OneChannel<proxy.proto.CommitResponse> newCommitResponseCh(UUID uuid) {
		One2OneChannel<proxy.proto.CommitResponse> respCh = Channel.one2one();
		ExecService.go(() -> {
			ToServer answer = null;
			CommitResponse resp = respCh.in().read();
			boolean ok = resp != null;
			if (ok) {
				//answer = new ToServer(uuid[:], resp.StateHash, resp.Error);
				Answer pAnswer = ToServer.Answer.newBuilder().setUid(UuidUtils.asByteString(uuid))
						.setData(ByteString.copyFrom(resp.StateHash)).setError(resp.Error.Error()).build();
				answer = ToServer.newBuilder().setAnswer(pAnswer).build();
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
			if (resp != null) {
				answer = newAnswer(UuidUtils.asBytes(uuid), resp.StateHash, resp.Error);
			}
			sendToServer(answer);
		});
		return respCh;
	}

	public ToServer newAnswer(byte[] uuid, byte[] data, error err) {
		Builder ansBuilder = ToServer.Answer.newBuilder().setUid(ByteString.copyFrom(uuid));
		if (err != null) {
			ansBuilder.setError(err.Error()).build();
		} else {
			ansBuilder.setData(ByteString.copyFrom(data)).build();
		}
		ToServer server = ToServer.newBuilder().setAnswer(ansBuilder.build()).build();
		return server;
	}

	public error streamSend(ToServer data) {
		LachesisNode_ConnectClient v = stream.get();
		if (v == null) {
			return ErrNeedReconnect;
		}
		return v.Send(data);
	}

	public RResult<ToClient> streamRecv() {
		logger.field("stream", stream).debug("streamRecv()");
		LachesisNode_ConnectClient v = stream.get();
		if (v == null) {
			return new RResult<ToClient>(null, ErrNeedReconnect);
		}
		return v.Recv();
	}

	public void setStream(LachesisNode_ConnectClient client) {
		stream.set(client); // .Store(stream);
	}

	public void closeStream() {
		LachesisNode_ConnectClient v = stream.get(); // .Load();
		if (v != null) {
			stream.get().CloseSend();
		}
	}
}