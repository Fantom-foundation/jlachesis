package common;

import common.error;

public interface CmdRun {
	error run(Cmd cmd, String[] args);
}