package lachesis.commands;

import common.Cmd;
import common.error;
import picocli.CommandLine.Option;

public class VersionCmd extends Cmd {
	public VersionCmd() {
		setUse("version");
		setShort("Show version info");
	}

	/**
	 * Main container of version command's options
	 */
	public class VersionOptions {
		@Option(names = { "-v", "--version"}, versionHelp = true, description = "Show the version")
	    private boolean version;

		public boolean isVersion() {
			return version;
		}
	}

	public error run(Cmd cmd, String[] args) {
		VersionOptions options = new VersionOptions();
        error err = common.Utils.populateOptions(options, args);
		if (err != null)
			return err;
		if (options.isVersion()) {
			System.out.println(version.Version.getInstance().getVersion());
		}
		return null;
	}
}
