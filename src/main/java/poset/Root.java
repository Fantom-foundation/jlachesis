package poset;

import java.util.HashMap;
import java.util.Map;

import common.RetResult;
import common.error;

public class Root {
	long NextRound; //  `protobuf:"varint,1,opt,name=NextRound,proto3" json:"NextRound,omitempty"`
	RootEvent SelfParent; //   `protobuf:"bytes,2,opt,name=SelfParent,proto3" json:"SelfParent,omitempty"`
	Map<String,RootEvent> Others; // `protobuf:"bytes,3,rep,name=Others,proto3" json:"Others,omitempty" protobuf_key:"bytes,1,opt,name=key,proto3" protobuf_val:"bytes,2,opt,name=value,proto3"`

	public Root()
	{

	}

	public Root(long nextRound, RootEvent selfParent, Map<String, RootEvent> others) {
		super();
		NextRound = nextRound;
		SelfParent = selfParent;
		Others = new HashMap<String,RootEvent>(others);
	}

	//Root forms a base on top of which a participant's Events can be inserted. It
	//contains the SelfParent of the first descendant of the Root, as well as other
	//Events, belonging to a past before the Root, which might be referenced
	//in future Events. NextRound corresponds to a proposed value for the child's
	//Round; it is only used if the child's OtherParent is empty or NOT in the
	//Root's Others.
	//NewBaseRoot initializes a Root object for a fresh Poset.
	public Root(long creatorID) {
		super();
		RootEvent rootEvent = new RootEvent(creatorID);
		this.NextRound = 0;
		this.SelfParent = rootEvent;
		this.Others = new HashMap<String,RootEvent>();
	}

//	public Reset()         { *m = Root{} }
//	public String() string { return proto.CompactTextString(m) }
//
//	public XXX_Unmarshal(b []byte) error {
//		return xxx_messageInfo_Root.Unmarshal(m, b)
//	}
//	public XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
//		return xxx_messageInfo_Root.Marshal(b, m, deterministic)
//	}

	public long GetNextRound() {
		return NextRound;
	}

	public RootEvent GetSelfParent() {
		return SelfParent;
	}

	public Map<String,RootEvent> GetOthers() {
		return Others;
	}

	public boolean EqualsMapStringRootEvent(Map<String,RootEvent> thisMap, Map<String,RootEvent> thatMap) {
		return thisMap.equals(thatMap);
	}

	public boolean Equals(Root that) {
		return this.NextRound == that.NextRound &&
			this.SelfParent.equals(that.SelfParent) &&
			EqualsMapStringRootEvent(this.Others, that.Others);
	}

	public RetResult<byte[]> ProtoMarshal() {
//		proto.Buffer bf;
//		bf.SetDeterministic(true);
//		if err := bf.Marshal(root); err != null {
//			return null, err;
//		}
//		return bf.Bytes(), null;

		// TBD

		return null;
	}

	public error ProtoUnmarshal(byte[] data) {
//		return proto.Unmarshal(data, root);
		// TBD

		return null;
	}
}