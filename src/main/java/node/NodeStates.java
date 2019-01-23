package node;

/**
 * Defines possible states of a node.
 */
public enum NodeStates {
	// Gossiping is the initial state of a Lachesis node.
	Gossiping (0, "Gossiping"),

	CatchingUp (1, "CatchingUp"),

	Shutdown (2, "Shutdown"),

	Unknown (3, "Unknown");

	private NodeStates(int state, String stateName) {
		this.state = state;
		this.stateName = stateName;
	}

	public static NodeStates getNodeState(int i) {
		switch (i) {
		case 0: return Gossiping;
		case 1: return CatchingUp;
		case 2: return Shutdown;
		case 3: return Unknown;
		default: return Gossiping;
		}
	}

	public int getState() {
		return state;
	}

	public String getStateName() {
		return stateName;
	}

	final int state;

	final String stateName;
}