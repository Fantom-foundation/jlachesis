package poset;

import java.util.Arrays;

public class Utils {
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
}