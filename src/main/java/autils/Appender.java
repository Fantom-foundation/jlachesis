package autils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.Stream;

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

	public static <T> T[] append(T[] a, T[] b) {
//		final T[] result = (T[]) Array.newInstance(a[0].getClass().getComponentType(), a.length + b.length);
//		result = Stream.of(a, b).flatMap(Stream::of).collect(Collectors.toList()).toArray(result);
//		return result;

		T[] result = Stream.of(a, b).flatMap(Stream::of).toArray(size -> {
			return (T[]) Array.newInstance(a[0].getClass().getComponentType(), size);
		});
		return result;
	}

	public static <T> T[] append(T[] a, T b) {
//		final T[] result = (T[]) Array.newInstance(a[0].getClass().getComponentType(), a.length + b.length);
//		result = Stream.of(a, b).flatMap(Stream::of).collect(Collectors.toList()).toArray(result);
//		return result;

		T[] result = Stream.of(a, b).flatMap(Stream::of).toArray(size -> {
			return (T[]) Array.newInstance(a[0].getClass().getComponentType(), size);
		});
		return result;
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
}