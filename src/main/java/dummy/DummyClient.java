package dummy;

import org.jcsp.lang.Alternative;
import org.jcsp.lang.Guard;

import autils.Logger;
import channel.ExecService;
import common.RResult;
import common.error;
import dummy.DummyClient;
import proxy.GrpcLachesisProxy;
import proxy.LachesisProxy;
import proxy.ProxyHandler;
import proxy.proto.Commit;
import proxy.proto.RestoreRequest;
import proxy.proto.SnapshotRequest;

/**
 * DummyClient is a implementation of the dummy app. Lachesis and the
 * app run in separate processes and communicate through proxy
 */
public class DummyClient {
	Logger logger;
	proxy.ProxyHandler state;
	proxy.LachesisProxy lachesisProxy;

	/**
	 * Construct a dummy client
	 * @param logger
	 * @param state
	 * @param lachesisProxy
	 */
	public DummyClient(Logger logger, ProxyHandler state, LachesisProxy lachesisProxy) {
		super();
		this.logger = logger;
		this.state = state;
		this.lachesisProxy = lachesisProxy;
	}


	/**
	 * SubmitTx sends a transaction to node via proxy
	 * @param tx
	 * @return
	 */
	public error SubmitTx(byte[] tx) {
		return lachesisProxy.SubmitTx(tx);
	}


	public static proxy.AppProxy NewInmemDummyApp(Logger logger)  {
		State state = new State(logger);
		return new proxy.InmemAppProxy(state, logger);
	}

	public static DummyClient NewDummySocketClient(String addr, Logger logger) {
		GrpcLachesisProxy lachesisProxy = new GrpcLachesisProxy(addr, logger);

		return new DummyClient(logger, null, lachesisProxy);
	}

	/**
	 * NewDummyClient instantiates an implementation of the dummy app
	 * @param lachesisProxy
	 * @param handler
	 * @param logger
	 * @return
	 */
	public RResult<DummyClient> NewDummyClient(proxy.LachesisProxy lachesisProxy , proxy.ProxyHandler handler , Logger logger) {
		DummyClient c = new DummyClient( logger, handler, lachesisProxy);
		if (handler == null) {
			return new RResult<DummyClient>(c, null);
		}

		error err1 = null;

		ExecService.go(() -> {
			while (true) {

				// TBD : check conversion
//				select {
//				case b, ok := <-lachesisProxy.CommitCh():
//					if !ok {
//						return;
//					}
//					logger.Debugf("block commit event: %v", b.Block);
//					hash, err := handler.CommitHandler(b.Block)
//					b.Respond(hash, err)
//
//				case r, ok := <-lachesisProxy.RestoreCh():
//					if !ok {
//						return;
//					}
//					logger.Debugf("snapshot restore command: %v", r.Snapshot);
//					hash, err := handler.RestoreHandler(r.Snapshot)
//					r.Respond(hash, err);
//
//				case s, ok := <-lachesisProxy.SnapshotRequestCh():
//					if !ok {
//						return;
//					}
//					logger.Debugf("get snapshot query: %v", s.BlockIndex);
//					hash, err := handler.SnapshotHandler(s.BlockIndex);
//					s.Respond(hash, err);
//				}

				final Alternative alt = new Alternative (new Guard[] {lachesisProxy.CommitCh().in(), lachesisProxy.RestoreCh().in(), lachesisProxy.SnapshotRequestCh().in()});
				final int COMMIT = 0, RESTORE = 1, SNAPSHOT = 2;

				byte[] hash;
				error err = null;
				switch (alt.priSelect ()) {
					case COMMIT:
						Commit b = lachesisProxy.CommitCh().in().read();
						if (b == null) {
							return;
						}
						logger.debug(String.format("block commit event: %s", b.getBlock()));
						RResult<byte[]> commitHandler = handler.CommitHandler(b.getBlock());
						err = commitHandler.err;
						hash = commitHandler.result;
						b.Respond(hash, err);
						break;
					case RESTORE:
						RestoreRequest r = lachesisProxy.RestoreCh().in().read();
						if (r == null) {
							return;
						}
						logger.debug(String.format("snapshot restore command: %s", r.getSnapshot()));
						RResult<byte[]> restoreHandler = handler.RestoreHandler(r.getSnapshot());
						hash = restoreHandler.result;
						err = restoreHandler.err;
						r.Respond(hash, err);
						break;
					case SNAPSHOT:
						SnapshotRequest s = lachesisProxy.SnapshotRequestCh().in().read();
						if (s == null) {
							return;
						}
						logger.debug(String.format("get snapshot query: %d", s.getBlockIndex()));
						RResult<byte[]> snapshotHandler = handler.SnapshotHandler(s.getBlockIndex());
						hash = snapshotHandler.result;
						err = snapshotHandler.err;
						s.Respond(hash, err);
						break;
				}

				if (err != null) {
					err1.setErrMessage(err.Error());
				}
			}
		});

		return new RResult<DummyClient>(c, err1);
	}
}
