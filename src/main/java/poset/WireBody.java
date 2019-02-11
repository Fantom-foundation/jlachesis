package poset;

import java.util.Arrays;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(BlockSignatures);
		result = prime * result + (int) (CreatorID ^ (CreatorID >>> 32));
		result = prime * result + (int) (Index ^ (Index >>> 32));
		result = prime * result + Arrays.hashCode(InternalTransactions);
		result = prime * result + (int) (OtherParentCreatorID ^ (OtherParentCreatorID >>> 32));
		result = prime * result + (int) (OtherParentIndex ^ (OtherParentIndex >>> 32));
		result = prime * result + (int) (SelfParentIndex ^ (SelfParentIndex >>> 32));
		result = prime * result + Arrays.deepHashCode(Transactions);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WireBody other = (WireBody) obj;
		if (!Arrays.equals(BlockSignatures, other.BlockSignatures))
			return false;
		if (CreatorID != other.CreatorID)
			return false;
		if (Index != other.Index)
			return false;
		if (!Arrays.equals(InternalTransactions, other.InternalTransactions))
			return false;
		if (OtherParentCreatorID != other.OtherParentCreatorID)
			return false;
		if (OtherParentIndex != other.OtherParentIndex)
			return false;
		if (SelfParentIndex != other.SelfParentIndex)
			return false;
		if (!Arrays.deepEquals(Transactions, other.Transactions))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WireBody [Transactions=");
		builder.append(Arrays.toString(Transactions));
		builder.append(", InternalTransactions=");
		builder.append(Arrays.toString(InternalTransactions));
		builder.append(", BlockSignatures=");
		builder.append(Arrays.toString(BlockSignatures));
		builder.append(", SelfParentIndex=");
		builder.append(SelfParentIndex);
		builder.append(", OtherParentCreatorID=");
		builder.append(OtherParentCreatorID);
		builder.append(", OtherParentIndex=");
		builder.append(OtherParentIndex);
		builder.append(", CreatorID=");
		builder.append(CreatorID);
		builder.append(", Index=");
		builder.append(Index);
		builder.append("]");
		return builder.toString();
	}
}