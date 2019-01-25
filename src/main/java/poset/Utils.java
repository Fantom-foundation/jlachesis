package poset;

import java.util.Arrays;

public class Utils {
	public static boolean BytesEquals(byte[] thisByte, byte[] thatByte)  {
		return Arrays.equals(thisByte, thatByte);
	}

	public static boolean ByteArraysEquals(byte[][] thisList, byte[][] thatList)  {
		if (thisList.length != thatList.length) {
			return false;
		}
		for (int i = 0; i < thisList.length; ++i ) {
			if (!BytesEquals(thisList[i],thatList[i])) {
				return false;
			}
		}
		return true;
	}

	public static <T> boolean ListEquals(T[] thisList, T[] thatList) {
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

	public static boolean InternalTransactionListEquals(InternalTransaction[] thisList, InternalTransaction[] thatList) {
		return Utils.<InternalTransaction>ListEquals(thisList, thatList);
	}

	public static boolean BlockSignatureListEquals(BlockSignature[] thisList, BlockSignature[] thatList) {
		return Utils.<BlockSignature>ListEquals(thisList, thatList);
	}

	public static boolean RootListEquals(Root[] thisList, Root[] thatList) {
		return Utils.<Root>ListEquals(thisList, thatList);
	}

	public static boolean EventListEquals(EventMessage[] thisList, EventMessage[] thatList) {
		return Utils.<EventMessage>ListEquals(thisList, thatList);
	}
}