package dummy;

import dummy.commands.RootCmd;

public class Dummy {
	public static void main(String[] args) {
		RootCmd rootCmd = RootCmd.newDummyRootCmd();
		rootCmd.run(rootCmd, args);
	}
}