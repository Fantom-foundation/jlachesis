package poset;

import java.util.HashMap;
import java.util.Map;

import common.IProto;
import poset.proto.FlagTableWrapper.Builder;

public class FlagTableWrapper {
	Map<String,Long> Body;

	/**
	 * @param body
	 */
	public FlagTableWrapper(Map<String, Long> body) {
		if (body != null) {
			Body = new HashMap<String,Long>(body);
		} else {
			Body = new HashMap<String,Long>();
		}
	}

	public FlagTableWrapper() {
		Body = new HashMap<String,Long>();
	}

	public Map<String,Long> GetBody() {
		return Body;
	}

	public IProto<FlagTableWrapper, poset.proto.FlagTableWrapper> marshaller() {
		return new IProto<FlagTableWrapper, poset.proto.FlagTableWrapper>() {
			@Override
			public poset.proto.FlagTableWrapper toProto() {
				Builder builder = poset.proto.FlagTableWrapper.newBuilder();
				if (Body != null) {
					builder.putAllBody(Body);
				}
				return builder.build();
			}

			@Override
			public void fromProto(poset.proto.FlagTableWrapper pBlock) {
				if (pBlock.getBodyMap() != null) {
					Body.putAll(pBlock.getBodyMap());
				}
			}

			@Override
			public com.google.protobuf.Parser<poset.proto.FlagTableWrapper> parser() {
				return poset.proto.FlagTableWrapper.parser();
			}
		};
	}
}