package poset;

import java.util.HashMap;
import java.util.Map;

import common.RetResult;
import common.error;

public class FlagTableWrapper {
	Map<String,Long> Body; //  `protobuf:"bytes,1,rep,name=Body,proto3" json:"Body,omitempty" protobuf_key:"bytes,1,opt,name=key,proto3" protobuf_val:"varint,2,opt,name=value,proto3"`

	/**
	 * @param body
	 */
	public FlagTableWrapper(Map<String, Long> body) {
		Body = body;
	}

	public FlagTableWrapper() {
		Body = new HashMap<String,Long>();
		// TODO Auto-generated constructor stub
	}

	public void Reset()         { Body.clear(); }
//	public String() string { return proto.CompactTextString(m) }

	public Map<String,Long> GetBody() {
		return Body;
	}

	public RetResult<byte[]> toByteArray() {
		// TODO
		byte[] res = null;

		return new RetResult<byte[]>(res, null);
	}

	public error fromByteArray(byte[] bytes) {
		// TODO
		error err = null;
		FlagTableWrapper res = null;

		return err;
	}
}