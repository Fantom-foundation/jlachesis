package net;

import org.jcsp.lang.One2OneChannel;

import common.error;

/**
 * RPC has a command, and provides a response mechanism.
 */
public class RPC {
	private Object command;
	private One2OneChannel<RPCResponse> respChan; // chan<- RPCResponse

	public RPC(One2OneChannel<RPCResponse> chan) {
		super();
		command = null;
		respChan = chan;
	}

	public RPC(Object command, One2OneChannel<RPCResponse> chan) {
		super();
		this.command = command;
		this.respChan = chan;
	}

	/**
	 * Respond is used to respond with a response, error or both
	 * @param resp
	 * @param err
	 */
	public void respond(ParsableMessage resp, error err) {
		respChan.out().write(new RPCResponse(resp, err));
	}

	public Object getCommand() {
		return command;
	}

	public void setCommand(Object command) {
		this.command = command;
	}
}