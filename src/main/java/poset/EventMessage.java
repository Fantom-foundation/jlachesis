package poset;

import java.util.Arrays;

import com.google.protobuf.ByteString;
import com.google.protobuf.Parser;

import common.IProto;

public class EventMessage {
	EventBody Body;
	String Signature;
	byte[] FlagTable;
	String[] WitnessProof;
	long SelfParentIndex;
	long OtherParentCreatorID;
	long OtherParentIndex;
	long CreatorID;
	long TopologicalIndex;

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

	public EventMessage(EventBody body) {
		this();
		Body = body;

		// TODO add these
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

	public IProto<EventMessage, poset.proto.EventMessage> marshaller() {
		return new IProto<EventMessage, poset.proto.EventMessage>() {
			@Override
			public poset.proto.EventMessage toProto() {
				poset.proto.EventMessage.Builder builder = poset.proto.EventMessage.newBuilder();
				if (Body != null) {
					builder.setBody(Body.marshaller().toProto());
				}
				if (Signature != null) {
					builder.setSignature(Signature);
				}
				if (FlagTable != null) {
					builder.setFlagTable(ByteString.copyFrom(FlagTable));
				}
				if (WitnessProof != null) {
					Arrays.asList(WitnessProof).forEach(witnessProof -> {
						builder.addWitnessProof(witnessProof);
					});
				}
				builder.setSelfParentIndex(SelfParentIndex)
					.setOtherParentCreatorID(OtherParentCreatorID)
					.setOtherParentIndex(OtherParentIndex)
					.setCreatorID(CreatorID)
					.setTopologicalIndex(TopologicalIndex);
				return builder.build();
			}

			@Override
			public void fromProto(poset.proto.EventMessage proto) {
				poset.proto.EventBody body = proto.getBody();
				Body = null;
				if (body != null) {
					Body = new EventBody();
					Body.marshaller().fromProto(body);
				}
				Signature = proto.getSignature();
				FlagTable = proto.getFlagTable().toByteArray();
				WitnessProof = proto.getWitnessProofList().toArray(new String[0]);
				SelfParentIndex = proto.getSelfParentIndex();
				OtherParentCreatorID = proto.getOtherParentCreatorID();
				OtherParentIndex = proto.getOtherParentIndex();
				CreatorID = proto.getCreatorID();
				TopologicalIndex = proto.getTopologicalIndex();
			}

			@Override
			public Parser<poset.proto.EventMessage> parser() {
				return poset.proto.EventMessage.parser();
			}
		};
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EventMessage [Body=").append(Body).append(", Signature=").append(Signature)
				.append(", FlagTable=").append(Arrays.toString(FlagTable)).append(", WitnessProof=")
				.append(Arrays.toString(WitnessProof)).append(", SelfParentIndex=").append(SelfParentIndex)
				.append(", OtherParentCreatorID=").append(OtherParentCreatorID).append(", OtherParentIndex=")
				.append(OtherParentIndex).append(", CreatorID=").append(CreatorID).append(", TopologicalIndex=")
				.append(TopologicalIndex).append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Body == null) ? 0 : Body.hashCode());
		result = prime * result + (int) (CreatorID ^ (CreatorID >>> 32));
		result = prime * result + Arrays.hashCode(FlagTable);
		result = prime * result + (int) (OtherParentCreatorID ^ (OtherParentCreatorID >>> 32));
		result = prime * result + (int) (OtherParentIndex ^ (OtherParentIndex >>> 32));
		result = prime * result + (int) (SelfParentIndex ^ (SelfParentIndex >>> 32));
		result = prime * result + ((Signature == null) ? 0 : Signature.hashCode());
		result = prime * result + (int) (TopologicalIndex ^ (TopologicalIndex >>> 32));
		result = prime * result + Arrays.hashCode(WitnessProof);
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
		EventMessage other = (EventMessage) obj;
		if (Body == null) {
			if (other.Body != null)
				return false;
		} else if (!Body.equals(other.Body))
			return false;
		if (CreatorID != other.CreatorID)
			return false;
		if (!Utils.protoBytesEquals(FlagTable, other.FlagTable))
			return false;
		if (OtherParentCreatorID != other.OtherParentCreatorID)
			return false;
		if (OtherParentIndex != other.OtherParentIndex)
			return false;
		if (SelfParentIndex != other.SelfParentIndex)
			return false;
		if (Signature == null) {
			if (other.Signature != null)
				return false;
		} else if (!Signature.equals(other.Signature))
			return false;
		if (TopologicalIndex != other.TopologicalIndex)
			return false;
		if (!Utils.protoEquals(WitnessProof, other.WitnessProof))
			return false;
		return true;
	}
}