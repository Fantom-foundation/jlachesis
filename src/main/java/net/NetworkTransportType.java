package net;


public enum NetworkTransportType {
	rpcSync ((byte) 0),
	rpcEagerSync((byte) 1),
	rpcFastForward ((byte) 2);

	NetworkTransportType(byte b) {
		this.b = b;
	}

	byte b;

	public static final NetworkTransportType values[] = values();
}