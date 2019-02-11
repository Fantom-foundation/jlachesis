package peers;

import common.Hash32;
import common.IProto;
import common.RetResult;
import common.error;

/**
 * A Peer
 *
 */
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (ID ^ (ID >>> 32));
		result = prime * result + ((NetAddr == null) ? 0 : NetAddr.hashCode());
		result = prime * result + ((PubKeyHex == null) ? 0 : PubKeyHex.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Peer other = (Peer) obj;
		if (ID != other.ID)
			return false;
		if (NetAddr == null) {
			if (other.NetAddr != null)
				return false;
		} else if (!NetAddr.equals(other.NetAddr))
			return false;
		if (PubKeyHex == null) {
			if (other.PubKeyHex != null)
				return false;
		} else if (!PubKeyHex.equals(other.PubKeyHex))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Peer [ID=").append(ID).append(", NetAddr=").append(NetAddr).append(", PubKeyHex=")
				.append(PubKeyHex).append("]");
		return builder.toString();
	}

	@SuppressWarnings("finally")
	public RetResult<byte[]> PubKeyBytes() {
		//PubKeyHex[2:]
		try {
			byte[] decode = crypto.Utils.decodeString(PubKeyHex.substring(2, PubKeyHex.length())).result;
			return new RetResult<byte[]>(decode, null);
		} catch (Exception e){
			return new RetResult<byte[]>(null, error.Errorf(e.getMessage()));
		}
	}


	public IProto<Peer, peers.proto.Peer> marshaller() {
		return new IProto<Peer, peers.proto.Peer>() {
			@Override
			public peers.proto.Peer toProto() {
				peers.proto.Peer.Builder builder = peers.proto.Peer.newBuilder();
				builder.setID(ID).setNetAddr(NetAddr).setPubKeyHex(PubKeyHex);
				return builder.build();
			}

			@Override
			public void fromProto(peers.proto.Peer pPeer) {
				ID = pPeer.getID();
				NetAddr = pPeer.getNetAddr();
				PubKeyHex = pPeer.getPubKeyHex();
			}

			@Override
			public com.google.protobuf.Parser<peers.proto.Peer> parser() {
				return peers.proto.Peer.parser();
			}
		};
	}

	public error computeID() {
		// TODO: Use the decoded bytes from hex
		RetResult<byte[]> pubKey = PubKeyBytes();
		error err = pubKey.err;
		if (err != null) {
			return err;
		}
		ID = Hash32.Hash32(pubKey.result);
		return null;
	}

	public void Reset() {
		ID = -1;
		NetAddr = "";
		PubKeyHex = "";
	}

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