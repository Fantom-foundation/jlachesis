package poset;

public class WireBlockSignature {
	long Index; // `protobuf:"varint,1,opt,name=Index,json=index" json:"Index,omitempty"`
	String Signature; // `protobuf:"bytes,2,opt,name=Signature,json=signature" json:"Signature,omitempty"`

	public WireBlockSignature(long index, String signature) {
		super();
		Index = index;
		Signature = signature;
	}

	public void Reset() {
		Index = 0;
		Signature = "";
	}

	public long GetIndex() {
		return Index;
	}
	public String GetSignature() {
		return this.Signature;
	}

	public WireBlockSignature ToWire()  {
		return new WireBlockSignature (Index, Signature);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WireBlockSignature other = (WireBlockSignature) obj;
		if (Index != other.Index)
			return false;
		if (Signature == null) {
			if (other.Signature != null)
				return false;
		} else if (!Signature.equals(other.Signature))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WireBlockSignature [Index=");
		builder.append(Index);
		builder.append(", Signature=");
		builder.append(Signature);
		builder.append("]");
		return builder.toString();
	}
}
