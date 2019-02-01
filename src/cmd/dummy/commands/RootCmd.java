package dummy.commands;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Level;

import autils.Logger;
import common.Cmd;
import common.error;
import dummy.DummyClient;
import lachesis.ConfigUtils;
import picocli.CommandLine.Option;

public class RootCmd extends Cmd {
	CLIConfig config;
	Logger logger;

	//RootCmd is the root command for Dummy
	public static RootCmd newDummyRootCmd() {
		RootCmd RootCmd= new RootCmd();
		RootCmd.setUse("dummy");
		RootCmd.setShort("Dummy Socket Client for Lachesis");
		return RootCmd;
	}

	//RootCmd is the root command for Lachesis
	public static RootCmd newRootCmd() {
		RootCmd cmd = new RootCmd();
		cmd.setUse("lachesis");
		cmd.setShort("lachesis consensus");
		return cmd;
	}

	/*******************************************************************************
	* RUN
	*******************************************************************************/
	public error run(Cmd cmd, String[] args)  {
		// setPreRunE
		error err = loadConfig(cmd, args);
		if (err != null)
			return err;
		// setRunE
		err = runDummy(cmd, args);
		return err;
	}

	public error runDummy(Cmd cmd, String[] args)  {
		String name = config.Name;
		String address = config.ProxyAddr;
		//Create and run Dummy Socket Client
		DummyClient client = DummyClient.NewDummySocketClient(address, logger);

		//Listen for input messages from tty
		error err;
		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNext()) {
			System.out.print("Enter your text: ");
			String text = scanner.nextLine();
			String message = String.format("%s: %s", name, text);
			err = client.SubmitTx(message.getBytes());
			if (err != null) {
				System.err.printf("Error in SubmitTx: %v\n", err);
			}
		}
		return null;
	}

	/*******************************************************************************
	* CONFIG
	*******************************************************************************/

	public error loadConfig(Cmd cmd, String[] args) {

		// Create a new Options class, which holds our flags.
        RootOptions options = new RootOptions();
        common.Utils.populateOptions(options, args);
		config = new CLIConfig();

		logger = newLogger();
		logger.setLevel(ConfigUtils.LogLevel(config.LogLevel));
		logger.field("name", config.Name)
			.field("client-listen", config.ClientAddr)
			.field("proxy-connect", config.ProxyAddr)
			.field("discard", config.Discard)
			.field("log", config.LogLevel)
			.debug("RUN");
		return null;
	}

	public Logger newLogger()  {
		Logger logger = Logger.getLogger(RootCmd.class);
		Map<Level,String> pathMap = new HashMap<Level,String>();
		OutputStream out;
		try {
			out = Files.newOutputStream(Paths.get("dummy_info.log"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		} catch (IOException e) {
			logger.info("Failed to open dummy_info.log file, using default stderr");
		}
		pathMap.put(Level.INFO, "dummy_info.log");

		try {
			out = Files.newOutputStream(Paths.get("dummy_debug.log"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		} catch (IOException e) {
			logger.info("Failed to open dummy_debug.log file, using default stderr");
		}
		pathMap.put(Level.DEBUG, "dummy_debug.log");

		return logger;
	}

	/**
	 * This is the main container which will be populated by picocli with values
	 * from the arguments.
	 */
	public class RootOptions {
		CLIConfig config = new CLIConfig();

		@Option(names = { "-n", "--name"}, description= "Client name")
	    private String name = config.Name;

	    @Option(names = {"--client-listen"}, description= "Listen IP:Port of Dummy Socket Client")
	    private String clientListen = config.ClientAddr;

	    @Option(names = {"--proxy-connect"}, description= "IP:Port to connect to Lachesis proxy")
	    private String proxyConnect = config.ProxyAddr;

	    @Option(names = {"--discard"}, description= "discard output to stderr and sdout")
	    private boolean discard = config.Discard;

	    @Option(names = {"--log"}, description= "debug, info, warn, error, fatal, panic")
	    private String log = config.LogLevel;

		public CLIConfig getConfig() {
			return config;
		}

		public String getName() {
			return name;
		}

		public String getClientListen() {
			return clientListen;
		}

		public String getProxyConnect() {
			return proxyConnect;
		}

		public boolean isDiscard() {
			return discard;
		}

		public String getLog() {
			return log;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("RootOptions [config=");
			builder.append(config);
			builder.append(", name=");
			builder.append(name);
			builder.append(", clientListen=");
			builder.append(clientListen);
			builder.append(", proxyConnect=");
			builder.append(proxyConnect);
			builder.append(", discard=");
			builder.append(discard);
			builder.append(", log=");
			builder.append(log);
			builder.append("]");
			return builder.toString();
		}
	}
}
