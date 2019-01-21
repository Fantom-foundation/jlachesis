package difftool;

import java.util.ArrayList;

// Result is a set of differences
public class Result extends ArrayList<Diff>{
	 //Diff[] diffArray;

	public Result() {
		super();
	}
	
	/*
	 * Result's methods
	 */
	
	public boolean IsEmpty() {
		for (Diff diff: this) {
			if (!diff.IsEmpty()) {
				return false;
			}
		}
		return true;
	}
	
	public String ToString() {
		ArrayList<String> output = new ArrayList<String>();
		for (Diff diff: this) {
			if (!diff.IsEmpty()) {
				output.add(diff.ToString());
				if (!diff.Descr.isEmpty()) {
					output.add("\t"+diff.Descr);
				}
			}
		}
		return String.join("\n", output);
	}
}