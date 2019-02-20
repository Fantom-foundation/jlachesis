package net;

import java.net.InetAddress;
import java.net.Socket;
import java.time.Duration;

import common.RResult;
import common.error;

/**
 * StreamLayer is used with the NetworkTransport to provide
 * the low level stream abstraction.
 */
interface StreamLayer  {
	/** Dial is used to create a new outgoing connection. */
	RResult<Socket> dial(String address, Duration timeout);

	RResult<Socket> accept();

	error close();

	InetAddress addr();
}
