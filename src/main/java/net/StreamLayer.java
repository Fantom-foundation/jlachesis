package net;

import java.net.InetAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.Duration;

import common.RResult;
import common.error;

/**
 * StreamLayer is used with the NetworkTransport to provide
 * the low level stream abstraction.
 */
interface StreamLayer  {
	/** Dial is used to create a new outgoing connection. */
	RResult<SocketChannel> dial(String address, Duration timeout);

	RResult<SocketChannel> accept();

	Selector selector();

	error close();

	InetAddress addr();
}
