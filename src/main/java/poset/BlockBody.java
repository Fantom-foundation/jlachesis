package poset;

import java.util.Arrays;
import java.util.List;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import common.IProto;
import common.RetResult;
import common.error;
import crypto.hash;
import poset.proto.BlockBody.Builder;

public class BlockBody {
	long Index;
	long RoundReceived;
	byte[][] Transactions;

	public BlockBody()
	{

	}

	public BlockBody(long index, long roundReceived, byte[][] transactions) {
		super();
		Index = index;
		RoundReceived = roundReceived;
		Transactions = transactions;
	}

	public void Reset() {
		Index = 0;
		RoundReceived= 0;
		Transactions= null;
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

	public poset.proto.BlockBody toProtoc(){
		Builder builder = poset.proto.BlockBody.newBuilder().setIndex(Index).setRoundReceived(RoundReceived);
		Arrays.asList(this.Transactions)
			.forEach(transaction -> builder.addTransactions(ByteString.copyFrom(transaction)));
		poset.proto.BlockBody bb = builder.build();
		return bb;
	}

	public RetResult<poset.proto.BlockBody> readProtoc(byte[] data) {
		try {
			poset.proto.BlockBody bb = poset.proto.BlockBody.parseFrom(data);
			return new RetResult<poset.proto.BlockBody>(bb, null);
		} catch (InvalidProtocolBufferException e) {
			return new RetResult<poset.proto.BlockBody>(null, error.Errorf(e.getMessage()));
		}
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
			public RetResult<poset.proto.BlockBody> parseFrom(byte[] data) {
				try {
					poset.proto.BlockBody block = poset.proto.BlockBody.parseFrom(data);
					return new RetResult<>(block, null);
				} catch (InvalidProtocolBufferException e) {
					return new RetResult<>(null, error.Errorf(e.getMessage()));
				}
			}

			@Override
			public RetResult<byte[]> protoMarshal() {
				poset.proto.BlockBody protoc = toProto();
				return new RetResult<>(protoc.toByteArray(), null);
			}

			@Override
			public error protoUnmarshal(byte[] data) {
				RetResult<poset.proto.BlockBody> protBlock = parseFrom(data);
				error err = protBlock.err;
				if (err != null) {
					return err;
				}
				poset.proto.BlockBody pBlock = protBlock.result;
				fromProto(pBlock);
				return null;
			}
		};
	}

	public RetResult<byte[]> ProtoMarshal() {
		poset.proto.BlockBody bb = toProtoc();
		return new RetResult<>(bb.toByteArray(), null);
	}


//	public error ProtoUnmarshal(byte[] data) {
//		// return proto.Unmarshal(data, bb)
//
//		// TBD
//		return null;
//	}


	public boolean equals(BlockBody that) {
		return this.Index == that.Index &&
			this.RoundReceived == that.RoundReceived &&
			Utils.ByteArraysEquals(this.Transactions, that.Transactions);
	}

	public RetResult<byte[]> Hash() {
		RetResult<byte[]> marshal = ProtoMarshal();

		byte[] hashBytes = marshal.result;
		error err = marshal.err;
		if (err != null) {
			return new RetResult<byte[]>(null, err);
		}
		return new RetResult<byte[]>(hash.SHA256(hashBytes), null);
	}
}
