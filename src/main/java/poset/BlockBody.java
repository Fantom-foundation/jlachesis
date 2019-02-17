package poset;

import java.util.Arrays;

import com.google.protobuf.ByteString;

import common.IProto;
import common.RetResult;
import common.error;
import crypto.hash;

public class BlockBody {
	long index;
	long roundReceived;
	byte[][] transactions;

	public BlockBody()
	{
		index = -1;
		roundReceived= -1;
		transactions= null;
	}

	public BlockBody(long index, long roundReceived, byte[][] transactions) {
		super();
		this.index = index;
		this.roundReceived = roundReceived;
		this.transactions = transactions;
	}

	public long getIndex() {
		return this.index;
	}

	public long getRoundReceived() {
		return this.roundReceived;
	}

	public byte[][] getTransactions() {
		return this.transactions;
	}

	public RetResult<byte[]> hash() {
		RetResult<byte[]> marshal = marshaller().protoMarshal();

		byte[] hashBytes = marshal.result;
		error err = marshal.err;
		if (err != null) {
			return new RetResult<byte[]>(null, err);
		}
		return new RetResult<byte[]>(hash.SHA256(hashBytes), null);
	}

	public IProto<BlockBody, poset.proto.BlockBody> marshaller() {
		return new IProto<BlockBody, poset.proto.BlockBody>() {
			@Override
			public poset.proto.BlockBody toProto() {
				poset.proto.BlockBody.Builder builder = poset.proto.BlockBody.newBuilder();
				builder.setIndex(index).setRoundReceived(roundReceived);
				if (transactions != null) {
					Arrays.asList(transactions).forEach(transaction -> {
						builder.addTransactions(ByteString.copyFrom(transaction));
					});
				}
				return builder.build();
			}

			@Override
			public void fromProto(poset.proto.BlockBody pBlock) {
				index = pBlock.getIndex();
				roundReceived = pBlock.getRoundReceived();
				int transactionsCount = pBlock.getTransactionsCount();
				transactions = new byte[][]{};
				if (transactionsCount > 0) {
					transactions = new byte[transactionsCount][];
					for (int i = 0; i < transactionsCount; ++i) {
						transactions[i] = pBlock.getTransactions(i).toByteArray();
					}
				}
			}

			@Override
			public com.google.protobuf.Parser<poset.proto.BlockBody> parser() {
				return poset.proto.BlockBody.parser();
			}
		};
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (index ^ (index >>> 32));
		result = prime * result + (int) (roundReceived ^ (roundReceived >>> 32));
		result = prime * result + Arrays.deepHashCode(transactions);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BlockBody [Index=").append(index).append(", RoundReceived=").append(roundReceived)
				.append(", Transactions=").append(Arrays.toString(transactions)).append("]");
		return builder.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BlockBody other = (BlockBody) obj;
		if (index != other.index)
			return false;
		if (roundReceived != other.roundReceived)
			return false;
		if (!Arrays.deepEquals(transactions, other.transactions))
			return false;
		return true;
	}
}
