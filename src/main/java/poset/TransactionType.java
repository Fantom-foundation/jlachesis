package poset;

public enum TransactionType {
	PEER_ADD(0), PEER_REMOVE(1);

	TransactionType(int v) {
		value = v;
	}

	int value;
}