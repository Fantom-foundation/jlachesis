package proxy;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.Duration;
import java.util.Arrays;
import java.util.Random;

import org.jcsp.lang.CSTimer;

import autils.Logger;
import autils.time;
import channel.ExecService;
import common.NetUtils;
import common.RResult;
import common.TimedOutEventSelectors;
import common.error;
import poset.Block;
import proxy.proto.Commit;
import proxy.proto.CommitResponse;
import proxy.proto.RestoreRequest;
import proxy.proto.RestoreResponse;
import proxy.proto.SnapshotRequest;
import proxy.proto.SnapshotResponse;

/**
 * Test for Grpc
 * @author qn
 *
 */
public class GrpcTest {
	static final long timeout = 1 * time.Second;
	static final String errTimeout = "time is over";
	Logger logger = Logger.getLogger(this.getClass());
	CSTimer timer;

	//@Test
	public void TestGrpcCalls() {
		String addr = NetUtils.getUnusedNetAddr();
		GrpcAppProxy s = new GrpcAppProxy(addr, Duration.ofSeconds(timeout), null) ;

		//"#1 Send tx"
		byte[] gold = "123456".getBytes();

		GrpcLachesisProxy c = new GrpcLachesisProxy(addr, null);
		error err = c.SubmitTx(gold);
		assertNull("No error", err);

		logger.debug("after SubmitTx() " +  new String(gold));

		new TimedOutEventSelectors<byte[]>(s.SubmitCh()) {
			public void onEvent() {
				byte[] tx = ch.in().read();
				logger.debug("onEvent() " +  new String(tx));
				assertEquals("read transactions should match" , gold, tx);
			}
		}.setTimeout(timeout, errTimeout).run();


		logger.debug("receive block");

		//"#2 Receive block"
		Block block = new poset.Block();

		ExecService.go( () -> {
			new TimedOutEventSelectors<Commit>(c.CommitCh()) {
				public void onEvent() {
					Commit event = ch.in().read();
					logger.debug("onEvent() event =" +  event);
					assertEquals("read transactions should match", block, event.getBlock());
					event.getRespChan().out().write( new CommitResponse (gold, null));
				}
			}.run();
		});

		logger.debug("commit block");

		RResult<byte[]> commitBlock = s.CommitBlock(block);
		byte[] answ = commitBlock.result;
		err = commitBlock.err;
		assertNull("No error", err);
		assertArrayEquals("Result from commit should match", gold, answ);

		//"#3 Receive snapshot query"
		long index = 1L;
		ExecService.go(() -> {
			new TimedOutEventSelectors<SnapshotRequest>(c.SnapshotRequestCh(), timer) {
				public void onEvent() {
					SnapshotRequest event = ch.in().read();
					logger.debug("onEvent() event =" +  event);
					assertEquals("read transactions should match", index, event.getBlockIndex());
					event.getRespChan().out().write( new SnapshotResponse (gold, null));
				}
			}.setTimeout(timeout, errTimeout).run();
		});

		RResult<byte[]> getSnapshot = s.GetSnapshot(index);
		answ = getSnapshot.result;
		err = getSnapshot.err;
		assertNull("No error", err);
		assertEquals("Snapshot result should match", gold, answ);

		// "#4 Receive restore command"
		ExecService.go(() -> {
			new TimedOutEventSelectors<RestoreRequest>(c.RestoreCh(), timer) {
				public void onEvent() {
					RestoreRequest event = ch.in().read();
					assertEquals("Restore result should match", gold, event.getSnapshot());
					event.getRespChan().out().write( new RestoreResponse (gold, null));
				}
			}.setTimeout(timeout, errTimeout).run();
		});

		err = s.Restore(gold);
		assertNull("No error", err);

		err = c.Close();
		assertNull("No error", err);

		err = s.Close();
		assertNull("No error", err);

	}

	//@Test
	public void TestGrpcReConnection() {
		String addr = NetUtils.getUnusedNetAddr();
		Logger logger = common.TestUtils.NewTestLogger(this.getClass());

		GrpcAppProxy s = new GrpcAppProxy(addr, Duration.ofSeconds(timeout), logger) ;
		GrpcLachesisProxy c = new GrpcLachesisProxy(addr, logger);

		// "#1 Send tx after connection"
		timer = new CSTimer();
		runTest(s, c);

		s = new GrpcAppProxy(addr, Duration.ofMillis(timeout/2 * 1000), logger);
		timer.setAlarm(timer.read() + timeout);

		//"#2 Send tx after reconnection")
		runTest(s, c);
	}

	private void runTest(GrpcAppProxy s, GrpcLachesisProxy c) {
		byte[] gold = "123456".getBytes();

		error err = c.SubmitTx(gold);
		assertNull("No error", err);

		new TimedOutEventSelectors<byte[]>(s.SubmitCh(), timer) {
			public void onEvent() {
				byte[] tx = ch.in().read();
				assertEquals("read transactions should match", gold, tx);
			}
		}.setTimeout(timeout, errTimeout).run();

		err = s.Close();
		assertNull("No error", err);
	}

	//@Test
	public void TestGrpcMaxMsgSize() {
		int largeSize  = 1024 * 1024;
		Duration timeout = Duration.ofMinutes(3);

		String addr = NetUtils.getUnusedNetAddr();
		logger = Logger.getLogger(getClass());

		GrpcAppProxy s = new GrpcAppProxy(addr, timeout, logger);
		GrpcLachesisProxy c = new GrpcLachesisProxy(addr, logger);

		byte[] largeData = new byte[largeSize];
		error err;
		// init random bytes
		new Random().nextBytes(largeData);

		//"#1 Send large tx"
		err = c.SubmitTx(largeData);
		assertNull("No error", err);

		new TimedOutEventSelectors<byte[]>(s.SubmitCh(), timer) {
			public void onEvent() {
				byte[] tx = ch.in().read();
				assertEquals("read transactions should match", largeData, tx);
			}
		}.setTimeout(timeout.toMillis(), errTimeout).run();

		//"#2 Receive large block"
		Block block = new Block(-1, -1, null, new byte[][] {largeData});
		byte[] hash = Arrays.copyOfRange(largeData, 0, (int) (largeSize/10));


		ExecService.go(() -> {
			new TimedOutEventSelectors<Commit>(c.CommitCh(), timer) {
				public void onEvent() {
					Commit event = ch.in().read();
					assertEquals("read transactions should match", block, event.getBlock());
					event.getRespChan().out().write(
							new CommitResponse(hash, null));
				}
			}.setTimeout(timeout.toMillis(), errTimeout).run();
		});

		RResult<byte[]> commitBlock = s.CommitBlock(block);
		byte[] answ = commitBlock.result;
		err = commitBlock.err;

		assertNull("No error", err);
		assertArrayEquals("Hash should match", hash, answ);

		err = c.Close();
		assertNull("No error", err);

		err = s.Close();
		assertNull("No error", err);
	}
}