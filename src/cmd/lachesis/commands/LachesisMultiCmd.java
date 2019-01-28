// This version will be built when tag MULTI is used in go build
//
package lachesis.commands;

import channel.ExecService;
import common.Cmd;
import common.CmdRun;
import common.error;

public class LachesisMultiCmd implements CmdRun {

	@Override
	public error run(Cmd cmd, String[] args) {
		return runLachesis(cmd, args);
	}

	public error runLachesis(Cmd cmd, String[] args)  {

		int n = 3;
		String nValue = System.getenv().get("n");
		error err = null;

		if (nValue.length() > 0) {
			long n64 = Long.parseLong(nValue, 10);
			if (err != null) {
				return err;
			}
			n = (int) n64;
		}

		CLIConfig[] configs = new CLIConfig[n];
		int digits = Long.toUnsignedString(n).length();

		for (int i = 0; i < n; i++) {
			configs[i] = new CLIConfig();
			configs[i].Lachesis.BindAddr = String.format("127.0.0.1:%d", 12000 + i + 1);
			configs[i].Lachesis.ServiceAddr = String.format("127.0.0.1:%d", 8000 + i + 1);
			configs[i].ProxyAddr = String.format("127.0.0.1:%d", 9000 + i + 1);
			configs[i].Lachesis.DataDir += String.format("/%0*d", digits, i);

			final CLIConfig cfg = configs[i];
			if (i > 0) {
				ExecService.go(() -> RunCmd.runSingleLachesis(cfg));
			}
		}

		return RunCmd.runSingleLachesis(configs[0]);
	}

}
