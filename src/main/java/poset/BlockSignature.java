package poset;

import java.util.Arrays;

import com.google.protobuf.ByteString;
import com.google.protobuf.Parser;

import common.IProto;

public class BlockSignature {
	byte[] validator; // `protobuf:"bytes,1,opt,name=Validator,json=validator,proto3" json:"Validator,omitempty"`
	long index; // `protobuf:"varint,2,opt,name=Index,json=index" json:"Index,omitempty"`
	String signature; // `protobuf:"bytes,3,opt,name=Signature,json=signature" json:"Signature,omitempty"`

	public BlockSignature(byte[] validator, long index, String signature) {
		super();
		this.validator = validator;
		this.index = index;
		this.signature = signature;
	}

	public BlockSignature() {
	}

	public BlockSignature(BlockSignature blockSignature) {
		super();
		this.validator = Arrays.copyOf(blockSignature.validator, blockSignature.validator.length);
		this.index = blockSignature.index;
		this.signature = blockSignature.signature;
	}

	public void Reset() {
		validator = null;
		index = 0;
		signature = "";
	}

	public byte[] getValidator() {
		return validator;
	}

	public long getIndex() {
		return index;
	}

	public String getSignature() {
			return signature;
	}

	public String validatorHex() {
//		return String.format("0x%X", Validator);
		return crypto.Utils.toHexString(validator);
	}

	public IProto<BlockSignature, poset.proto.BlockSignature> marshaller() {
		return new IProto<BlockSignature, poset.proto.BlockSignature>() {
			@Override
			public poset.proto.BlockSignature toProto() {
				poset.proto.BlockSignature.Builder builder = poset.proto.BlockSignature.newBuilder();

				if (validator != null) {
					builder.setValidator(ByteString.copyFrom(validator));
				}
				builder.setIndex(index);
				if (signature != null) {
					builder.setSignature(signature);
				}
				return builder.build();
			}

			@Override
			public void fromProto(poset.proto.BlockSignature pBlock) {
				validator = pBlock.getValidator().toByteArray();
				index = pBlock.getIndex();
				signature = pBlock.getSignature();
			}

			@Override
			public Parser<poset.proto.BlockSignature> parser() {
				return poset.proto.BlockSignature.parser();
			}
		};
	}

	public WireBlockSignature toWire()  {
		return new WireBlockSignature (index, signature);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BlockSignature [Validator=");
		builder.append(Arrays.toString(validator));
		builder.append(", Index=");
		builder.append(index);
		builder.append(", Signature=");
		builder.append(signature);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (index ^ (index >>> 32));
		result = prime * result + ((signature == null) ? 0 : signature.hashCode());
		result = prime * result + Arrays.hashCode(validator);
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
		if (index != other.index)
			return false;
		if (signature == null) {
			if (other.signature != null)
				return false;
		} else if (!signature.equals(other.signature))
			return false;
		if (!Arrays.equals(validator, other.validator))
			return false;
		return true;
	}
}
