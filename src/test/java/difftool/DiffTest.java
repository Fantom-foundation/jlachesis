package difftool;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Level;
import org.junit.Test;

import autils.Logger;
import node.Node;
import node.NodeList;
import node.NodeList.Stoppable;

/**
 * Test of diff
 *
 * @author qn
 */
public class DiffTest {

	@Test
	public void testPem() {
		Logger logger = Logger.getLogger(this.getClass());
		logger.setLevel(Level.FATAL);

		NodeList nodes = new NodeList(3, logger);

		Stoppable stop = nodes.StartRandTxStream();
		nodes.WaitForBlock(5);
		stop.stop();

		Node[] nodeArray = nodes.Values();

		Diff diff = Diff.compare(nodeArray[0],  nodeArray[1]);
		assertTrue("diff = " + diff + " should not be empty", diff.IsEmpty());
	}

}