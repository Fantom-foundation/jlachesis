package proxy;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.time.Duration;

import org.jcsp.lang.CSTimer;
import org.jcsp.lang.One2OneChannel;

import autils.Logger;
import autils.time;
import channel.ExecService;
import channel.Selectors;
import common.NetUtils;
import common.RetResult;
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

	//@Test
	public void TestGrpcCalls() {
		String addr = NetUtils.GetUnusedNetAddr();
		Logger logger = Logger.getLogger(this.getClass());

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
		}.run();

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

		RetResult<byte[]> commitBlock = s.CommitBlock(block);
		byte[] answ = commitBlock.result;
		err = commitBlock.err;
		assertNull("No error", err);
		assertArrayEquals("Result from commit should match", gold, answ);

		//"#3 Receive snapshot query"
		long index = 1L;
		ExecService.go(() -> {
			new TimedOutEventSelectors<SnapshotRequest>(c.SnapshotRequestCh()) {
				public void onEvent() {
					SnapshotRequest event = ch.in().read();
					logger.debug("onEvent() event =" +  event);
					assertEquals("read transactions should match", index, event.getBlockIndex());
					event.getRespChan().out().write( new SnapshotResponse (gold, null));
				}
			}.run();
		});

		RetResult<byte[]> getSnapshot = s.GetSnapshot(index);
		answ = getSnapshot.result;
		err = getSnapshot.err;
		assertNull("No error", err);
		assertEquals("Snapshot result should match", gold, answ);

		// "#4 Receive restore command"
		ExecService.go(() -> {
			new TimedOutEventSelectors<RestoreRequest>(c.RestoreCh()) {
				public void onEvent() {
					RestoreRequest event = ch.in().read();
					assertEquals("Restore result should match", gold, event.getSnapshot());
					event.getRespChan().out().write( new RestoreResponse (gold, null));
				}
			}.run();
		});

		err = s.Restore(gold);
		assertNull("No error", err);

		err = c.Close();
		assertNull("No error", err);

		err = s.Close();
		assertNull("No error", err);

	}

	static class TimedOutEventSelectors<T> extends Selectors<T> {
		public TimedOutEventSelectors(One2OneChannel<T> ch) {
			super(ch);
		}

		public void onTimeOut() {
			tim.setAlarm(tim.read() + timeout * 1000);
			fail(errTimeout);
		}
	}

	//@Test
	public void TestGrpcReConnection() {
		String addr = NetUtils.GetUnusedNetAddr();
		Logger logger = common.TestUtils.NewTestLogger(this.getClass());

		GrpcAppProxy s = new GrpcAppProxy(addr, Duration.ofSeconds(timeout), logger) ;
		GrpcLachesisProxy c = new GrpcLachesisProxy(addr, logger);

		// "#1 Send tx after connection"
		runTest(s, c);

		s = new GrpcAppProxy(addr, Duration.ofMillis(timeout/2 * 1000), logger);
//		<-time.After(timeout);
		final CSTimer tim = new CSTimer();
		tim.setAlarm(tim.read() + timeout * 1000);

		//"#2 Send tx after reconnection")
		runTest(s, c);
	}

	private void runTest(GrpcAppProxy s, GrpcLachesisProxy c) {
		byte[] gold = "123456".getBytes();

		error err = c.SubmitTx(gold);
		assertNull("No error", err);

		new TimedOutEventSelectors<byte[]>(s.SubmitCh()) {
			public void onEvent() {
				byte[] tx = ch.in().read();
				assertEquals("read transactions should match", gold, tx);
			}
		}.run();

		err = s.Close();
		assertNull("No error", err);
	}

	/*
	public void TestGrpcMaxMsgSize() {
		const (
			largeSize  = 100 * 1024 * 1024
			timeout    = 3 * time.Minute
			errTimeout = "time is over"

		)
		addr := utils.GetUnusedNetAddr(t);
		logger := common.NewTestLogger(t)

		s, err := NewGrpcAppProxy(addr, timeout, logger)
		assertNull("No error", err);

		c, err := NewGrpcLachesisProxy(addr, logger)
		assertNull("No error", err);

		largeData := make([]byte, largeSize)
		_, err = rand.Read(largeData)
		assertNull("No error", err);

		t.Run("#1 Send large tx", public void() {
			assert := assert.New(t)

			err = c.SubmitTx(largeData)
			assert.NoError(err)

			select {
			case tx := <-s.SubmitCh():
				assert.Equal(largeData, tx)
			case <-time.After(timeout):
				assert.Fail(errTimeout)
			}
		})

		t.Run("#2 Receive large block", public void() {
			assert := assert.New(t)
			block := poset.Block{
				Body: poset.BlockBody{
					Transactions: [][]byte{
						largeData,
					},
				},
			}
			hash := largeData[:largeSize/10]

			go public void() {
				select {
				case event := <-c.CommitCh():
					assert.EqualValues(block, event.Block)
					event.RespChan <- proto.CommitResponse{
						StateHash: hash,
						Error:     null,
					}
				case <-time.After(timeout):
					assert.Fail(errTimeout)
				}
			}()

			answ, err := s.CommitBlock(block)
			if assert.NoError(err) {
				assert.Equal(hash, answ)
			}
		})

		err = c.Close()
		assertNull("No error", err);

		err = s.Close()
		assertNull("No error", err);
	}
	 */
}