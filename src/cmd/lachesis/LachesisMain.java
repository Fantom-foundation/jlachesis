package lachesis;

import common.error;
import dummy.commands.RootCmd;
import lachesis.commands.KeygenCmd;
import lachesis.commands.RunCmd;
import lachesis.commands.VersionCmd;

public class LachesisMain {

	public static void main(String[] args) {
		RootCmd rootCmd = RootCmd.newDummyRootCmd();

		VersionCmd version = new VersionCmd();
		KeygenCmd keygen = new KeygenCmd();
		RunCmd run = new RunCmd();

		// TODO
//		rootCmd.AddCommand(
//			cmd.VersionCmd,
//			cmd.NewKeygenCmd(),
//			cmd.NewRunCmd());

		error err = rootCmd.run(rootCmd, args);
		if (err != null) {
			System.exit(1);
		}
	}
}