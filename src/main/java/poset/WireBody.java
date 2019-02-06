package poset;
public class WireBody {
	byte[][] Transactions;
	InternalTransaction[] InternalTransactions;
	WireBlockSignature[] BlockSignatures;

	long SelfParentIndex;
	long OtherParentCreatorID;
	long OtherParentIndex;
	long CreatorID;

	long Index;

	public WireBody() {
		Transactions = null;
		InternalTransactions = null;
		BlockSignatures = null;
		SelfParentIndex = -1;
		OtherParentCreatorID = -1;
		OtherParentIndex = -1;
		CreatorID = -1;
		Index = -1;
	}

	public WireBody(byte[][] transactions, InternalTransaction[] internalTransactions,
			WireBlockSignature[] blockSignatures, long selfParentIndex, long otherParentCreatorID,
			long otherParentIndex, long creatorID, long index) {
		super();
		Transactions = transactions;
		InternalTransactions = internalTransactions;
		BlockSignatures = blockSignatures;
		SelfParentIndex = selfParentIndex;
		OtherParentCreatorID = otherParentCreatorID;
		OtherParentIndex = otherParentIndex;
		CreatorID = creatorID;
		Index = index;
	}

	public byte[][] getTransactions() {
		return Transactions;
	}

	public void setTransactions(byte[][] transactions) {
		Transactions = transactions;
	}

	public InternalTransaction[] getInternalTransactions() {
		return InternalTransactions;
	}

	public void setInternalTransactions(InternalTransaction[] internalTransactions) {
		InternalTransactions = internalTransactions;
	}

	public WireBlockSignature[] getBlockSignatures() {
		return BlockSignatures;
	}

	public void setBlockSignatures(WireBlockSignature[] blockSignatures) {
		BlockSignatures = blockSignatures;
	}

	public long getSelfParentIndex() {
		return SelfParentIndex;
	}

	public void setSelfParentIndex(long selfParentIndex) {
		SelfParentIndex = selfParentIndex;
	}

	public long getOtherParentCreatorID() {
		return OtherParentCreatorID;
	}

	public void setOtherParentCreatorID(long otherParentCreatorID) {
		OtherParentCreatorID = otherParentCreatorID;
	}

	public long getOtherParentIndex() {
		return OtherParentIndex;
	}

	public void setOtherParentIndex(long otherParentIndex) {
		OtherParentIndex = otherParentIndex;
	}

	public long getCreatorID() {
		return CreatorID;
	}

	public void setCreatorID(long creatorID) {
		CreatorID = creatorID;
	}

	public long getIndex() {
		return Index;
	}

	public void setIndex(long index) {
		Index = index;
	}
}