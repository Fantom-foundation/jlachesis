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

	public boolean equals(WireBlockSignature that) {
		return this.Index == that.Index && this.Signature.equals(that.Signature);
	}
}
