package common;

import java.util.List;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;

/**
 * Interace for the un/marshaller
 *
 * @param <T> Type
 * @param <P> Proto
 */
public interface IProto<T, P extends AbstractMessage> {

	public abstract P toProto();

	public abstract void fromProto(P proto);

	public abstract Parser<P> parser();

	default public RResult<byte[]> protoMarshal() {
		return new RResult<>(toProto().toByteArray(), null);
	}

	default public error protoUnmarshal(byte[] data) {
		try {
			P pBlock = parser().parseFrom(data);
			fromProto(pBlock);
			return null;
		} catch (InvalidProtocolBufferException e) {
			return error.Errorf(e.getMessage());
		}
	}

	default public byte[][] toArray(List<ByteString> list) {
		int txCount = list.size();
		byte[][] tx = new byte[][]{};
		if (txCount > 0) {
			tx = new byte[txCount][];
			for (int i = 0; i < txCount; ++i) {
				tx[i] = list.get(i).toByteArray();
			}
		}
		return tx;
	}
}
