package poset;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Map;

import com.google.protobuf.ByteString;
import com.google.protobuf.Parser;

import common.IProto;
import common.RetResult;
import common.RetResult3;
import common.error;
import node.FlagtableContainer;
import poset.proto.FlagTableWrapper.Builder;

public class Event implements FlagtableContainer {
	EventMessage message;

	//used for sorting
	long round;
	long lamportTimestamp;
	long roundReceived;
	String creator;
	byte[] hash;
	String hex;

	// NewEvent creates new block event.
	public Event(byte[][] transactions,
			InternalTransaction[] internalTransactions,
			BlockSignature[] blockSignatures,
			String[] parents, byte[] creator, long index,
			Map<String,Long> flagTable) {

		InternalTransaction[] internalTransactionPointers = null;
		if (internalTransactions != null) {
			internalTransactionPointers = new InternalTransaction[internalTransactions.length];
			for (int i = 0; i< internalTransactions.length; ++i) {
				internalTransactionPointers[i] = new InternalTransaction(internalTransactions[i]);
			}
		}

		BlockSignature[] blockSignaturePointers = null;
		if (blockSignaturePointers != null) {
			blockSignaturePointers = new BlockSignature[blockSignatures.length];
			for (int i = 0; i< blockSignatures.length; ++i) {
				blockSignaturePointers[i] = new BlockSignature (blockSignatures[i]);
			}
		}

		EventBody body = new EventBody(
			transactions,
			internalTransactionPointers,
			parents,
			creator,
			index,
			blockSignaturePointers
		);

		Builder builder = poset.proto.FlagTableWrapper.newBuilder();
		if (flagTable!= null) {
			builder.putAllBody(flagTable);
		}
		byte[] ft = builder.build().toByteArray();

		this.message = new EventMessage();
		this.message.Body = body;
		this.message.FlagTable = ft;

		// TODO I added these init
		this.round = -1;
		this.lamportTimestamp = -1;
		this.roundReceived = -1;
		this.creator = null;
		this.hash = null;
		this.hex = null;
	}

	public Event(EventMessage eventMessage) {
		// TBD add stub
		this.message = new EventMessage();
		this.message.Body = eventMessage.Body;
		this.message.FlagTable = eventMessage.FlagTable;

		// TODO I added these init
		this.round = -1;
		this.lamportTimestamp = -1;
		this.roundReceived = -1;
		this.creator = null;
		this.hash = null;
		this.hex = null;
	}

	public Event() {
		// TODO I added these init
		this.message = null;

		this.round = -1;
		this.lamportTimestamp = -1;
		this.roundReceived = -1;
		this.creator = null;
		this.hash = null;
		this.hex = null;
	}

	public Event(Event ev) {
		// TODO Auto-generated constructor stub
	}

	// Round returns round of event.
	public long GetRound() {
		if (round < 0) {
			return -1;
		}
		return round;
	}

	public String creator() {
		if (creator == null || creator.isEmpty()) {
			creator = crypto.Utils.toHexString(message.Body.Creator);
		}
		return creator;
	}

	public String selfParent() {
		return message.Body.Parents[0];
	}

	public String otherParent() {
		return message.Body.Parents[1];
	}

	public byte[][] transactions() {
		return message.Body.Transactions;
	}

	public long index() {
		return message.Body.Index;
	}

	public BlockSignature[] blockSignatures() {
		return message.Body.BlockSignatures;
	}

	//True if Event contains a payload or is the initial Event of its creator
	public boolean isLoaded() {
		if (message.Body.Index == 0) {
			return true;
		}

		boolean hasTransactions = message.Body.Transactions != null &&
			(message.Body.Transactions.length > 0 ||
					(message.Body.InternalTransactions != null && message.Body.InternalTransactions.length > 0));

		return hasTransactions;
	}

