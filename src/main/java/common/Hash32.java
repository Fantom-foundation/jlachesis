package common;
//import "hash/fnv"

public class Hash32 {

	private static final int FNV1_32_INIT = 0x811c9dc5;
	  private static final int FNV1_PRIME_32 = 16777619;

	public static int Hash32(byte[] data) {
//		h := fnv.New32a()

//		h.Write(data)

//		return int(h.Sum32())


		// check on go hash/fnv, the actual is 32-bit FNV-1a
		// TODO
		int hash = FNV1_32_INIT;
	    for (int i = 0; i < data.length; i++) {
	      hash ^= (data[i] & 0xff);
	      hash *= FNV1_PRIME_32;
	    }

	    return hash;
	}
}
