package poset;

import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.InvalidProtocolBufferException;

import common.IProto;
import common.RetResult;
import common.error;
import poset.proto.PFlagTableWrapper;
import poset.proto.PFlagTableWrapper.FlagTableWrapper.Builder;

public class FlagTableWrapper {
	private PFlagTableWrapper.FlagTableWrapper pFTW;

	Map<String,Long> Body; //  `protobuf:"bytes,1,rep,name=Body,proto3" json:"Body,omitempty" protobuf_key:"bytes,1,opt,name=key,proto3" protobuf_val:"varint,2,opt,name=value,proto3"`

	/**
	 * @param body
	 */
	public FlagTableWrapper(Map<String, Long> body) {
		Body = new HashMap<String,Long>(body);
	}

	public FlagTableWrapper() {
		Body = new HashMap<String,Long>();
	}

	public Map<String,Long> GetBody() {
		return Body;
	}

	public IProto<FlagTableWrapper, PFlagTableWrapper.FlagTableWrapper> marshaller() {
		return new IProto<FlagTableWrapper, PFlagTableWrapper.FlagTableWrapper>() {
			@Override
			public PFlagTableWrapper.FlagTableWrapper toProto() {
				Builder builder = PFlagTableWrapper.FlagTableWrapper.newBuilder();
				if (Body != null) {
					builder.putAllBody(Body);
				}
				return builder.build();
			}

			@Override
			public void fromProto(PFlagTableWrapper.FlagTableWrapper pBlock) {
				if (pFTW.getBodyMap() != null) {
					Body.clear();
					Body.putAll(pFTW.getBodyMap());
				}
			}

			@Override
			public RetResult<byte[]> protoMarshal() {
				if (pFTW == null) {
					pFTW = toProto();
				}
				return new RetResult<>(pFTW.toByteArray(), null);
			}

			@Override
			public error protoUnmarshal(byte[] data) {
				try {
					pFTW = PFlagTableWrapper.FlagTableWrapper.parseFrom(data);
				} catch (InvalidProtocolBufferException e) {
					return error.Errorf(e.getMessage());
				}
				fromProto(pFTW);
				return null;
			}
		};
	}
}