package poset;

public class WireEvent {
	WireBody Body;
	String Signature;
	byte[] FlagTable;
	String[] WitnessProof;

	public WireEvent(WireBody body, String signature, byte[] flagTable, String[] witnessProof) {
		super();
		Body = body;
		Signature = signature;
		FlagTable = flagTable;
		WitnessProof = witnessProof;
	}

	public BlockSignature[] BlockSignatures(byte[] validator)  {
		if (Body.BlockSignatures != null) {
			BlockSignature[] blockSignatures = new BlockSignature[Body.BlockSignatures.length];
			for (int k = 0; k < Body.BlockSignatures.length; ++k) {
				WireBlockSignature bs = Body.BlockSignatures[k];
				blockSignatures[k] = new BlockSignature(validator, bs.Index, bs.Signature);
			}
			return blockSignatures;
		}
		return null;
	}
}
