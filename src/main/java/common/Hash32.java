package common;

public class Hash32 {

	private static final int FNV1_32_INIT = 0x811c9dc5;
	private static final int FNV1_PRIME_32 = 16777619;

	private static final int OFFSET_32	= 0x7FFFFFFF + 18652614;
	private static final int PRIME_32	= 16777619;

	public static int Hash32(byte[] data) {
		// check on go hash/fnv, the actual is 32-bit FNV-1a
		int hash = FNV1_32_INIT;
		for (int i = 0; i < data.length; i++) {
			hash ^= (data[i] & 0xff);
			hash *= FNV1_PRIME_32;
	    }

		// TODO ensure a positive number
		hash &= 0x7fffffff;
	    return hash;
	}

	public static int hash32(byte[] data) {
		// check on go hash/fnv, the actual is 32-bit FNV-1a
		int hash = OFFSET_32;
		for (int i = 0; i < data.length; i++) {
			hash ^= data[i];
			hash *= PRIME_32;
	    }

	    return hash;
	}
}
