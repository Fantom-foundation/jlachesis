package poset;


/*
Roots constitute the base of a Poset. Each Participant is assigned a Root on
top of which Events will be added. The first Event of a participant must have a
Self-Parent and an Other-Parent that match its Root X and Y respectively.

This construction allows us to initialize Posets where the first Events are
taken from the middle of another Poset

ex 1:

-----------------        -----------------       -----------------
- Event E0      -        - Event E1      -       - Event E2      -
- SP = ""       -        - SP = ""       -       - SP = ""       -
- OP = ""       -        - OP = ""       -       - OP = ""       -
-----------------        -----------------       -----------------
        |                        |                       |
-----------------		 -----------------		 -----------------
- Root 0        - 		 - Root 1        - 		 - Root 2        -
- X = Y = ""    - 		 - X = Y = ""    -		 - X = Y = ""    -
- Index= -1     -		 - Index= -1     -       - Index= -1     -
- Others= empty - 		 - Others= empty -       - Others= empty -
-----------------		 -----------------       -----------------

ex 2:

-----------------
- Event E02     -
- SP = E01      -
- OP = E_OLD    -
-----------------
       |
-----------------
- Event E01     -
- SP = E00      -
- OP = E10      -  \
-----------------    \
       |               \
-----------------        -----------------       -----------------
- Event E00     -        - Event E10     -       - Event E20     -
- SP = x0       -        - SP = x1       -       - SP = x2       -
- OP = y0       -        - OP = y1       -       - OP = y2       -
-----------------        -----------------       -----------------
        |                        |                       |
-----------------		 -----------------		 -----------------
- Root 0        - 		 - Root 1        - 		 - Root 2        -
- X: x0, Y: y0  - 		 - X: x1, Y: y1  - 		 - X: x2, Y: y2  -
- Index= i0     -		 - Index= i1     -       - Index= i2     -
- Others= {     - 		 - Others= empty -       - Others= empty -
-  E02: E_OLD   -        -----------------       -----------------
- }             -
-----------------
*/

//RootEvent contains enough information about an Event and its direct descendant
//to allow inserting Events on top of it.
//NewBaseRootEvent creates a RootEvent corresponding to the the very beginning
//of a Poset.

public class RootEvent {
	String Hash; //                string   `protobuf:"bytes,1,opt,name=Hash,proto3" json:"Hash,omitempty"`
	long CreatorID; //           int64    `protobuf:"varint,2,opt,name=CreatorID,proto3" json:"CreatorID,omitempty"`
	long Index; //                int64    `protobuf:"varint,3,opt,name=Index,proto3" json:"Index,omitempty"`
	long LamportTimestamp; //     int64    `protobuf:"varint,4,opt,name=LamportTimestamp,proto3" json:"LamportTimestamp,omitempty"`
	long Round; //                int64    `protobuf:"varint,5,opt,name=Round,proto3" json:"Round,omitempty"`

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
		return this.Hash == that.Hash &&
			this.CreatorID == that.CreatorID &&
			this.Index == that.Index &&
			this.LamportTimestamp == that.LamportTimestamp &&
			this.Round == that.Round;
	}


//	public void Reset()         { *m = RootEvent{} }
//	public String() string { return proto.CompactTextString(m) }
//	}
//
//	public XXX_Unmarshal(b []byte) error {
//		return xxx_messageInfo_RootEvent.Unmarshal(m, b)
//	}
//	public XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
//		return xxx_messageInfo_RootEvent.Marshal(b, m, deterministic)
//	}
//	var xxx_messageInfo_RootEvent proto.InternalMessageInfo

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