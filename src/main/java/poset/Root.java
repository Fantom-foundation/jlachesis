package poset;

import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.Parser;

import common.IProto;

public class Root {
	long NextRound; //  `protobuf:"varint,1,opt,name=NextRound,proto3" json:"NextRound,omitempty"`
	RootEvent SelfParent; //   `protobuf:"bytes,2,opt,name=SelfParent,proto3" json:"SelfParent,omitempty"`
	Map<String,RootEvent> Others; // `protobuf:"bytes,3,rep,name=Others,proto3" json:"Others,omitempty" protobuf_key:"bytes,1,opt,name=key,proto3" protobuf_val:"bytes,2,opt,name=value,proto3"`

	public Root()
	{
		// TODO add these inits. ok?
		NextRound = -1;
		SelfParent = null;
		Others = null;
	}

	public Root(long nextRound, RootEvent selfParent, Map<String, RootEvent> others) {
		super();
		NextRound = nextRound;
		SelfParent = selfParent;
		Others = new HashMap<String,RootEvent>(others);
	}

	/**
	 * Root forms a base on top of which a participant's Events can be inserted. It
	 * contains the SelfParent of the first descendant of the Root, as well as other
	 * Events, belonging to a past before the Root, which might be referenced
	 * in future Events. NextRound corresponds to a proposed value for the child's
	 * Round; it is only used if the child's OtherParent is empty or NOT in the
	 * Root's Others. NewBaseRoot initializes a Root object for a fresh Poset.
	 *
	 * @param creatorID
	 */
	public Root(long creatorID) {
		super();
		RootEvent rootEvent = new RootEvent(creatorID);
		this.NextRound = 0;
		this.SelfParent = rootEvent;
		this.Others = new HashMap<String,RootEvent>();
	}

	public IProto<Root, poset.proto.Root> marshaller() {
		return new IProto<Root, poset.proto.Root>() {
			@Override
			public poset.proto.Root toProto() {
				poset.proto.Root.Builder builder = poset.proto.Root.newBuilder();
				builder.setNextRound(NextRound);
				if (SelfParent != null) {
					builder.setSelfParent(SelfParent.marshaller().toProto());
				}

				if (Others != null) {
					Others.forEach((s,r) -> {
						builder.putOthers(s, r.marshaller().toProto());
					});
				}
				return builder.build();
			}

			@Override
			public void fromProto(poset.proto.Root proto) {
				NextRound = proto.getNextRound();

				poset.proto.RootEvent sParent = proto.getSelfParent();
				SelfParent = null;
				if (sParent != null) {
					SelfParent = new RootEvent();
					SelfParent.marshaller().fromProto(sParent);
				}

				Map<String, poset.proto.RootEvent> othersMap = proto.getOthersMap();
				Others = null;
				if (othersMap != null) {
					Others = new HashMap<String,RootEvent>();
					othersMap.forEach((s,r) -> {
						RootEvent re = new RootEvent();
						re.marshaller().fromProto(r);
						Others.put(s, re);
					});
				}
			}

			@Override
			public Parser<poset.proto.Root> parser() {
				return poset.proto.Root.parser();
			}
		};
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Root [NextRound=").append(NextRound).append(", SelfParent=").append(SelfParent)
				.append(", Others=").append(Others).append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (NextRound ^ (NextRound >>> 32));
		result = prime * result + ((Others == null) ? 0 : Others.hashCode());
		result = prime * result + ((SelfParent == null) ? 0 : SelfParent.hashCode());
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
		Root other = (Root) obj;
		if (NextRound != other.NextRound)
			return false;
		if (Others == null) {
			if (other.Others != null)
				return false;
		} else if (!Others.equals(other.Others))
			return false;
		if (SelfParent == null) {
			if (other.SelfParent != null)
				return false;
		} else if (!SelfParent.equals(other.SelfParent))
			return false;
		return true;
	}

	public long GetNextRound() {
		return NextRound;
	}

	public RootEvent GetSelfParent() {
		return SelfParent;
	}

	public Map<String,RootEvent> GetOthers() {
		return Others;
	}
}