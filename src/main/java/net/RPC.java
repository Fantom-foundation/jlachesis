package net;

import org.jcsp.lang.One2OneChannel;
import java.io.Reader;


import common.error;

// RPC has a command, and provides a response mechanism.
public class RPC {
	private Object Command;
	Reader Reader;
	One2OneChannel<RPCResponse> RespChan; // chan<- RPCResponse

	public RPC(Object command, Reader reader, One2OneChannel<RPCResponse> respChan) {
		super();
		Command= command;
		Reader = reader;
		RespChan = respChan;
	}

	public RPC(One2OneChannel<RPCResponse> chan) {
		super();
		Command = null;
		Reader = null;
		RespChan = chan;
	}

	public RPC(Object command, One2OneChannel<RPCResponse> chan) {
		super();
		Command = command;
		Reader = null;
		RespChan = chan;
	}

	// Respond is used to respond with a response, error or both
	public void Respond(Object resp, error err) {
		RespChan.out().write(new RPCResponse(resp, err));
	}

	public Object getCommand() {
		return Command;
	}

	public void setCommand(Object command) {
		Command = command;
	}
}