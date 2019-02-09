package poset;

import java.util.Arrays;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import common.IProto;
import common.RetResult;
import common.error;
import crypto.hash;

public class BlockBody {
	private poset.proto.BlockBody pBlockBody;
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
			public RetResult<byte[]> protoMarshal() {
				if (pBlockBody == null) {
					pBlockBody = toProto();
				}
				return new RetResult<>(pBlockBody.toByteArray(), null);
			}

			@Override
			public error protoUnmarshal(byte[] data) {
				try {
					pBlockBody = poset.proto.BlockBody.parseFrom(data);
				} catch (InvalidProtocolBufferException e) {
					return error.Errorf(e.getMessage());
				}
				fromProto(pBlockBody);
				return null;
			}
		};
	}

	public boolean equals(BlockBody that) {
		return this.Index == that.Index &&
			this.RoundReceived == that.RoundReceived &&
			Utils.ByteArraysEquals(this.Transactions, that.Transactions);
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
