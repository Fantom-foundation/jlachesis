package poset;

import com.google.protobuf.Parser;

import common.IProto;
import peers.Peer;

public class InternalTransaction {
	TransactionType Type; // `protobuf:"varint,1,opt,name=Type,json=type,enum=poset.TransactionType" json:"Type,omitempty"`
	Peer Peer; // `protobuf:"bytes,2,opt,name=peer" json:"peer,omitempty"`

	public InternalTransaction(TransactionType type, Peer peer) {
		super();
		Type = type;
		Peer = peer;
	}

	public InternalTransaction() {
		super();
		Type = null;
		Peer = null;
	}

	public InternalTransaction(InternalTransaction internalTransaction) {
		Type = internalTransaction.Type;
		Peer = internalTransaction.Peer;
	}

	public void Reset() {
		Type = null;
		Peer = null;
	}

	public TransactionType GetType() {
		if (Type != null) {
			return Type;
		}
		return TransactionType.PEER_ADD;
	}

	public peers.Peer GetPeer() {
		return Peer;
	}

	public IProto<InternalTransaction, poset.proto.InternalTransaction> marshaller() {
		return new IProto<InternalTransaction, poset.proto.InternalTransaction>() {
			@Override
			public poset.proto.InternalTransaction toProto() {
				poset.proto.InternalTransaction.Builder builder = poset.proto.InternalTransaction.newBuilder();
				if (Type != null) {
					builder.setType(poset.proto.TransactionType.forNumber(Type.value));
				}
				if (Peer != null) {
					builder.setPeer(Peer.marshaller().toProto());
				}
				return builder.build();
			}

			@Override
			public void fromProto(poset.proto.InternalTransaction pIntTransaction) {
				Type = TransactionType.values()[pIntTransaction.getType().getNumber()];

				peers.proto.Peer pPeer = pIntTransaction.getPeer();
				Peer = null;
				if (pPeer != null) {
					Peer = new Peer();
					Peer.marshaller().fromProto(pPeer);
				}
			}

			@Override
			public Parser<poset.proto.InternalTransaction> parser() {
				return poset.proto.InternalTransaction.parser();
			}
		};
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Peer == null) ? 0 : Peer.hashCode());
		result = prime * result + ((Type == null) ? 0 : Type.hashCode());
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
		InternalTransaction other = (InternalTransaction) obj;
		if (Peer == null) {
			if (other.Peer != null)
				return false;
		} else if (!Peer.equals(other.Peer))
			return false;
		if (Type != other.Type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InternalTransaction [Type=");
		builder.append(Type);
		builder.append(", Peer=");
		builder.append(Peer);
		builder.append("]");
		return builder.toString();
	}
}
