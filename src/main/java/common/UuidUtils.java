package common;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.google.protobuf.ByteString;

public class UuidUtils {
  public static UUID asUuid(byte[] bytes) {
    ByteBuffer bb = ByteBuffer.wrap(bytes);
    long firstLong = bb.getLong();
    long secondLong = bb.getLong();
    return new UUID(firstLong, secondLong);
  }

  public static byte[] asBytes(UUID uuid) {
    ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());
    return bb.array();
  }

  public static ByteString asByteString(UUID uuid) {
	  return ByteString.copyFrom(asBytes(uuid));
  }
}