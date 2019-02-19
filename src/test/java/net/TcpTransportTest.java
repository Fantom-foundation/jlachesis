package net;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Duration;

import org.junit.Test;

import common.RetResult;
import common.TestUtils;
import common.error;

/**
 * Test for TcpTransport
 * @author qn
 *
 */
public class TcpTransportTest {

	@Test
	public void TestTCPTransport_BadAddr() {
		error err = TCPTransport.NewTCPTransport("0.0.0.0:100000000", null, 1, Duration.ZERO,
				TestUtils.NewTestLogger(this.getClass())).err;
		assertEquals("err", "Port value out of range: 100000000", err.Error());

		err = TCPTransport.NewTCPTransport("0.0.0.0:-1", null, 1, Duration.ZERO,
				TestUtils.NewTestLogger(this.getClass())).err;
		assertEquals("err", "Port value out of range: -1", err.Error());

		err = TCPTransport.NewTCPTransport("0.0.0.0:44775", null, 1, Duration.ZERO,
				TestUtils.NewTestLogger(this.getClass())).err;
		assertNull("No err", err);

		err = TCPTransport.NewTCPTransport(":0", null, 1, Duration.ZERO,
				TestUtils.NewTestLogger(this.getClass())).err;
		assertNull("No err", err);

		err = TCPTransport.NewTCPTransport(":", null, 1, Duration.ZERO,
				TestUtils.NewTestLogger(this.getClass())).err;
		assertNull("No err", err);

		err = TCPTransport.NewTCPTransport(":A", null, 1, Duration.ZERO,
				TestUtils.NewTestLogger(this.getClass())).err;
		assertNotNull("Err as port is invalid", err);
	}

	@Test
	public void TestTCPTransport_WithAdvertise() throws UnknownHostException {
		int expectedPort = 12345;
		InetSocketAddress addr = new InetSocketAddress("127.0.0.1", expectedPort);
		assertEquals("Bind port should match", expectedPort, addr.getPort());

		RetResult<NetworkTransport> newTCPTransport = TCPTransport.NewTCPTransport(
				":12345", addr.getAddress(), 1, Duration.ZERO, TestUtils.NewTestLogger(this.getClass()));
		NetworkTransport trans = newTCPTransport.result;
		error err = newTCPTransport.err;

		assertNull("No error when create new tcp transport", err);
		assertEquals("Bind address should match", "127.0.0.1", trans.localAddr());

		int actualPort = ((TCPStreamLayer) trans.stream).listener.getLocalPort();
		assertEquals("Bind port should match", expectedPort, actualPort);
	}
}