package poset;

public class ParentRoundInfo {
	int round;
	boolean isRoot;
	int rootStronglySeenWitnesses;
	
	public ParentRoundInfo()  {
		this.round = -1;
		this.isRoot = false;
		
		/**
		 * TBD: It was not set in go code. I set it to -1
		 */
		rootStronglySeenWitnesses = -1;
	}
}
