package net;
public enum NetworkTransportEnum {
	rpcSync ((byte) 0), // uint8 = iota
	rpcEagerSync((byte) 1),
	rpcFastForward ((byte) 2);

	NetworkTransportEnum(byte b) {
		this.b = b;
	}

	byte b;

	public static final NetworkTransportEnum values[] = values();
}