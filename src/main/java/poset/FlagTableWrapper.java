package poset;

import java.util.HashMap;
import java.util.Map;

import common.IProto;
import poset.proto.PFlagTableWrapper;
import poset.proto.PFlagTableWrapper.FlagTableWrapper.Builder;

public class FlagTableWrapper {
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
				if (pBlock.getBodyMap() != null) {
					Body.putAll(pBlock.getBodyMap());
				}
			}

			@Override
			public com.google.protobuf.Parser<poset.proto.PFlagTableWrapper.FlagTableWrapper> parser() {
				return PFlagTableWrapper.FlagTableWrapper.parser();
			}
		};
	}
}