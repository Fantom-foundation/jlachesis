package poset;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import autils.Appender;
import common.RetResult;
import common.RetResult3;
import common.error;

public class Block {
	BlockBody Body;
	Map<String,String> Signatures;
	byte[] Hash;
	String Hex;
	private byte[] StateHash;
	byte[] FrameHash;

	public Block() {

	}

	public void Reset() {
		Body = null;
		Signatures.clear();
		Hash = null;
		Hex = null;
		setStateHash(null);
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


//	public String String(){
//		//return proto.CompactTextString(m);
//		return Body + ",sign:" + Signatures + Hash + "," + Hex + "," + StateHash + "," + FrameHash;
//	}


	public boolean equals(Block that) {
		return this.Body.equals(that.Body) &&
			MapStringsEquals(this.Signatures, that.Signatures) &&
			Utils.BytesEquals(this.Hash, that.Hash) &&
			this.Hex == that.Hex;
	}


	public boolean MapStringsEquals(Map<String,String> thisMap, Map<String,String> thatMap) {
		if (thisMap.size() != thatMap.size()) {
			return false;
		}
		for (Entry entry : thisMap.entrySet()) {
			String v1 = thatMap.get(entry.getKey());
			if (v1 == null || entry.getValue() != v1) {
				return false;
			}
		}
		return true;
	}

	//------------------------------------------------------------------------------

//	public static RetResult<Block> Block(long blockIndex, Frame frame) {
//		Block block = new Block();
//		RetResult<byte[]> hash2 = frame.Hash();
//		byte[] frameHash = hash2.result;
//		error err = hash2.err;
//		if (err != null) {
//			return new RetResult<Block>(new Block(), err);
//		}
//		byte[][] transactions;
//		for (int i = 0; i< frame.Events.length; ++i) {
//			EventMessage e = frame.Events[i];
//			transactions[i] = e.Body.Transactions;
//		}
//		return new RetResult<Block>(this, err);
//	}


	public static RetResult<Block> NewBlockFromFrame(long blockIndex, Frame frame) {
		RetResult<byte[]> hash2 = frame.Hash();
		byte[] frameHash = hash2.result;
		error err = hash2.err;
		if (err != null) {
			return new RetResult<Block>(new Block(), err);
		}

		byte[][] transactions = null;
		for (int i = 0; i< frame.Events.length; ++i) {
			EventMessage e = frame.Events[i];
//			transactions = append(transactions, e.Body.Transactions...)
			transactions = Appender.append(transactions, e.Body.Transactions);
		}

		return new RetResult<Block>(new Block(blockIndex, frame.Round, frameHash, transactions), null);
	}


	public Block(long blockIndex, long roundReceived, byte[] frameHash, byte[][] txs)  {
		this.Body = new BlockBody (blockIndex, roundReceived, txs);
		this.FrameHash = frameHash;
		this.Signatures = new HashMap<String,String>();
	}

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

	public RetResult<byte[]> ProtoMarshal() {
//		var bf proto.Buffer;
//		bf.SetDeterministic(true);
//		if err := bf.Marshal(b); err != null {
//			return null, err;
//		}
//		return bf.Bytes(), null;

		// TBD

		return null;
	}

//	public RetResult<PBlock.Block> ProtoUnmarshal(byte[] data) {
//		PBlock.Block block;
//		try {
//			block = PBlock.Block.parseFrom(data);
//		} catch (InvalidProtocolBufferException e) {
//			return new RetResult<>(null, error.Errorf(e.getMessage()));
//		}
//		return new RetResult<>(block, null);
//	}

	public RetResult<Block> ProtoUnmarshal(byte[] data) {
		// TODO
//		Block block;
//		try {
//			block = Block.parseFrom(data);
//		} catch (InvalidProtocolBufferException e) {
//			return new RetResult<>(null, error.Errorf(e.getMessage()));
//		}
//		return new RetResult<>(block, null);

		return null;
	}


	public RetResult<BlockSignature> Sign(PrivateKey privKey) {
		RetResult<byte[]> hash2 = Body.Hash();
		byte[] signBytes = hash2.result;
		error err = hash2.err;
		BlockSignature bs = null;
		if (err != null) {
			return new RetResult<BlockSignature>(bs, err);
		}
		RetResult3<BigInteger, BigInteger> sign = crypto.Utils.Sign(privKey, signBytes);
		BigInteger R = sign.result1;
		BigInteger S = sign.result2;
		err = sign.err;
		if (err != null) {
			return new RetResult<BlockSignature>(bs, err);
		}

		BlockSignature signature = new BlockSignature(
			crypto.Utils.FromECDSAPub(crypto.Utils.getPublicFromPrivate(privKey)),
			Index(),
			crypto.Utils.encodeSignature(R, S));

		return new RetResult<BlockSignature>(signature, null);
	}

	public error SetSignature( BlockSignature bs) {
		Signatures.put(bs.ValidatorHex(), bs.Signature);
		return null;
	}

	public RetResult<Boolean> Verify( BlockSignature sig) {
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
