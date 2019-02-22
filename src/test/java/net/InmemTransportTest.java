package net;
import org.jcsp.lang.One2OneChannel;
import org.junit.Test;

import autils.time;

/**
 * Test for InmemTransport
 * @author qn
 *
 */
public class InmemTransportTest extends NetTransportTest {
	long timeout = 200 * time.Millisecond;


	@Test
	public void testInmemTransport() {
		// Transport 1 is consumer
		InmemTransport trans1 = new InmemTransport("");

		One2OneChannel<RPC> rpcCh = trans1.getConsumer();

		// Transport 2 makes outbound request
		InmemTransport trans2 = new InmemTransport("");

		//"Sync"
		testSync(rpcCh, trans1, trans2);

		// "Sync"
		testSync(rpcCh, trans1, trans2);

		// "EagerSync"
		testEagerSync(rpcCh, trans1, trans2);

		// "FastForward"
		testFastForward(rpcCh, trans1, trans2);

		trans2.close();
		trans1.close();
	}
}