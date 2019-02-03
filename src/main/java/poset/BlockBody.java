package poset;

import java.util.Arrays;

import com.google.protobuf.ByteString;

import common.RetResult;
import common.error;
import crypto.hash;
import poset.proto.BlockBody.Builder;

public class BlockBody {
	long Index;
	long RoundReceived;
	byte[][] Transactions;

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

//	//json encoding of body only
	public RetResult<byte[]> ProtoMarshal() {
//		var bf proto.Buffer
//		bf.SetDeterministic(true)
//		if err := bf.Marshal(bb); err != null {
//			return null, err
//		}
//		return bf.Bytes(), null


		Builder builder = poset.proto.BlockBody.newBuilder().setIndex(Index).setRoundReceived(RoundReceived);
		Arrays.stream(this.Transactions)
			.forEachOrdered(transaction -> builder.addTransactions(ByteString.copyFrom(transaction)));
		poset.proto.BlockBody bb = builder.build();

		return new RetResult<>(bb.toByteArray(), null);
	}
//
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
