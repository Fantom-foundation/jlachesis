package lachesis.commands;

import common.Cmd;
import common.CmdRun;
import common.error;

//This version will be built when no tag MULTI is used
public class LachesisCmd implements CmdRun {

	@Override
	public error run(Cmd cmd, String[] args) {
		return runLachesis(cmd, args);
	}

	public error runLachesis(Cmd cmd, String[] args) {

		CLIConfig config = new CLIConfig();

//		error err = bindFlagsLoadViper(cmd, config);
//
//		if (err != null) {
//			return err;
//		}

		return RunCmd.runSingleLachesis(config);
	}
}
