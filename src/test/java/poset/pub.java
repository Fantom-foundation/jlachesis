package poset;

import java.security.KeyPair;

class pub {
	long id;
	KeyPair privKey;
	byte[] pubKey;
	String hex;
	public pub(long id, KeyPair privKey, byte[] pubKey, String hex) {
		super();
		this.id = id;
		this.privKey = privKey;
		this.pubKey = pubKey;
		this.hex = hex;
	}
}