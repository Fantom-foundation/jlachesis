package poset;

import com.google.protobuf.Parser;

import common.IProto;

/**
 * Roots constitute the base of a Poset. Each Participant is assigned a Root on
 * top of which Events will be added. The first Event of a participant must have a
 * Self-Parent and an Other-Parent that match its Root X and Y respectively.
 * This construction allows us to initialize Posets where the first Events are
 * taken from the middle of another Poset
 *
 * ex 1:
 * ----------------        -----------------       -----------------
 * - Event E0      -        - Event E1      -       - Event E2      -
 * - SP = ""       -        - SP = ""       -       - SP = ""       -
 * - OP = ""       -        - OP = ""       -       - OP = ""       -
 * -----------------        -----------------       -----------------
 *         |                        |                       |
 *         -----------------		 -----------------		 -----------------
 *         - Root 0        - 		 - Root 1        - 		 - Root 2        -
 *         - X = Y = ""    - 		 - X = Y = ""    -		 - X = Y = ""    -
 *         - Index= -1     -		 - Index= -1     -       - Index= -1     -
 *         - Others= empty - 		 - Others= empty -       - Others= empty -
 *         -----------------		 -----------------       -----------------
 *
 * ex 2:
 *
 * -----------------
 * - Event E02     -
 * - SP = E01      -
 * - OP = E_OLD    -
 * -----------------
 *        |
 * -----------------
 * - Event E01     -
 * - SP = E00      -
 * - OP = E10      -  \
 * -----------------    \
 *        |               \
 * -----------------        -----------------       -----------------
 * - Event E00     -        - Event E10     -       - Event E20     -
 * - SP = x0       -        - SP = x1       -       - SP = x2       -
 * - OP = y0       -        - OP = y1       -       - OP = y2       -
 * -----------------        -----------------       -----------------
 *         |                        |                       |
 * -----------------		 -----------------		 -----------------
 * - Root 0        - 		 - Root 1        - 		 - Root 2        -
 * - X: x0, Y: y0  - 		 - X: x1, Y: y1  - 		 - X: x2, Y: y2  -
 * - Index= i0     -		 - Index= i1     -       - Index= i2     -
 * - Others= {     - 		 - Others= empty -       - Others= empty -
 * -  E02: E_OLD   -        -----------------       -----------------
 * - }             -
 * -----------------
 *
 *
 * RootEvent contains enough information about an Event and its direct descendant
 * to allow inserting Events on top of it.
 * RootEvent constructor creates a RootEvent corresponding to the the very beginning
 * of a Poset.
 */
public class RootEvent {
	String Hash;
	long CreatorID;
	long Index;
	long LamportTimestamp;
	long Round;

	public RootEvent(String hash, long creatorID, long index, long lamportTimestamp, long round) {
		super();
		Hash = hash;
		CreatorID = creatorID;
		Index = index;
		LamportTimestamp = lamportTimestamp;
		Round = round;
	}

	public RootEvent() {

	}

	public RootEvent(long creatorID) {
		String hash = String.format("Root%d", creatorID);
		this.Hash = hash;
		this.CreatorID = creatorID;
		this.Index = -1;
		this.LamportTimestamp = -1;
		this.Round = -1;
	}

	public boolean equals(RootEvent that) {
		return this.Hash.equals(that.Hash) &&
			this.CreatorID == that.CreatorID &&
			this.Index == that.Index &&
			this.LamportTimestamp == that.LamportTimestamp &&
			this.Round == that.Round;
	}

	public IProto<RootEvent, poset.proto.RootEvent> marshaller() {
		return new IProto<RootEvent, poset.proto.RootEvent>() {
			@Override
			public poset.proto.RootEvent toProto() {
				poset.proto.RootEvent.Builder builder = poset.proto.RootEvent.newBuilder();
				builder.setHash(Hash)
				.setCreatorID(CreatorID)
				.setIndex(Index)
				.setLamportTimestamp(LamportTimestamp)
				.setRound(Round);
				return builder.build();
			}

			@Override
			public void fromProto(poset.proto.RootEvent proto) {
				Hash = proto.getHash();
				CreatorID = proto.getCreatorID();
				Index = proto.getIndex();
				LamportTimestamp = proto.getLamportTimestamp();
				Round = proto.getRound();
			}

			@Override
			public Parser<poset.proto.RootEvent> parser() {
				return poset.proto.RootEvent.parser();
			}
		};
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (CreatorID ^ (CreatorID >>> 32));
		result = prime * result + ((Hash == null) ? 0 : Hash.hashCode());
		result = prime * result + (int) (Index ^ (Index >>> 32));
		result = prime * result + (int) (LamportTimestamp ^ (LamportTimestamp >>> 32));
		result = prime * result + (int) (Round ^ (Round >>> 32));
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
		RootEvent other = (RootEvent) obj;
		if (CreatorID != other.CreatorID)
			return false;
		if (Hash == null) {
			if (other.Hash != null)
				return false;
		} else if (!Hash.equals(other.Hash))
			return false;
		if (Index != other.Index)
			return false;
		if (LamportTimestamp != other.LamportTimestamp)
			return false;
		if (Round != other.Round)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RootEvent [Hash=").append(Hash).append(", CreatorID=").append(CreatorID).append(", Index=")
				.append(Index).append(", LamportTimestamp=").append(LamportTimestamp).append(", Round=").append(Round)
				.append("]");
		return builder.toString();
	}

	public String GetHash() {
		return Hash;
	}

	public long GetCreatorID() {
		return CreatorID;
	}

	public long GetIndex() {
		return Index;
	}

	public long GetLamportTimestamp() {
		return LamportTimestamp;
	}

	public long GetRound() {
		return Round;
	}
}