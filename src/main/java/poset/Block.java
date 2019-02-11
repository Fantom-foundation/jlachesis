package poset;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.ByteString;

import autils.Appender;
import common.IProto;
import common.RetResult;
import common.RetResult3;
import common.error;

public class Block {
	private BlockBody Body;
	private Map<String,String> Signatures;
	private byte[] Hash;
	private String Hex;
	private byte[] StateHash;
	private byte[] FrameHash;

	public Block() {
		Body = null;
		Signatures = null;
		Hash = null;
		Hex = null;
		StateHash = null;
		FrameHash = null;
	}

	public Block(long blockIndex, long roundReceived, byte[] frameHash, byte[][] txs)  {
		this.Body = new BlockBody (blockIndex, roundReceived, txs);
		this.FrameHash = frameHash;
		this.Signatures = new HashMap<String,String>();
	}

	public void Reset() {
		Body = null;
		Signatures.clear();
		Hash = null;
		Hex = null;
		StateHash = null;
		FrameHash = null;
	}


	public BlockBody GetBody() {
		if (this.Body != null) {
			return this.Body;
		}
		return null;
	}

	public Map<String,String> GetSignatures() {
		if (this.Signatures != null) {
			return this.Signatures;
		}
		return null;
	}

	public byte[] GetHash() {
		if (this.Hash != null) {
			return this.Hash;
		}
		return null;
	}

	public String GetHex() {
		if (this.Hex != null) {
			return this.Hex;
		}
		return "";
	}

	public byte[] GetStateHash() {
		if (this.getStateHash() != null) {
			return this.getStateHash();
		}
		return null;
	}

	public byte[] GetFrameHash() {
		if (this.FrameHash != null) {
			return this.FrameHash;
		}
		return null;
	}

	public static RetResult<Block> NewBlockFromFrame(long blockIndex, Frame frame) {
		RetResult<byte[]> hash2 = frame.Hash();
		byte[] frameHash = hash2.result;
		error err = hash2.err;
		if (err != null) {
			return new RetResult<Block>(null, err);
		}

		byte[][] transactions = null;
		for (int i = 0; i< frame.Events.length; ++i) {
			EventMessage e = frame.Events[i];
//			transactions = append(transactions, e.Body.Transactions...)
			transactions = Appender.append(transactions, e.Body.Transactions);
		}

		return new RetResult<Block>(new Block(blockIndex, frame.Round, frameHash, transactions), null);
	}

//	public boolean equals(Block that) {
//		return this.Body.equals(that.Body) &&
//			this.Signatures.equals(that.Signatures) &&
//			Utils.bytesEquals(this.Hash, that.Hash) &&
//			this.Hex.equals(that.Hex);
//	}

	public long Index() {
		return Body.Index;
	}

	public byte[][] Transactions() {
		return Body.Transactions;
	}

	public long RoundReceived() {
		return Body.RoundReceived;
	}

	public BlockSignature[] GetBlockSignatures()  {
		BlockSignature[] res = new BlockSignature[Signatures.size()];
		int i = 0;
		for (String val : Signatures.keySet()) {
			String sig = Signatures.get(val);
			byte[] validatorBytes = crypto.Utils.decodeString(val.substring(2, val.length())).result;
			res[i] = new BlockSignature (
				validatorBytes,
				Index(),
				sig
			);
			i++;
		}
		return res;
	}

	public RetResult<BlockSignature> GetSignature(String validator) {
		String sig = Signatures.get(validator);
		BlockSignature res = null;
		if (sig == null) {
			return new RetResult<BlockSignature>(res, error.Errorf("signature not found"));
		}

		byte[] validatorBytes = crypto.Utils.decodeString(validator.substring(2, validator.length())).result;
		return new RetResult<BlockSignature>(
				new BlockSignature(validatorBytes, Index(), sig), null);
	}

	public void AppendTransactions(byte[][] txs) {
		Body.Transactions = Appender.append(Body.Transactions, txs);
	}

