package poset;

import java.util.Arrays;

import com.google.protobuf.ByteString;
import com.google.protobuf.Parser;

import common.IProto;
import common.RetResult;
import common.error;
import crypto.hash;

public class EventBody {
	byte[][] Transactions; //  `protobuf:"bytes,1,rep,name=Transactions,json=transactions,proto3" json:"Transactions,omitempty"`
	InternalTransaction[] InternalTransactions; // `protobuf:"bytes,2,rep,name=InternalTransactions,json=internalTransactions" json:"InternalTransactions,omitempty"`
	String[] Parents ; //  `protobuf:"bytes,3,rep,name=Parents,json=parents" json:"Parents,omitempty"`
	byte[] Creator; //  `protobuf:"bytes,4,opt,name=Creator,json=creator,proto3" json:"Creator,omitempty"`
	long Index; // `protobuf:"varint,5,opt,name=Index,json=index" json:"Index,omitempty"`
	BlockSignature [] BlockSignatures; //     `protobuf:"bytes,6,rep,name=BlockSignatures,json=blockSignatures" json:"BlockSignatures,omitempty"`

	public EventBody(byte[][] transactions, InternalTransaction[] internalTransactions, String[] parents,
			byte[] creator, long index, BlockSignature[] blockSignatures) {
		super();
		Transactions = transactions;
		InternalTransactions = internalTransactions;
		Parents = parents;
		Creator = creator;
		Index = index;
		BlockSignatures = blockSignatures;
	}

	public EventBody() {
		super();
		Transactions = null;
		InternalTransactions = null;
		Parents = null;
		Creator = null;
		Index = -1;
		BlockSignatures = null;
	}

	public void Reset()                    {
		Transactions = null;
		InternalTransactions = null;
		Parents = null;
		Creator = null;
		Index = -1;
		BlockSignatures = null;
	}

	public byte[][] GetTransactions() {
		return Transactions;
	}

	public InternalTransaction[] GetInternalTransactions() {
		return InternalTransactions;
	}

	public String[] GetParents() {
		return Parents;
	}

	public byte[] GetCreator() {
		return Creator;
	}

	public long GetIndex() {
		return Index;
	}

	public BlockSignature[] GetBlockSignatures() {
		return BlockSignatures;
	}

	public IProto<EventBody, poset.proto.EventBody> marshaller() {
		return new IProto<EventBody, poset.proto.EventBody>() {
			@Override
			public poset.proto.EventBody toProto() {
				poset.proto.EventBody.Builder builder = poset.proto.EventBody.newBuilder();

				if (Transactions != null) {
					Arrays.asList(Transactions).forEach(transaction -> {
						builder.addTransactions(ByteString.copyFrom(transaction));
					});
				}
				if (InternalTransactions != null) {
					Arrays.asList(InternalTransactions).forEach(InternalTransaction -> {
						builder.addInternalTransactions(InternalTransaction.marshaller().toProto());
					});
				}
				if (Parents != null) {
					Arrays.asList(Parents).forEach(parent -> {
						if (parent != null) {
							builder.addParents(parent);
						}
					});
				}
				if (Creator != null) {
					builder.setCreator(ByteString.copyFrom(Creator));
				}
				builder.setIndex(Index);
				if (BlockSignatures != null) {
					Arrays.asList(BlockSignatures).forEach(BlockSignature -> {
						builder.addBlockSignatures(BlockSignature.marshaller().toProto());
					});
				}
				return builder.build();
			}

			@Override
			public void fromProto(poset.proto.EventBody proto) {
				Transactions = toArray(proto.getTransactionsList());

				int intranCount = proto.getInternalTransactionsCount();
				InternalTransactions = null;
				if (intranCount >= 0) {
					InternalTransactions = new InternalTransaction[intranCount];
					for (int i =0; i < intranCount; ++i) {
						InternalTransactions[i] = new InternalTransaction();
						InternalTransactions[i].marshaller().fromProto(proto.getInternalTransactions(i));
					}
				}

				Parents = proto.getParentsList().toArray(new String[0]);
				Creator = proto.getCreator().toByteArray();
				Index = proto.getIndex();

				int bsCount = proto.getBlockSignaturesCount();
				BlockSignatures = null;
				if (bsCount >= 0) {
					BlockSignatures = new BlockSignature[bsCount];
					for (int i =0; i < bsCount; ++i) {
						BlockSignatures[i] = new BlockSignature();
						BlockSignatures[i].marshaller().fromProto(proto.getBlockSignatures(i));
					}
				}
			}

			@Override
			public Parser<poset.proto.EventBody> parser() {
				return poset.proto.EventBody.parser();
			}
		};
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EventBody [Transactions=");
		builder.append(Arrays.toString(Transactions));
		builder.append(", InternalTransactions=");
		builder.append(Arrays.toString(InternalTransactions));
		builder.append(", Parents=");
		builder.append(Arrays.toString(Parents));
		builder.append(", Creator=");
		builder.append(Arrays.toString(Creator));
		builder.append(", Index=");
		builder.append(Index);
		builder.append(", BlockSignatures=");
		builder.append(Arrays.toString(BlockSignatures));
		builder.append("]");
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
		EventBody other = (EventBody) obj;
		if (!Utils.protoEquals(BlockSignatures, other.BlockSignatures))
			return false;
		if (!Utils.protoBytesEquals(Creator, other.Creator))
			return false;
		if (Index != other.Index)
			return false;
		if (!Utils.protoEquals(InternalTransactions, other.InternalTransactions))
			return false;
		if (!Utils.protoEquals(Parents, other.Parents))
			return false;
		if (!Arrays.deepEquals(Transactions, other.Transactions))
			return false;
		return true;
	}

	public RetResult<byte[]> Hash() {
		RetResult<byte[]> protoMarshal = marshaller().protoMarshal();
		byte[] hashBytes = protoMarshal.result;
		error err = protoMarshal.err;
		if (err != null) {
			return new RetResult<byte[]>(null, err);
		}
		return new RetResult<byte[]>(hash.SHA256(hashBytes), null);
	}
}