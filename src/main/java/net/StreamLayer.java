package net;

import java.net.InetAddress;
import java.net.Socket;
import java.time.Duration;

import common.RetResult;
import common.error;

/**
 * StreamLayer is used with the NetworkTransport to provide
 * the low level stream abstraction.
 */
interface StreamLayer  {
	/** Dial is used to create a new outgoing connection. */
	RetResult<Socket> dial(String address, Duration timeout);

	RetResult<Socket> accept();

	error close();

	InetAddress addr();
}
