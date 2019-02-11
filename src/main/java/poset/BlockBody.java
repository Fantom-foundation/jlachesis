package poset;

import java.util.Arrays;

import com.google.protobuf.ByteString;

import common.IProto;
import common.RetResult;
import common.error;
import crypto.hash;

public class BlockBody {
	long Index;
	long RoundReceived;
	byte[][] Transactions;

	public BlockBody()
	{
		Index = -1;
		RoundReceived= -1;
		Transactions= null;
	}

	public BlockBody(long index, long roundReceived, byte[][] transactions) {
		super();
		Index = index;
		RoundReceived = roundReceived;
		Transactions = transactions;
	}

	public long GetIndex() {
		return this.Index;
	}

	public long GetRoundReceived() {
		return this.RoundReceived;
	}

	public byte[][] GetTransactions() {
		return this.Transactions;
	}

	public IProto<BlockBody, poset.proto.BlockBody> marshaller() {
		return new IProto<BlockBody, poset.proto.BlockBody>() {
			@Override
			public poset.proto.BlockBody toProto() {
				poset.proto.BlockBody.Builder builder = poset.proto.BlockBody.newBuilder();
				builder.setIndex(Index).setRoundReceived(RoundReceived);
				if (Transactions != null) {
					Arrays.asList(Transactions).forEach(transaction -> {
						builder.addTransactions(ByteString.copyFrom(transaction));
					});
				}
				return builder.build();
			}

			@Override
			public void fromProto(poset.proto.BlockBody pBlock) {
				Index = pBlock.getIndex();
				RoundReceived = pBlock.getRoundReceived();
				int transactionsCount = pBlock.getTransactionsCount();
				Transactions = new byte[][]{};
				if (transactionsCount > 0) {
					Transactions = new byte[transactionsCount][];
					for (int i = 0; i < transactionsCount; ++i) {
						Transactions[i] = pBlock.getTransactions(i).toByteArray();
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
		result = prime * result + (int) (Index ^ (Index >>> 32));
		result = prime * result + (int) (RoundReceived ^ (RoundReceived >>> 32));
		result = prime * result + Arrays.deepHashCode(Transactions);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BlockBody [Index=").append(Index).append(", RoundReceived=").append(RoundReceived)
				.append(", Transactions=").append(Arrays.toString(Transactions)).append("]");
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
		if (Index != other.Index)
			return false;
		if (RoundReceived != other.RoundReceived)
			return false;
		if (!Arrays.deepEquals(Transactions, other.Transactions))
			return false;
		return true;
	}

	public RetResult<byte[]> Hash() {
		RetResult<byte[]> marshal = marshaller().protoMarshal();

		byte[] hashBytes = marshal.result;
		error err = marshal.err;
		if (err != null) {
			return new RetResult<byte[]>(null, err);
		}
		return new RetResult<byte[]>(hash.SHA256(hashBytes), null);
	}
}
