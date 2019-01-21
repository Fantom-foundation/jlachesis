package poset;

import java.math.BigInteger;
import java.util.Comparator;

public class EventComparatorByLamportTimestamp implements Comparator<Event> {

	public EventComparatorByLamportTimestamp() {
		// TODO Auto-generated constructor stub
	}

	public int compare(Event o1, Event o2) {
		long it = o1.lamportTimestamp;
		long jt = o2.lamportTimestamp;
		if (it != jt) {
			return Long.compare(it, jt);
		}

		BigInteger wsi = crypto.Utils.DecodeSignature(o1.Message.Signature).result1;
		BigInteger wsj = crypto.Utils.DecodeSignature(o2.Message.Signature).result1;
		return wsi.compareTo(wsj);
	}
}
