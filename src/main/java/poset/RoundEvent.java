package poset;

import com.google.protobuf.Parser;

import common.IProto;
import poset.proto.Trilean;

public class RoundEvent {
	boolean Consensus; // `protobuf:"varint,1,opt,name=Consensus,proto3" json:"Consensus,omitempty"`
	boolean Witness; //  `protobuf:"varint,2,opt,name=Witness,proto3" json:"Witness,omitempty"`
	Trilean Famous; // `protobuf:"varint,3,opt,name=Famous,proto3,enum=poset.Trilean" json:"Famous,omitempty"`

	public RoundEvent() {
		// TODO: just add these inits
		Consensus = false;
		Witness = false;
		Famous = Trilean.UNDEFINED;
	}

	public RoundEvent(boolean witness) {
		this.Witness= witness;
		// TODO: just add these inits
		Consensus = false;
		Famous = Trilean.UNDEFINED;
	}

	public IProto<RoundEvent, poset.proto.RoundEvent> marshaller() {
		return new IProto<RoundEvent, poset.proto.RoundEvent>() {
			@Override
			public poset.proto.RoundEvent toProto() {
				poset.proto.RoundEvent.Builder builder = poset.proto.RoundEvent.newBuilder();
				builder.setConsensus(Consensus)
				.setWitness(Witness)
				.setFamous(Famous);
				return builder.build();
			}

			@Override
			public void fromProto(poset.proto.RoundEvent proto) {
				Consensus = proto.getConsensus();
				Witness = proto.getWitness();
				Famous = proto.getFamous();
			}

			@Override
			public Parser<poset.proto.RoundEvent> parser() {
				return poset.proto.RoundEvent.parser();
			}
		};
	}

	public boolean GetConsensus() {
		return Consensus;
	}

	public boolean GetWitness() {
		return Witness;
	}

	public Trilean GetFamous()  {
		return Famous;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (Consensus ? 1231 : 1237);
		result = prime * result + ((Famous == null) ? 0 : Famous.hashCode());
		result = prime * result + (Witness ? 1231 : 1237);
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
		RoundEvent other = (RoundEvent) obj;
		if (Consensus != other.Consensus)
			return false;
		if (Famous != other.Famous)
			return false;
		if (Witness != other.Witness)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RoundEvent [Consensus=").append(Consensus).append(", Witness=").append(Witness)
				.append(", Famous=").append(Famous).append("]");
		return builder.toString();
	}
}