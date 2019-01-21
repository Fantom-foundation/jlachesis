package poset;

import java.util.Arrays;

public class BlockSignature {

	byte[] Validator; // `protobuf:"bytes,1,opt,name=Validator,json=validator,proto3" json:"Validator,omitempty"`
	long Index; // `protobuf:"varint,2,opt,name=Index,json=index" json:"Index,omitempty"`
	String Signature; // `protobuf:"bytes,3,opt,name=Signature,json=signature" json:"Signature,omitempty"`

	public BlockSignature(byte[] validator, long index, String signature) {
		super();
		Validator = validator;
		Index = index;
		Signature = signature;
	}

	public BlockSignature() {
	}

	public BlockSignature(BlockSignature blockSignature) {
		super();
		this.Validator = Arrays.copyOf(blockSignature.Validator, blockSignature.Validator.length);
		this.Index = blockSignature.Index;
		this.Signature = blockSignature.Signature;
	}

	public void Reset() {
		Validator = null;
		Index = 0;
		Signature = "";
	}

	public byte[] GetValidator() {
		return Validator;
	}

	public long GetIndex() {
		return Index;
	}

	public String GetSignature() {
			return Signature;
	}


	public String ValidatorHex() {
		return String.format("0x%X", Validator);
	}

//	public RetResult<byte[]> ProtoMarshal() {
//		var bf proto.Buffer
//		bf.SetDeterministic(true)
//		if err := bf.Marshal(bs); err != null {
//			return null, err;
//		}
//		return bf.Bytes(), null;
//		return null;
//	}

//	public error ProtoUnmarshal( byte[] data) {
//		return proto.Unmarshal(data, this);
//	}

	public WireBlockSignature ToWire()  {
		return new WireBlockSignature (Index, Signature);
	}


	public boolean equals(BlockSignature that) {
		return Utils.BytesEquals(this.Validator, that.Validator) &&
			this.Index == that.Index &&
			this.Signature == that.Signature;
}

}
