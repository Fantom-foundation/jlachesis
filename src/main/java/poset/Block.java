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
import common.RResult;
import common.RResult3;
import common.error;

public class Block {
	private BlockBody body;
	private Map<String,String> signatures;
	private byte[] hash;
	private String hex;
	private byte[] stateHash;
	private byte[] frameHash;

	public Block() {
		body = null;
		signatures = null;
		hash = null;
		hex = null;
		stateHash = null;
		frameHash = null;
	}

	public Block(long blockIndex, long roundReceived, byte[] frameHash, byte[][] txs)  {
		this.body = new BlockBody (blockIndex, roundReceived, txs);
		this.frameHash = frameHash;
		this.signatures = new HashMap<String,String>();
	}

	public void reset() {
		body = null;
		signatures.clear();
		hash = null;
		hex = null;
		stateHash = null;
		frameHash = null;
	}


	public BlockBody getBody() {
		if (this.body != null) {
			return this.body;
		}
		return null;
	}

	public Map<String,String> getSignatures() {
		if (this.signatures != null) {
			return this.signatures;
		}
		return null;
	}

	public byte[] getHash() {
		if (this.hash != null) {
			return this.hash;
		}
		return null;
	}

	public String getHex() {
		if (this.hex != null) {
			return this.hex;
		}
		return "";
	}

	public byte[] stateHash() {
		if (this.getStateHash() != null) {
			return this.getStateHash();
		}
		return null;
	}

	public byte[] getFrameHash() {
		if (this.frameHash != null) {
			return this.frameHash;
		}
		return null;
	}

	public static RResult<Block> newBlockFromFrame(long blockIndex, Frame frame) {
		RResult<byte[]> hash2 = frame.Hash();
		byte[] frameHash = hash2.result;
		error err = hash2.err;
		if (err != null) {
			return new RResult<Block>(null, err);
		}

		byte[][] transactions = null;
		if (frame.Events != null) {
			for (int i = 0; i< frame.Events.length; ++i) {
				EventMessage e = frame.Events[i];
				transactions = Appender.append(transactions, e.Body.Transactions);
			}
		}

		return new RResult<Block>(new Block(blockIndex, frame.Round, frameHash, transactions), null);
	}

//	public boolean equals(Block that) {
//		return this.Body.equals(that.Body) &&
//			this.Signatures.equals(that.Signatures) &&
//			Utils.bytesEquals(this.Hash, that.Hash) &&
//			this.Hex.equals(that.Hex);
//	}

	public long Index() {
		return body.index;
	}

	public byte[][] transactions() {
		return body.transactions;
	}

	public long roundReceived() {
		return body.roundReceived;
	}

