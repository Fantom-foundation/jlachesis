package net;

import org.jcsp.lang.One2OneChannel;
import java.io.Reader;


import common.error;

/**
 * RPC has a command, and provides a response mechanism.
 */
public class RPC {
	private Object command;
	Reader reader;
	One2OneChannel<RPCResponse> respChan; // chan<- RPCResponse

	public RPC(Object command, Reader reader, One2OneChannel<RPCResponse> respChan) {
		super();
		this.command= command;
		this.reader = reader;
		this.respChan = respChan;
	}

	public RPC(One2OneChannel<RPCResponse> chan) {
		super();
		command = null;
		reader = null;
		respChan = chan;
	}

	public RPC(Object command, One2OneChannel<RPCResponse> chan) {
		super();
		this.command = command;
		this.reader = null;
		this.respChan = chan;
	}

	// Respond is used to respond with a response, error or both
	public void Respond(ParsableMessage resp, error err) {
		respChan.out().write(new RPCResponse(resp, err));
	}

	public Object getCommand() {
		return command;
	}

	public void setCommand(Object command) {
		this.command = command;
	}
	
	
}