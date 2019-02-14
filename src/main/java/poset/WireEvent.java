package poset;

import java.util.Arrays;

public class WireEvent {
	WireBody Body;
	String Signature;
	byte[] FlagTable;
	String[] WitnessProof;

	public WireEvent() {
		Body = null;
		Signature = null;
		FlagTable = null;
		WitnessProof = null;
	}

	public WireEvent(WireBody body, String signature) {
		super();
		Body = body;
		Signature = signature;
		FlagTable = null;
		WitnessProof = null;
	}

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

	public WireBody getBody() {
		return Body;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Body == null) ? 0 : Body.hashCode());
		result = prime * result + Arrays.hashCode(FlagTable);
		result = prime * result + ((Signature == null) ? 0 : Signature.hashCode());
		result = prime * result + Arrays.hashCode(WitnessProof);
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
		WireEvent other = (WireEvent) obj;
		if (Body == null) {
			if (other.Body != null)
				return false;
		} else if (!Body.equals(other.Body))
			return false;
		if (!Arrays.equals(FlagTable, other.FlagTable))
			return false;
		if (Signature == null) {
			if (other.Signature != null)
				return false;
		} else if (!Signature.equals(other.Signature))
			return false;
		if (!Arrays.equals(WitnessProof, other.WitnessProof))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WireEvent [Body=");
		builder.append(Body);
		builder.append(", Signature=");
		builder.append(Signature);
		builder.append(", FlagTable=");
		builder.append(Arrays.toString(FlagTable));
		builder.append(", WitnessProof=");
		builder.append(Arrays.toString(WitnessProof));
		builder.append("]");
		return builder.toString();
	}
}