	public BlockSignature[] getBlockSignatures()  {
		BlockSignature[] res = new BlockSignature[signatures.size()];
		int i = 0;
		for (String val : signatures.keySet()) {
			String sig = signatures.get(val);
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

	public RResult<BlockSignature> getSignature(String validator) {
		String sig = signatures.get(validator);
		BlockSignature res = null;
		if (sig == null) {
			return new RResult<BlockSignature>(res, error.Errorf("signature not found"));
		}

		byte[] validatorBytes = crypto.Utils.decodeString(validator.substring(2, validator.length())).result;
		return new RResult<BlockSignature>(
				new BlockSignature(validatorBytes, Index(), sig), null);
	}

	public void appendTransactions(byte[][] txs) {
		body.transactions = Appender.append(body.transactions, txs);
	}

	public IProto<Block, poset.proto.Block> marshaller() {
		return new IProto<Block, poset.proto.Block>() {
			@Override
			public poset.proto.Block toProto() {
				poset.proto.Block.Builder builder = poset.proto.Block.newBuilder();
				if (body != null) {
					poset.proto.BlockBody pBlockBody = body.marshaller().toProto();
					builder.setBody(pBlockBody);
				}
				if (signatures != null) {
					builder.clearSignatures().putAllSignatures(signatures);
				}
				if (hash != null) {
					builder.setHash(ByteString.copyFrom(hash));
				}
				if (hex != null) {
					builder.setHex(hex);
				}
				if (stateHash != null) {
					builder.setStateHash(ByteString.copyFrom(stateHash));
				}
				if (frameHash != null) {
					builder.setFrameHash(ByteString.copyFrom(frameHash));
				}
				return builder.build();
			}

			@Override
			public void fromProto(poset.proto.Block pBlock) {
				poset.proto.BlockBody pBody = pBlock.getBody();
				body = null;
				if (pBody != null) {
					body = new BlockBody();
					body.marshaller().fromProto(pBody);
				}

				signatures = pBlock.getSignaturesMap();
				hash = pBlock.getHash().toByteArray();
				hex = pBlock.getHex();
				stateHash = pBlock.getStateHash().toByteArray();
				frameHash = pBlock.getFrameHash().toByteArray();
			}

			@Override
			public com.google.protobuf.Parser<poset.proto.Block> parser() {
				return poset.proto.Block.parser();
			}
		};
	}

	public RResult<BlockSignature> sign(KeyPair keyPair) {
		RResult<byte[]> hash2 = body.hash();
		byte[] signBytes = hash2.result;
		error err = hash2.err;
		BlockSignature bs = null;
		if (err != null) {
			return new RResult<BlockSignature>(bs, err);
		}
		RResult3<BigInteger, BigInteger> sign = crypto.Utils.Sign(keyPair.getPrivate(), signBytes);
		BigInteger R = sign.result1;
		BigInteger S = sign.result2;
		err = sign.err;
		if (err != null) {
			return new RResult<BlockSignature>(bs, err);
		}

		bs = new BlockSignature(
			crypto.Utils.FromECDSAPub(keyPair.getPublic()),
			Index(),
			crypto.Utils.encodeSignature(R, S));

		return new RResult<BlockSignature>(bs, null);
	}

	public error setSignature(BlockSignature bs) {
		signatures.put(bs.validatorHex(), bs.signature);
		return null;
	}

	public RResult<Boolean> verify(BlockSignature sig) {
		RResult<byte[]> hash2 = body.hash();
		byte[] signBytes = hash2.result;
		error err = hash2.err;
		if (err != null) {
			return new RResult<Boolean>(false, err);
		}

		PublicKey pubKey = crypto.Utils.ToECDSAPub(sig.validator);

		RResult3<BigInteger, BigInteger> decodeSignature = crypto.Utils.DecodeSignature(sig.signature);
		BigInteger r = decodeSignature.result1;
		BigInteger s = decodeSignature.result2;
		err = decodeSignature.err;
		if (err != null) {
			return new RResult<Boolean>(false, err);
		}

		return new RResult<Boolean>(crypto.Utils.Verify(pubKey, signBytes, r, s), null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + Arrays.hashCode(frameHash);
		result = prime * result + Arrays.hashCode(hash);
		result = prime * result + ((hex == null) ? 0 : hex.hashCode());
		result = prime * result + ((signatures == null) ? 0 : signatures.hashCode());
		result = prime * result + Arrays.hashCode(stateHash);
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
		if (body == null) {
			if (other.body != null)
				return false;
		} else if (!body.equals(other.body))
			return false;
		if (!Utils.protoBytesEquals(frameHash, other.frameHash))
			return false;
		if (!Utils.protoBytesEquals(hash, other.hash))
			return false;
		if (!Utils.protoStringEquals(hex, other.hex))
			return false;
		if (signatures == null) {
			if (other.signatures != null)
				return false;
		} else if (!signatures.equals(other.signatures))
			return false;
		if (!Utils.protoBytesEquals(stateHash, other.stateHash))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Block [Body=" + body + ", Signatures=" + signatures + ", Hash=" + Arrays.toString(hash) + ", Hex=" + hex
				+ ", StateHash=" + Arrays.toString(getStateHash()) + ", FrameHash=" + Arrays.toString(frameHash) + "]";
	}

	public byte[] getStateHash() {
		return stateHash;
	}

	public void setStateHash(byte[] stateHash) {
		this.stateHash = stateHash;
	}
}
