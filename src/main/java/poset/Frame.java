package poset;

import java.util.Arrays;

import com.google.protobuf.Parser;

import common.IProto;
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

	public IProto<Frame, poset.proto.Frame> marshaller() {
		return new IProto<Frame, poset.proto.Frame>() {
			@Override
			public poset.proto.Frame toProto() {
				poset.proto.Frame.Builder builder = poset.proto.Frame.newBuilder();
				builder.setRound(Round);

				if (Roots != null) {
					Arrays.asList(Roots).forEach(root -> {
						builder.addRoots(root.marshaller().toProto());
					});
				}
				builder.setRound(Round);
				if (Events != null) {
					Arrays.asList(Events).forEach(event -> {
						builder.addEvents(event.marshaller().toProto());
					});
				}
				return builder.build();
			}

			@Override
			public void fromProto(poset.proto.Frame proto) {
				Round = proto.getRound();

				int rootCount = proto.getRootsCount();
				Roots = null;
				if (rootCount > 0) {
					Roots = new Root[rootCount];
					for (int i =0; i < rootCount; ++i) {
						Roots[i] = new Root();
						Roots[i].marshaller().fromProto(proto.getRoots(i));
					}
				}

				int eventCount = proto.getEventsCount();
				Events = null;
				if (eventCount > 0) {
					Events = new EventMessage[eventCount];
					for (int i =0; i < eventCount; ++i) {
						Events[i] = new EventMessage();
						Events[i].marshaller().fromProto(proto.getEvents(i));
					}
				}
			}

			@Override
			public Parser<poset.proto.Frame> parser() {
				return poset.proto.Frame.parser();
			}
		};
	}

	public RetResult<byte[]> Hash() {
		RetResult<byte[]> protoMarshal = marshaller().protoMarshal();
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
