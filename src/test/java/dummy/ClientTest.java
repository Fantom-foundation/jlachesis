package dummy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.time.Duration;

import org.jcsp.lang.CSTimer;
import org.junit.Test;

import autils.Logger;
import autils.time;
import channel.ExecService;
import channel.Selectors;
import common.RetResult;
import common.TestUtils;
import common.error;
import crypto.hash;
import poset.Block;
import proxy.GrpcAppProxy;
import proxy.GrpcLachesisProxy;

/**
 * Test of Client
 *
 * @author qn
 *
 */
public class ClientTest {

	@Test
	public void TestSocketProxyServer() {
		long timeout = 2 * time.Second;
		String errTimeout = "time is over";
		String addr = "127.0.0.1:9990";

		Logger logger = null; //Logger.getLogger(getClass());

		byte[] txOrigin = "the test transaction".getBytes();

		// Server
		GrpcAppProxy app = new GrpcAppProxy(addr, Duration.ofSeconds(2), logger);

		//  listens for a request
		ExecService.go(() -> {
			new Selectors<byte[]>(app.SubmitCh()) {
				public void onEvent() {
					byte[] tx = ch.in().read();
					assertEquals("transactions should match", txOrigin, tx);
				}
				public void onTimeOut() {
					tim.setAlarm(tim.read() + timeout);
					fail(errTimeout);
				}
			}.run();
		});

		// Client part connecting to RPC service and calling methods
		GrpcLachesisProxy lachesisProxy = new GrpcLachesisProxy(addr, logger);
		DummyClient node = new DummyClient(logger, null, lachesisProxy);
		error err = node.SubmitTx(txOrigin);
		assertNull("No error when submit transactions", err);
	}

	//@Test
	public void TestDummySocketClient() {
		long timeout    = 2 * time.Second;
		String	addr       = "127.0.0.1:9992";

		Logger logger = TestUtils.NewTestLogger(this.getClass());

		// server
		GrpcAppProxy appProxy = new GrpcAppProxy(addr, Duration.ofSeconds(2), logger);

		// client
		GrpcLachesisProxy lachesisProxy = new GrpcLachesisProxy(addr, logger);

		State state = new State(logger);

		DummyClient dummy = new DummyClient(logger, state, lachesisProxy);

		byte[] initialStateHash = state.stateHash;
		//create a few blocks
		Block[] blocks = new Block[5];
		for (int i = 0; i < 5; i++) {
			blocks[i] = new Block(i, i+1, new byte[]{}, new byte[][]{String.format("block %d transaction", i).getBytes()});
		}

		CSTimer tim = new CSTimer();
		tim.setAlarm(tim.read() + timeout / 4);

		//commit first block and check that the client's statehash is correct
		RetResult<byte[]> commitBlock = appProxy.CommitBlock(blocks[0]);
		byte[] stateHash = commitBlock.result;
		error err = commitBlock.err;
		assertNull("No error when commit block", err);

		byte[] expectedStateHash = initialStateHash;

		for (byte[] t : blocks[0].Transactions()) {
			byte[] tHash = hash.SHA256(t);
			expectedStateHash = hash.SimpleHashFromTwoHashes(expectedStateHash, tHash);
		}

		assertEquals("state hash should match", expectedStateHash, stateHash);
		RetResult<byte[]> getSnapshot = appProxy.GetSnapshot(blocks[0].Index());
		byte[] snapshot = getSnapshot.result;
		err = getSnapshot.err;
		assertNull("No error when getSnapshot", err);
		assertEquals("snapshot should match", expectedStateHash, snapshot);

		//commit a few more blocks, then attempt to restore back to block 0 state
		for (int i = 1; i < 5; i++) {
			RetResult<byte[]> commitBlock2 = appProxy.CommitBlock(blocks[i]);
			err = commitBlock2.err;
			assertNull("No error when CommitBlock", err);
		}

		err = appProxy.Restore(snapshot);
		assertNull("No error when Restore", err);

		lachesisProxy.Close();
		appProxy.Close();
	}
}