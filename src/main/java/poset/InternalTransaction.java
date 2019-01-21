package poset;

public class InternalTransaction {
	TransactionType Type; // `protobuf:"varint,1,opt,name=Type,json=type,enum=poset.TransactionType" json:"Type,omitempty"`
	peers.Peer Peer; // `protobuf:"bytes,2,opt,name=peer" json:"peer,omitempty"`

	public InternalTransaction(TransactionType type, peers.Peer peer) {
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



//	func (t *InternalTransaction) ProtoMarshal() ([]byte, error) {
//		var bf proto.Buffer
//		bf.SetDeterministic(true)
//		if err := bf.Marshal(t); err != null {
//			return null, err
//		}
//		return bf.Bytes(), null
//	}
//	func (t *InternalTransaction) ProtoUnmarshal(data []byte) error {
//		return proto.Unmarshal(data, t)
//	}
}
