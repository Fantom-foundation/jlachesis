package dummy;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Level;
import org.junit.Test;

import autils.Logger;
import common.TestLoggerAdapter;
import common.TestUtils;
import node.Node;
import node.NodeList;
import node.NodeList.Stoppable;
import poset.Block;
import proxy.ProxyHandler;

/**
 * Test of diff
 *
 * @author qn
 *
 */
public class StateTest {

	@Test
	public void testProxyHandlerImplementation() {
		Logger logger = TestUtils.NewTestLogger(this.getClass());

		State state = new State(logger);

		assertTrue ("State implements ProxyHandler interface",state instanceof ProxyHandler);
	}

	@Test
	public void testCommit() {
		Logger logger = TestUtils.NewTestLogger(this.getClass());

		State state = new State(logger);
		Block block = new Block(0, 1,
				"framehash".getBytes(),
				new byte[][]{
					"abc".getBytes(),
					"def".getBytes(),
					"ghi".getBytes(),
				});
		state.commit(block);

		assertArrayEquals("State's commited txs", state.committedTxs, block.transactions());
		assertTrue("state snapshots contains block's index", state.snapshots.containsKey(block.Index()));
	}
}