package poset;

import common.RetResult;
import common.error;
import crypto.hash;

public class Frame {
	long Round; //  `protobuf:"varint,1,opt,name=Round,proto3" json:"Round,omitempty"`
	Root[] Roots; //  `protobuf:"bytes,2,rep,name=Roots,proto3" json:"Roots,omitempty"`
	EventMessage[] Events; // `protobuf:"bytes,3,rep,name=Events,proto3" json:"Events,omitempty"`

	public Frame() {
		// TODO
		Round = -1;
		Roots = null;
		Events = null;
	}

	public Frame(long round, Root[] roots, EventMessage[] events) {
		super();
		Round = round;
		Roots = roots;
		Events = events;
	}


	public RetResult<byte[]> ProtoMarshal() {
//		proto.Buffer bf;
//		bSetDeterministic(true);
//		if err := bMarshal(f); err != null {
//			return null, err;
//		}
//		return bBytes(), null;

		// TBD
		return null;
	}

	public error ProtoUnmarshal(byte[] data) {
//		return proto.Unmarshal(data, f);
		// TBD
		return null;
	}

	public RetResult<byte[]> Hash() {
		RetResult<byte[]> protoMarshal = ProtoMarshal();
		byte[] hashBytes = protoMarshal.result;
		error err = protoMarshal.err;
		if (err != null) {
			return new RetResult<byte[]>(null, err);
		}
		return new RetResult<byte[]>(hash.SHA256(hashBytes), null);
	}

	public boolean equals(Frame that) {
		return this.Round == that.Round &&
			Utils.RootListEquals(this.Roots, that.Roots) &&
			Utils.EventListEquals(this.Events, that.Events);
	}

	public long GetRound() {
		return Round;
	}

	public Root[] GetRoots() {
		return Roots;
	}

	public EventMessage[] GetEvents() {
		return Events;
	}
}
