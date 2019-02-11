package poset;

import java.util.Arrays;

import com.google.protobuf.ByteString;
import com.google.protobuf.Parser;

import common.IProto;

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
//		return String.format("0x%X", Validator);
		return crypto.Utils.toHexString(Validator);
	}

	public IProto<BlockSignature, poset.proto.BlockSignature> marshaller() {
		return new IProto<BlockSignature, poset.proto.BlockSignature>() {
			@Override
			public poset.proto.BlockSignature toProto() {
				poset.proto.BlockSignature.Builder builder = poset.proto.BlockSignature.newBuilder();

				if (Validator != null) {
					builder.setValidator(ByteString.copyFrom(Validator));
				}
				builder.setIndex(Index);
				if (Signature != null) {
					builder.setSignature(Signature);
				}
				return builder.build();
			}

			@Override
			public void fromProto(poset.proto.BlockSignature pBlock) {
				Validator = pBlock.getValidator().toByteArray();
				Index = pBlock.getIndex();
				Signature = pBlock.getSignature();
			}

			@Override
			public Parser<poset.proto.BlockSignature> parser() {
				return poset.proto.BlockSignature.parser();
			}
		};
	}

	public WireBlockSignature ToWire()  {
		return new WireBlockSignature (Index, Signature);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BlockSignature [Validator=");
		builder.append(Arrays.toString(Validator));
		builder.append(", Index=");
		builder.append(Index);
		builder.append(", Signature=");
		builder.append(Signature);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (Index ^ (Index >>> 32));
		result = prime * result + ((Signature == null) ? 0 : Signature.hashCode());
		result = prime * result + Arrays.hashCode(Validator);
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
		BlockSignature other = (BlockSignature) obj;
		if (Index != other.Index)
			return false;
		if (Signature == null) {
			if (other.Signature != null)
				return false;
		} else if (!Signature.equals(other.Signature))
			return false;
		if (!Arrays.equals(Validator, other.Validator))
			return false;
		return true;
	}
}
