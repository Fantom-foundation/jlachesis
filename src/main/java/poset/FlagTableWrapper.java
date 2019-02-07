package poset;

import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.InvalidProtocolBufferException;

import common.RetResult;
import common.error;
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

	public void Reset()         { Body.clear(); }

	public Map<String,Long> GetBody() {
		return Body;
	}

	public RetResult<byte[]> toByteArray() {
		Builder builder = PFlagTableWrapper.FlagTableWrapper.newBuilder().clearBody();
		builder.putAllBody(Body);
		poset.proto.PFlagTableWrapper.FlagTableWrapper ftw = builder.build();
		byte[] res = ftw.toByteArray();
		return new RetResult<byte[]>(res, null);
	}

	public error fromByteArray(byte[] bytes) {
		try {
			poset.proto.PFlagTableWrapper.FlagTableWrapper parseFtw = PFlagTableWrapper.FlagTableWrapper.parseFrom(bytes);
			if (parseFtw.getBodyMap() != null) {
				Body.clear();
				Body.putAll(parseFtw.getBodyMap());
			}
		} catch (InvalidProtocolBufferException e) {
			return error.Errorf(e.getMessage());
		}
		return null;
	}
}