	public IProto<Block, poset.proto.Block> marshaller() {
		return new IProto<Block, poset.proto.Block>() {
			@Override
			public poset.proto.Block toProto() {
				poset.proto.Block.Builder builder = poset.proto.Block.newBuilder();
				if (Body != null) {
					poset.proto.BlockBody pBlockBody = Body.marshaller().toProto();
					builder.setBody(pBlockBody);
				}
				if (Signatures != null) {
					builder.clearSignatures().putAllSignatures(Signatures);
				}
				if (Hash != null) {
					builder.setHash(ByteString.copyFrom(Hash));
				}
				if (Hex != null) {
					builder.setHex(Hex);
				}
				if (StateHash != null) {
					builder.setStateHash(ByteString.copyFrom(StateHash));
				}
				if (FrameHash != null) {
					builder.setFrameHash(ByteString.copyFrom(FrameHash));
				}
				return builder.build();
			}

			@Override
			public void fromProto(poset.proto.Block pBlock) {
				poset.proto.BlockBody pBody = pBlock.getBody();
				Body = null;
				if (pBody != null) {
					Body = new BlockBody();
					Body.marshaller().fromProto(pBody);
				}

				Signatures = pBlock.getSignaturesMap();
				Hash = pBlock.getHash().toByteArray();
				Hex = pBlock.getHex();
				StateHash = pBlock.getStateHash().toByteArray();
				FrameHash = pBlock.getFrameHash().toByteArray();
			}

			@Override
			public com.google.protobuf.Parser<poset.proto.Block> parser() {
				return poset.proto.Block.parser();
			}
		};
	}

	public RetResult<BlockSignature> Sign(KeyPair keyPair) {
		RetResult<byte[]> hash2 = Body.Hash();
		byte[] signBytes = hash2.result;
		error err = hash2.err;
		BlockSignature bs = null;
		if (err != null) {
			return new RetResult<BlockSignature>(bs, err);
		}
		RetResult3<BigInteger, BigInteger> sign = crypto.Utils.Sign(keyPair.getPrivate(), signBytes);
		BigInteger R = sign.result1;
		BigInteger S = sign.result2;
		err = sign.err;
		if (err != null) {
			return new RetResult<BlockSignature>(bs, err);
		}

		bs = new BlockSignature(
			crypto.Utils.FromECDSAPub(keyPair.getPublic()),
			Index(),
			crypto.Utils.encodeSignature(R, S));

		return new RetResult<BlockSignature>(bs, null);
	}

	public error SetSignature(BlockSignature bs) {
		Signatures.put(bs.ValidatorHex(), bs.Signature);
		return null;
	}

	public RetResult<Boolean> Verify(BlockSignature sig) {
		RetResult<byte[]> hash2 = Body.Hash();
		byte[] signBytes = hash2.result;
		error err = hash2.err;
		if (err != null) {
			return new RetResult<Boolean>(false, err);
		}

		PublicKey pubKey = crypto.Utils.ToECDSAPub(sig.Validator);

		RetResult3<BigInteger, BigInteger> decodeSignature = crypto.Utils.DecodeSignature(sig.Signature);
		BigInteger r = decodeSignature.result1;
		BigInteger s = decodeSignature.result2;
		err = decodeSignature.err;
		if (err != null) {
			return new RetResult<Boolean>(false, err);
		}

		return new RetResult<Boolean>(crypto.Utils.Verify(pubKey, signBytes, r, s), null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Body == null) ? 0 : Body.hashCode());
		result = prime * result + Arrays.hashCode(FrameHash);
		result = prime * result + Arrays.hashCode(Hash);
		result = prime * result + ((Hex == null) ? 0 : Hex.hashCode());
		result = prime * result + ((Signatures == null) ? 0 : Signatures.hashCode());
		result = prime * result + Arrays.hashCode(StateHash);
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
		Block other = (Block) obj;
		if (Body == null) {
			if (other.Body != null)
				return false;
		} else if (!Body.equals(other.Body))
			return false;
		if (!Utils.protoBytesEquals(FrameHash, other.FrameHash))
			return false;
		if (!Utils.protoBytesEquals(Hash, other.Hash))
			return false;
		if (!Utils.protoStringEquals(Hex, other.Hex))
			return false;
		if (Signatures == null) {
			if (other.Signatures != null)
				return false;
		} else if (!Signatures.equals(other.Signatures))
			return false;
		if (!Utils.protoBytesEquals(StateHash, other.StateHash))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Block [Body=" + Body + ", Signatures=" + Signatures + ", Hash=" + Arrays.toString(Hash) + ", Hex=" + Hex
				+ ", StateHash=" + Arrays.toString(getStateHash()) + ", FrameHash=" + Arrays.toString(FrameHash) + "]";
	}

	public byte[] getStateHash() {
		return StateHash;
	}

	public void setStateHash(byte[] stateHash) {
		StateHash = stateHash;
	}
}
