package common;

public enum StoreErrType {
	KeyNotFound("Not Found"), // StoreErrType = iota
	TooLate("Too Late"),
	PassedIndex("Passed Index"),
	SkippedIndex("Skipped Index"),
	NoRoot("No Root"),
	UnknownParticipant("Unknown Participant"),
	Empty("Empty");

	StoreErrType(String m) {
		this.m = m;
	}

	private String m;

	public String getM() {
		return m;
	}
}