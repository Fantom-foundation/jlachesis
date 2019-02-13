package poset;

import java.util.Arrays;

public class Utils {

	public static boolean protoBytesEquals(byte[] a, byte[] b) {
		if ((a == null && b == null)
			|| (a == null && b.length == 0)
			|| (b == null && a.length == 0)) {
			return true;
		}
		return Arrays.equals(a, b);
	}

	public static boolean protoStringEquals(String a, String b) {
		if ((a == null && b == null)
			|| (a == null && b.isEmpty())
			|| (b == null && a.isEmpty())) {
			return true;
		}
		return a.equals(b);
	}

	public static boolean protoEquals(Object[] a, Object[] b) {
		if ((a == null && b == null)
			|| (a == null && b.length == 0)
			|| (b == null && a.length == 0)) {
			return true;
		}
		return Arrays.equals(a, b);
	}

	public static boolean bytesEquals(byte[] thisByte, byte[] thatByte)  {
		return Arrays.equals(thisByte, thatByte);
	}

	public static boolean byteArraysEquals(byte[][] thisList, byte[][] thatList)  {
		if (thisList.length != thatList.length) {
			return false;
		}
		for (int i = 0; i < thisList.length; ++i ) {
			if (!bytesEquals(thisList[i],thatList[i])) {
				return false;
			}
		}
		return true;
	}

	public static boolean arrayEquals(Object[] thisList, Object[] thatList) {
		if (thisList.length != thatList.length) {
			return false;
		}
		for (int i = 0; i < thisList.length; ++i ) {
			if (!thisList[i].equals(thatList[i])) {
				return false;
			}
		}
		return true;
	}

	public static <T> T[] copyOf(T[] list) {
		if (list == null) {
			return null;
		}
		return Arrays.copyOf(list, list.length);
	}
}