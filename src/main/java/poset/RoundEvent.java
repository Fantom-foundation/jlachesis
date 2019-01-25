package poset;

import poset.proto.Trilean;

public class RoundEvent {
	boolean Consensus; // `protobuf:"varint,1,opt,name=Consensus,proto3" json:"Consensus,omitempty"`
	boolean Witness; //  `protobuf:"varint,2,opt,name=Witness,proto3" json:"Witness,omitempty"`
	Trilean Famous; // `protobuf:"varint,3,opt,name=Famous,proto3,enum=poset.Trilean" json:"Famous,omitempty"`

	public RoundEvent() {

	}

	public RoundEvent(boolean witness) {
		this.Witness= witness;
	}
//	public Reset()         { *m = RoundEvent{} }
//	public String() string { return proto.CompactTextString(m) }

//	public XXX_Unmarshal(b []byte) error {
//		return xxx_messageInfo_RoundEvent.Unmarshal(m, b)
//	}
//	public XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
//		return xxx_messageInfo_RoundEvent.Marshal(b, m, deterministic)
//	}

	public boolean GetConsensus() {
		return Consensus;
	}

	public boolean GetWitness() {
		return Witness;
	}

	public Trilean GetFamous()  {
		return Famous;
	}

	public boolean equals(RoundEvent that) {
		return this.Consensus == that.Consensus &&
			this.Witness == that.Witness &&
			this.Famous == that.Famous;
	}
}