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
}