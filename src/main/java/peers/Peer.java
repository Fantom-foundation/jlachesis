package peers;

import common.Hash32;
import common.IProto;
import common.RResult;
import common.error;

/**
 * A Peer
 */
public class Peer {
	long id; // `protobuf:"varint,1,opt,name=ID,proto3" json:"ID,omitempty"`
	String netAddr; // `protobuf:"bytes,2,opt,name=NetAddr,proto3" json:"NetAddr,omitempty"`
	String pubKeyHex; // `protobuf:"bytes,3,opt,name=PubKeyHex,proto3" json:"PubKeyHex,omitempty"`

	public static final String jsonPeerPath = "peers.json";

	public Peer(long iD, String netAddr, String pubKeyHex) {
		super();
		id = iD;
		this.netAddr = netAddr;
		this.pubKeyHex = pubKeyHex;
	}

	public Peer() {
		super();
		id = -1;
		netAddr = "";
		pubKeyHex = "";
	}

	public Peer(String pubKeyHex, String netAddr) {
		super();
		this.pubKeyHex = pubKeyHex;
		this.netAddr =   netAddr;
		computeID();
	}

	public void setNetAddr(String netAddr) {
		this.netAddr = netAddr;
	}

	public void reset() {
		id = -1;
		netAddr = "";
		pubKeyHex = "";
	}

	public long getID() {
		return id;
	}

	public String getNetAddr() {
		return netAddr;
	}
	public String getPubKeyHex() {
		return pubKeyHex;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((netAddr == null) ? 0 : netAddr.hashCode());
		result = prime * result + ((pubKeyHex == null) ? 0 : pubKeyHex.hashCode());
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
		if (id != other.id)
			return false;
		if (netAddr == null) {
			if (other.netAddr != null)
				return false;
		} else if (!netAddr.equals(other.netAddr))
			return false;
		if (pubKeyHex == null) {
			if (other.pubKeyHex != null)
				return false;
		} else if (!pubKeyHex.equals(other.pubKeyHex))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Peer [ID=").append(id).append(", NetAddr=").append(netAddr).append(", PubKeyHex=")
				.append(pubKeyHex).append("]");
		return builder.toString();
	}

	@SuppressWarnings("finally")
	public RResult<byte[]> PubKeyBytes() {
		//PubKeyHex[2:]
		try {
			byte[] decode = crypto.Utils.decodeString(pubKeyHex.substring(2, pubKeyHex.length())).result;
			return new RResult<byte[]>(decode, null);
		} catch (Exception e){
			return new RResult<byte[]>(null, error.Errorf(e.getMessage()));
		}
	}


	public IProto<Peer, peers.proto.Peer> marshaller() {
		return new IProto<Peer, peers.proto.Peer>() {
			@Override
			public peers.proto.Peer toProto() {
				peers.proto.Peer.Builder builder = peers.proto.Peer.newBuilder();
				builder.setID(id).setNetAddr(netAddr).setPubKeyHex(pubKeyHex);
				return builder.build();
			}

			@Override
			public void fromProto(peers.proto.Peer pPeer) {
				id = pPeer.getID();
				netAddr = pPeer.getNetAddr();
				pubKeyHex = pPeer.getPubKeyHex();
			}

			@Override
			public com.google.protobuf.Parser<peers.proto.Peer> parser() {
				return peers.proto.Peer.parser();
			}
		};
	}

	public error computeID() {
		// TODO: Use the decoded bytes from hex
		RResult<byte[]> pubKey = PubKeyBytes();
		error err = pubKey.err;
		if (err != null) {
			return err;
		}
		id = Hash32.Hash32(pubKey.result);
		return null;
	}
}