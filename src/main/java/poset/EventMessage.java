package poset;

import java.util.Arrays;

public class EventMessage {
	EventBody Body; // `protobuf:"bytes,1,opt,name=Body,json=body" json:"Body,omitempty"`
	String Signature; //     `protobuf:"bytes,2,opt,name=Signature,json=signature" json:"Signature,omitempty"`
	byte[] FlagTable; //     `protobuf:"bytes,3,opt,name=FlagTable,json=flagTable,proto3" json:"FlagTable,omitempty"`
	String[] WitnessProof; //   `protobuf:"bytes,4,rep,name=WitnessProof,json=witnessProof" json:"WitnessProof,omitempty"`
	long SelfParentIndex; //      `protobuf:"varint,5,opt,name=SelfParentIndex,json=selfParentIndex" json:"SelfParentIndex,omitempty"`
	long OtherParentCreatorID; //      `protobuf:"varint,6,opt,name=OtherParentCreatorID,json=otherParentCreatorID" json:"OtherParentCreatorID,omitempty"`
	long OtherParentIndex; //      `protobuf:"varint,7,opt,name=OtherParentIndex,json=otherParentIndex" json:"OtherParentIndex,omitempty"`
	long CreatorID; //      `protobuf:"varint,8,opt,name=CreatorID,json=creatorID" json:"CreatorID,omitempty"`
	long TopologicalIndex; //      `protobuf:"varint,9,opt,name=TopologicalIndex,json=topologicalIndex" json:"TopologicalIndex,omitempty"`

	public EventMessage() {
		super();
		Body = null;
		Signature = "";
		FlagTable = null;
		WitnessProof = null;
		SelfParentIndex = -1;
		OtherParentCreatorID = -1;
		OtherParentIndex = -1;
		CreatorID = -1;
		TopologicalIndex = -1;
	}

	public EventMessage(EventBody body, String signature, byte[] flagTable, String[] witnessProof, long selfParentIndex,
			long otherParentCreatorID, long otherParentIndex, long creatorID, long topologicalIndex) {
		super();
		Body = body;
		Signature = signature;
		FlagTable = flagTable;
		WitnessProof = witnessProof;
		SelfParentIndex = selfParentIndex;
		OtherParentCreatorID = otherParentCreatorID;
		OtherParentIndex = otherParentIndex;
		CreatorID = creatorID;
		TopologicalIndex = topologicalIndex;
	}

	public EventMessage(EventMessage eventMessage) {
		// TODO: make copy constructor for Body, FlagTable and WitnessProof
		Body = eventMessage.Body;
		Signature = eventMessage.Signature;
		FlagTable = eventMessage.FlagTable;
		WitnessProof = eventMessage.WitnessProof;
		SelfParentIndex = eventMessage.SelfParentIndex;
		OtherParentCreatorID = eventMessage.OtherParentCreatorID;
		OtherParentIndex = eventMessage.OtherParentIndex;
		CreatorID = eventMessage.CreatorID;
		TopologicalIndex = eventMessage.TopologicalIndex;
	}

	public void Reset() {
		Body = null;
		Signature = "";
		FlagTable = null;
		WitnessProof = null;
		SelfParentIndex = -1;
		OtherParentCreatorID = -1;
		OtherParentIndex = -1;
		CreatorID = -1;
		TopologicalIndex = -1;
	}

	public EventBody GetBody() {
		return Body;
	}

	public String GetSignature() {
		return Signature;
	}

	public byte[] GetFlagTable() {
		return FlagTable;
	}

	public String[] GetWitnessProof() {
		return WitnessProof;
	}

	public long GetSelfParentIndex() {
		return SelfParentIndex;
	}

	public long GetOtherParentCreatorID() {
		return OtherParentCreatorID;
	}

	public long GetOtherParentIndex() {
		return OtherParentIndex;
	}

	public long GetCreatorID() {
		return CreatorID;
	}

	public long GetTopologicalIndex() {
		return TopologicalIndex;
	}


	public Event ToEvent() {
		return new Event(this);
	}

	public boolean equals(EventMessage that) {
		return this.Body.equals(that.Body) &&
		this.Signature == that.Signature &&
		Utils.BytesEquals(this.FlagTable, that.FlagTable) &&
		Utils.ListEquals(this.WitnessProof, that.WitnessProof);
}

	@Override
	public String toString() {
		return "EventMessage [Body=" + Body + ", Signature=" + Signature + ", FlagTable=" + Arrays.toString(FlagTable)
				+ ", WitnessProof=" + Arrays.toString(WitnessProof) + ", SelfParentIndex=" + SelfParentIndex
				+ ", OtherParentCreatorID=" + OtherParentCreatorID + ", OtherParentIndex=" + OtherParentIndex
				+ ", CreatorID=" + CreatorID + ", TopologicalIndex=" + TopologicalIndex + "]";
	}
}