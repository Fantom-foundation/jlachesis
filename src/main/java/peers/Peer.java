package peers;

import org.bouncycastle.util.encoders.Hex;

import common.Hash32;
import common.RetResult;
import common.error;

// "encoding/hex"

public class Peer {
	long ID; // `protobuf:"varint,1,opt,name=ID,proto3" json:"ID,omitempty"`
	String NetAddr; // `protobuf:"bytes,2,opt,name=NetAddr,proto3" json:"NetAddr,omitempty"`
	String PubKeyHex; // `protobuf:"bytes,3,opt,name=PubKeyHex,proto3" json:"PubKeyHex,omitempty"`

	public static final String jsonPeerPath = "peers.json";

	public Peer(long iD, String netAddr, String pubKeyHex) {
		super();
		ID = iD;
		NetAddr = netAddr;
		PubKeyHex = pubKeyHex;
	}

	public Peer() {
		super();
		ID = -1;
		NetAddr = "";
		PubKeyHex = "";
	}

	public Peer(String pubKeyHex, String netAddr) {
		super();

		PubKeyHex = pubKeyHex;
		NetAddr =   netAddr;

		computeID();
	}

	public boolean equals(Peer that) {
		return this.ID == that.ID &&
			this.NetAddr == that.NetAddr &&
			this.PubKeyHex == that.PubKeyHex;
	}

	@SuppressWarnings("finally")
	public RetResult<byte[]> PubKeyBytes() {
		//PubKeyHex[2:]
		byte[] decode = null;
		error err = null;
		try {
			 decode = Hex.decode(PubKeyHex.substring(2,  PubKeyHex.length()));
		} catch (Exception e){
			err = new error(e.getMessage());
		} finally {
			return new RetResult<byte[]>(decode, err);
		}
	}

	public error computeID() {
		// TODO: Use the decoded bytes from hex
		RetResult<byte[]> pubKey = PubKeyBytes();
		error err = pubKey.err;
		if (err != null) {
			return err;
		}

		int ID = Hash32.Hash32(pubKey.result);

		return null;
	}

	public void Reset() {
		ID = -1;
		NetAddr = "";
		PubKeyHex = "";
	}

//	public XXX_Unmarshal(b []byte) error {
//		return xxx_messageInfo_Peer.Unmarshal(m, b)
//	}
//	public XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
//		return xxx_messageInfo_Peer.Marshal(b, m, deterministic)
//	}

//	var xxx_messageInfo_Peer proto.InternalMessageInfo

	public long GetID() {
		return ID;
	}

	public String GetNetAddr() {
		return NetAddr;
	}
	public String GetPubKeyHex() {
		return PubKeyHex;
	}
}