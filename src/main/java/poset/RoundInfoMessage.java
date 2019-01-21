package poset;

import java.util.HashMap;
import java.util.Map;

public class RoundInfoMessage {
	Map<String,RoundEvent> Events; //  `protobuf:"bytes,1,rep,name=Events,proto3" json:"Events,omitempty" protobuf_key:"bytes,1,opt,name=key,proto3" protobuf_val:"bytes,2,opt,name=value,proto3"`

	public RoundInfoMessage() {
		super();
		Events = new HashMap<String,RoundEvent>();
	}
	
//	public XXX_Unmarshal(b []byte) error {
//		return xxx_messageInfo_RoundInfoMessage.Unmarshal(m, b)
//	}
//	public XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
//		return xxx_messageInfo_RoundInfoMessage.Marshal(b, m, deterministic)
//	}
	

	public Map<String,RoundEvent> GetEvents() {
		return Events;
	}
}