package peers;

import java.util.Comparator;

public class PeerComparatorByID implements Comparator<Peer> {

	public PeerComparatorByID() {
	}

	public int compare(Peer o1, Peer o2) {
		return Long.compare(o1.ID, o2.ID);
	}
}
