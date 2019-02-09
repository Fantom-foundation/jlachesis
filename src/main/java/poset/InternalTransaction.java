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


	public boolean equals(InternalTransaction that) {
		return this.Peer.equals(that.Peer) && this.Type == that.Type;
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
}
