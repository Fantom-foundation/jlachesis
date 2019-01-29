package autils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * An Appender provides a means to express the append and slide operations.
 *
 * @author qn
 */
public class Appender {

	public static byte[][] append(byte[][] a, byte[][] b) {
		int asize = (a == null ? 0 : a.length);
		int bsize = (b == null ? 0 : b.length);

		byte[][] dest = new byte[asize + bsize][];

		int j = 0;

		if (a != null) {
			for (int i = 0; i < a.length; i++) {
				dest[j] = Arrays.copyOf(a[i], a[i].length);
				j++;
			}
		}

		if (b != null) {
			for (int i = 0; i < b.length; i++) {
				dest[j] = Arrays.copyOf(b[i], b[i].length);
				j++;
			}
		}

		return dest;
	}

	/**
	 * Appends two byte arrays into a single byte array
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	public static byte[] append(byte[] a, byte[] b) {
		byte[] destination = new byte[a.length + b.length];

		// copy a into start of destination (from pos 0, copy a.length bytes)
		System.arraycopy(a, 0, destination, 0, a.length);

		// copy b into end of destination (from pos a.length, copy b.length bytes)
		System.arraycopy(b, 0, destination, a.length, b.length);

		return destination;
	}

	/**
	 * Appends two arrays into an array
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	public static <T> T[] append(T[] a, T[] b) {
		if (a == null && b == null)
			return null;
		if (a == null) {
			return Arrays.copyOf(b, b.length);
		}
		if (b==null) {
			return Arrays.copyOf(a, a.length);
		}

		int countA = -1;
		for (int i = 0; i < a.length; ++i) {
			if (a[i] == null) {
				countA = i;
				break;
			}
		}

		T[] copy = a;
		if (countA == -1) {
			copy= Arrays.copyOf(a, a.length + b.length);
		} else if (countA + b.length > a.length) {
			copy= Arrays.copyOf(a, countA + b.length);
		}

		for (int i = 0; i < b.length ; ++i) {
			copy[countA + i] = b[i];
		}
		return copy;
	}

	/**
	 * Appends an object b into an array
	 * @param a
	 * @param b
	 * @return
	 */
	public static <T> T[] append(T[] a, T b) {
		if (b == null)
			return a;
		if (a == null) {
			T[] res = (T[]) Array.newInstance(b.getClass(), 1);
			res[0] = b;
			return res;
		}

		int countA = -1;
		for (int i = 0; i < a.length; ++i) {
			if (a[i] == null) {
				countA = i;
				break;
			}
		}

		if (countA != -1 && countA <= a.length -1 ) {
			a[countA] = b;
			return a;
		}
		else {
			// need to create a larger array
			T[] copy = Arrays.copyOf(a, a.length + 1);
			copy[a.length] = b;

			return copy;
		}
	}

	/**
	 * Slice a [low : high] returns array from a[low] to a[high-1]
	 *
	 * @param a
	 * @param low
	 * @param high
	 * @return
	 */
	public static byte[][] slice(byte[][] a, int low, int high) {
		byte[][] dest = new byte[high - low][];
		for (int i = low; i < high; ++i) {
			dest[i] = Arrays.copyOf(a[i], a[i].length);
		}
		return dest;
	}

	public static <T> T[] slice(T[] a, int low, int high) {
		if (a == null) {
			return null;
		}

		T[] res = Arrays.copyOfRange(a, low, high);
		return res;
	}
}