	//ecdsa sig
	public error sign(PrivateKey privKey) {
		RetResult<byte[]> hash2 = message.Body.Hash();
		byte[] signBytes = hash2.result;
		error err = hash2.err;
		if (err != null) {
			return err;
		}

		RetResult3<BigInteger, BigInteger> sign = crypto.Utils.Sign(privKey, signBytes);
		BigInteger R = sign.result1;
		BigInteger S = sign.result2;
		err = sign.err;
		if (err != null) {
			return err;
		}
		message.Signature = crypto.Utils.encodeSignature(R, S);
		return err;
	}

	public RetResult<Boolean> verify() {
		byte[] pubBytes = message.Body.Creator;
		PublicKey pubKey = crypto.Utils.ToECDSAPub(pubBytes);

		RetResult<byte[]> hash2 = message.Body.Hash();
		byte[] signBytes = hash2.result;
		error err = hash2.err;
		if (err != null) {
			return new RetResult<Boolean>(false, err);
		}

		RetResult3<BigInteger, BigInteger> decodeSignature = crypto.Utils.DecodeSignature(message.Signature);
		BigInteger r = decodeSignature.result1;
		BigInteger s = decodeSignature.result2;
		err = decodeSignature.err;
		if (err != null) {
			return new RetResult<Boolean>(false, err);
		}

		return new RetResult<Boolean>(crypto.Utils.Verify(pubKey, signBytes, r, s), null);
	}

	//sha256 hash of body
	public RetResult<byte[]> hash() {
		if (hash == null || hash.length == 0) {
			RetResult<byte[]> hash2 = message.Body.Hash();
			byte[] hash = hash2.result;
			error err = hash2.err;
			if (err != null) {
				return new RetResult<byte[]>(null, err);
			}
			this.hash = hash;
		}
		return new RetResult<byte[]>(this.hash, null);
	}

	public String hex() {
		if (hex == null || hex.isEmpty()) {
			byte[] hash = hash().result;
//			hex = String.format("0x%X", hash);
			hex = crypto.Utils.toHexString(hash);
		}
		return hex;
	}

	public void setRound(long r) {
		round = r;
	}

	public void setLamportTimestamp(long t) {
		lamportTimestamp = t;
	}

	public void setRoundReceived(long rr) {
		roundReceived = rr;
	}

	public void setWireInfo(long selfParentIndex, long otherParentCreatorID, long otherParentIndex,
		long creatorID) {
		message.SelfParentIndex = selfParentIndex;
		message.OtherParentCreatorID = otherParentCreatorID;
		message.OtherParentIndex = otherParentIndex;
		message.CreatorID = creatorID;
	}

	public WireBlockSignature[] WireBlockSignatures()  {
		if (message.Body.BlockSignatures != null){
			WireBlockSignature[] wireSignatures = new WireBlockSignature[message.Body.BlockSignatures.length];
			for (int i = 0; i < message.Body.BlockSignatures.length; ++i) {
				wireSignatures[i] = message.Body.BlockSignatures[i].toWire();
			}

			return wireSignatures;
		}
		return null;
	}

	public WireEvent toWire()  {
		InternalTransaction[] transactions = new InternalTransaction[message.Body.InternalTransactions.length];

		for (int i = 0; i <message.Body.InternalTransactions.length; ++i) {
			transactions[i] = message.Body.InternalTransactions[i];
		}

		WireBody wireBody = new WireBody(
				message.Body.Transactions,
				transactions,
				WireBlockSignatures(),
				message.SelfParentIndex,
				message.OtherParentCreatorID,
				message.OtherParentIndex,
				message.CreatorID,
				message.Body.Index);

		return new WireEvent(
			wireBody,
			message.Signature,
			message.FlagTable,
			message.WitnessProof
		);
	}

	// ReplaceFlagTable replaces flag tabl
	public error replaceFlagTable(Map<String,Long> flagTable) {
		FlagTableWrapper ftw = new FlagTableWrapper(flagTable);

		RetResult<byte[]> byteArrayCall = ftw.marshaller().protoMarshal();
		message.FlagTable = byteArrayCall.result;
		error err = byteArrayCall.err;
		return err;
	}

