package proxy;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import autils.Appender;
import autils.Logger;
import autils.time;
import channel.ExecService;
import channel.Selectors;
import common.RResult;
import common.TestUtils;
import common.error;
import poset.Block;

/**
 * Test for InmemApp
 * @author qn
 *
 */
public class InmemTest {
	@Test
	public void TestInmemAppCalls() {
		long timeout = 1 * time.Second;
		String errTimeout = "time is over";

		TestProxy proxy = TestProxy.NewTestProxy();

		byte[][] transactions = new byte[][]{
			"tx 1".getBytes(),
			"tx 2".getBytes(),
			"tx 3".getBytes(),
		};

		Block block = new Block(0, 1, new byte[]{}, transactions);

		//"#1 Send tx"
		byte[] tx_origin = "the test transaction".getBytes();
		ExecService.go(() -> {
			new Selectors<byte[]>(proxy.SubmitCh()) {
				public void onEvent() {
					byte[] tx = ch.in().read();
					// System.out.println("transation read = " + new String(tx));
					assertArrayEquals("read result submitCh should match", tx_origin, tx);
				}
				public void onTimeOut() {
					tim.setAlarm(tim.read() + timeout);
					fail(errTimeout);
				}
			}.run();
		});

		proxy.SubmitTx(tx_origin);


		//"#2 Commit block"
		RResult<byte[]> commitBlock = proxy.CommitBlock(block);
		byte[] stateHash = commitBlock.result;
		error err = commitBlock.err;

		assertNull("No error when commit block", err);
		assertArrayEquals("state hash should match", goldStateHash(), stateHash);
		assertArrayEquals("transactions should match", transactions, proxy.transactions);

		//"#3 Get snapshot"
		RResult<byte[]> getSnapshot = proxy.GetSnapshot(block.Index());
		byte[] snapshot = getSnapshot.result;
		err = getSnapshot.err;

		assertNull("No error when get snapshot", err);
		assertArrayEquals("snapshot should match", goldSnapshot(), snapshot);

		//"#4 Restore snapshot"
		err = proxy.Restore(goldSnapshot());
		assertNull("No error restore from snapshot", err);
	}

	static class TestProxy  extends InmemAppProxy implements ProxyHandler {
		byte[][] transactions;

		public TestProxy(Logger logger) {
			super(logger);
		}

		public static TestProxy NewTestProxy() {
			TestProxy p = new TestProxy(TestUtils.NewTestLogger(InmemTest.class));
			p.transactions = new byte[][]{};
			p.setHandler(p);
			return p;
		}

		public RResult<byte[]> CommitHandler(Block block) {
			logger.debug("CommitBlock");
			transactions = Appender.append(transactions, block.transactions());
			return new RResult<>(goldStateHash(), null);
		}

		public RResult<byte[]> SnapshotHandler(long blockIndex) {
			logger.debug("GetSnapshot");
			return new RResult<>(goldSnapshot(), null);
		}

		public RResult<byte[]> RestoreHandler(byte[] snapshot) {
			logger.debug("RestoreSnapshot");
			return new RResult<>(goldStateHash(), null);
		}
	}

	private static byte[] goldStateHash() {
		return "statehash".getBytes();
	}

	private static byte[] goldSnapshot() {
		return "snapshot".getBytes();
	}
}