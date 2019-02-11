package poset;

import java.util.HashMap;
import java.util.Map;

import common.IProto;
import poset.proto.RoundInfoMessage.Builder;

public class RoundInfoMessage {
	Map<String,RoundEvent> Events; //  `protobuf:"bytes,1,rep,name=Events,proto3" json:"Events,omitempty" protobuf_key:"bytes,1,opt,name=key,proto3" protobuf_val:"bytes,2,opt,name=value,proto3"`

	public RoundInfoMessage() {
		super();
		Events = new HashMap<String,RoundEvent>();
	}

	public RoundInfoMessage(Map<String, RoundEvent> events) {
		super();
		Events = events;
	}



	public IProto<RoundInfoMessage, poset.proto.RoundInfoMessage> marshaller() {
		return new IProto<RoundInfoMessage, poset.proto.RoundInfoMessage>() {
			@Override
			public poset.proto.RoundInfoMessage toProto() {
				Builder builder = poset.proto.RoundInfoMessage.newBuilder();
				if (Events != null) {
					Events.forEach((s,re) -> {
						builder.putEvents(s, re.marshaller().toProto());
					});
				}
				return builder.build();
			}

			@Override
			public void fromProto(poset.proto.RoundInfoMessage proto) {
				Map<String, poset.proto.RoundEvent> eventsMap = proto.getEventsMap();
				Events = null;
				if (eventsMap != null) {
					Events = new HashMap<String,RoundEvent>();
					eventsMap.forEach((s,r) -> {
						RoundEvent re = new RoundEvent();
						re.marshaller().fromProto(r);
						Events.put(s, re);
					});
				}
			}

			@Override
			public com.google.protobuf.Parser<poset.proto.RoundInfoMessage> parser() {
				return poset.proto.RoundInfoMessage.parser();
			}
		};
	}

	public Map<String,RoundEvent> GetEvents() {
		return Events;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Events == null) ? 0 : Events.hashCode());
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
		RoundInfoMessage other = (RoundInfoMessage) obj;
		if (Events == null) {
			if (other.Events != null)
				return false;
		} else if (!Events.equals(other.Events))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RoundInfoMessage [Events=").append(Events).append("]");
		return builder.toString();
	}
}