	// GetFlagTable returns the flag tabl
	public RetResult<Map<String,Long>> getFlagTable() {
		FlagTableWrapper flagTable = new FlagTableWrapper();
		error err = flagTable.marshaller().protoUnmarshal(message.FlagTable);
		return new RetResult<Map<String,Long>>(flagTable.Body, err);
	}

	/**
	 * MergeFlagTable returns merged flag table object.
	 * @param dst
	 * @return
	 */
	public RetResult<Map<String,Long>> mergeFlagTable(Map<String,Long> dst) {
		FlagTableWrapper src = new FlagTableWrapper();
		error err = src.marshaller().protoUnmarshal(message.FlagTable);
		if (err != null) {
			return new RetResult<Map<String,Long>>(null, err);
		}

		src.Body.putAll(dst);
		return new RetResult<Map<String,Long>>(src.Body, err);
	}


	public IProto<Event, poset.proto.Event> marshaller() {
		return new IProto<Event, poset.proto.Event>() {
			@Override
			public poset.proto.Event toProto() {
				poset.proto.Event.Builder builder = poset.proto.Event.newBuilder();
				if (message != null) {
					builder.setMessage(message.marshaller().toProto());
				}
				builder.setRound(round)
				.setLamportTimestamp(lamportTimestamp)
				.setRoundReceived(roundReceived);
				if (creator != null) {
					builder.setCreator(creator);
				}
				if (hex != null) {
					builder.setHex(hex);
				}
				if (hash != null) {
					builder.setHash(ByteString.copyFrom(hash));
				}
				return builder.build();
			}

			@Override
			public void fromProto(poset.proto.Event proto) {
				poset.proto.EventMessage msg = proto.getMessage();
				message = null;
				if (msg != null) {
					message = new EventMessage();
					message.marshaller().fromProto(msg);
				}

				round = proto.getRound();
				lamportTimestamp = proto.getLamportTimestamp();
				roundReceived = proto.getRoundReceived();
				creator = proto.getCreator();
				hash = proto.getHash().toByteArray();
				hex = proto.getHex();
			}

			@Override
			public Parser<poset.proto.Event> parser() {
				return poset.proto.Event.parser();
			}
		};
	}

	public long creatorID() {
		return message.CreatorID;
	}

	public long otherParentCreatorID() {
		return message.OtherParentCreatorID;
	}

	public EventMessage getMessage() {
		return message;
	}

	public long getRound() {
		return round;
	}

	public long getLamportTimestamp() {
		return lamportTimestamp;
	}

	public long getRoundReceived() {
		return roundReceived;
	}

	public String getCreator() {
		return creator;
	}

	public byte[] getHash() {
		return hash;
	}

	public String getHex() {
		return hex;
	}

	public static String rootSelfParent(long participantID) {
		return String.format("Root%d", participantID);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((creator == null) ? 0 : creator.hashCode());
		result = prime * result + Arrays.hashCode(hash);
		result = prime * result + ((hex == null) ? 0 : hex.hashCode());
		result = prime * result + (int) (lamportTimestamp ^ (lamportTimestamp >>> 32));
		result = prime * result + (int) (round ^ (round >>> 32));
		result = prime * result + (int) (roundReceived ^ (roundReceived >>> 32));
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
		Event other = (Event) obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (creator == null) {
			if (other.creator != null)
				return false;
		} else if (!creator.equals(other.creator))
			return false;
		if (!Arrays.equals(hash, other.hash))
			return false;
		if (hex == null) {
			if (other.hex != null)
				return false;
		} else if (!hex.equals(other.hex))
			return false;
		if (lamportTimestamp != other.lamportTimestamp)
			return false;
		if (round != other.round)
			return false;
		if (roundReceived != other.roundReceived)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Event [Message=").append(message).append(", round=").append(round).append(", lamportTimestamp=")
				.append(lamportTimestamp).append(", roundReceived=").append(roundReceived).append(", creator=")
				.append(creator).append(", hash=").append(Arrays.toString(hash)).append(", hex=").append(hex)
				.append("]");
		return builder.toString();
	}

}