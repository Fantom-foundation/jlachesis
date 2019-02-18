package lachesis.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;

import autils.FileUtils;
import autils.time;
import channel.ExecService;
import common.Cmd;
import common.RetResult;
import common.error;
import lachesis.ConfigUtils;
import lachesis.Lachesis;
import peers.Peers;
import picocli.CommandLine.Option;
import proxy.AppProxy;
import proxy.GrpcAppProxy;

public class RunCmd extends Cmd {
	//NewRunCmd returns the command that starts a Lachesis node
	public RunCmd() {
		setUse("run");
		setShort("Run node");
	}

	public error run(Cmd cmd, String[] args)  {
		// Create a new Options class, which holds our flags.
		RunOptions options = new RunOptions();
		error err = common.Utils.populateOptions(options, args);
		if (err != null)
			return err;
		err = runSingleLachesis(new CLIConfig());
		return err;
	}

	/**
	 * Main container of Run command's options
	 */
	public class RunOptions {
		CLIConfig config = new CLIConfig();

		// Log
		@Option(names = { "-datadir", "--datadir"}, description= "Top-level directory for configuration and data")
	    private String datadir = config.Lachesis.getDataDir();

	    @Option(names = { "-log", "--log"}, description= "debug, info, warn, error, fatal, panic")
	    private String log = config.Lachesis.getLogLevel();

	    @Option(names = {"--log2file"}, description= "duplicate log output into file lachesis_<BindAddr>.log")
	    private String log2file = config.ProxyAddr;

	    // Network
	    @Option(names = {"-l", "--listen"}, description= "Listen IP:Port for lachesis node")
	    private String listen = config.Lachesis.getBindAddr();

	    @Option(names = {"-t", "--timeout"}, description= "TCP Timeout")
	    private Duration timeout = config.Lachesis.NodeConfig.getTCPTimeout();

	    @Option(names = {"--max-pool"}, description= "Connection pool size max")
	    private int maxPool = config.Lachesis.getMaxPool();

	    // Proxy
 		@Option(names = {"--standalone"}, description= "Do not create a proxy")
	    private boolean standalone = config.Standalone;

	    @Option(names = {"--service-only"}, description= "Only host the http service")
	    private boolean serviceOnly = config.Lachesis.isServiceOnly();

	    @Option(names = {"-p", "--proxy-listen"}, description= "Listen IP:Port for lachesis proxy")
	    private int proxyListen = config.Lachesis.getMaxPool();

	    @Option(names = {"-c", "--client-connect"}, description= "IP:Port to connect to client")
	    private String clientConnect = config.ClientAddr;

	    // Service
 		@Option(names = {"-s", "--service-listen"}, description= "Listen IP:Port for HTTP service")
	    private String serviceListen = config.Lachesis.getServiceAddr();

 		// Store
 		@Option(names = {"--store"}, description= "Use badgerDB instead of in-mem DB")
	    private boolean store = config.Lachesis.isStore();

 		@Option(names = {"--cache-size"}, description= "Number of items in LRU caches")
	    private int cacheSize = config.Lachesis.NodeConfig.getCacheSize();

 		// Node configuration
		@Option(names = {"--heartbeat"}, description= "Time between gossips")
	    private Duration heartbeat = config.Lachesis.NodeConfig.HeartbeatTimeout;

 		@Option(names = {"--sync-limit"}, description= "Max number of events for sync")
	    private long syncLimit = config.Lachesis.NodeConfig.getSyncLimit();

 		// Test
		@Option(names = {"--test"}, description= "Enable testing (sends transactions to random nodes in the network)")
	    private boolean test = config.Lachesis.isTest();

 		@Option(names = {"--test_n"}, description= "Number of transactions to send")
	    private long testN = config.Lachesis.getTestN();

 		@Option(names = {"--test_delay"}, description= "Number of second to delay before sending transactions")
	    private long testDelay = config.Lachesis.getTestN();

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("RootOptions [config=");
			builder.append(config);
			builder.append(", datadir=");
			builder.append(datadir);
			builder.append(", log=");
			builder.append(log);
			builder.append(", log2file=");
			builder.append(log2file);
			builder.append(", listen=");
			builder.append(listen);
			builder.append(", timeout=");
			builder.append(timeout);
			builder.append(", maxPool=");
			builder.append(maxPool);
			builder.append(", standalone=");
			builder.append(standalone);
			builder.append(", serviceOnly=");
			builder.append(serviceOnly);
			builder.append(", proxyListen=");
			builder.append(proxyListen);
			builder.append(", clientConnect=");
			builder.append(clientConnect);
			builder.append(", serviceListen=");
			builder.append(serviceListen);
			builder.append(", store=");
			builder.append(store);
			builder.append(", cacheSize=");
			builder.append(cacheSize);
			builder.append(", heartbeat=");
			builder.append(heartbeat);
			builder.append(", syncLimit=");
			builder.append(syncLimit);
			builder.append(", test=");
			builder.append(test);
			builder.append(", testN=");
			builder.append(testN);
			builder.append(", testDelay=");
			builder.append(testDelay);
			builder.append("]");
			return builder.toString();
		}
	}


	public static error runSingleLachesis(CLIConfig config) {
		config.Lachesis.getLogger().setLevel(ConfigUtils.LogLevel(config.Lachesis.getLogLevel()));
		config.Lachesis.NodeConfig.setLogger(config.Lachesis.getLogger());

		error err = null;

		if (config.Log2file) {
			String filePath = String.format("lachesis_%v.log", config.Lachesis.getBindAddr());

			FileUtils.createFile(filePath, FileUtils.MOD_666);
			try {
				Files.newOutputStream(
					      Paths.get(filePath),
					      StandardOpenOption.APPEND, StandardOpenOption.CREATE,
							StandardOpenOption.TRUNCATE_EXISTING,
							StandardOpenOption.WRITE);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				err = error.Errorf(e.getMessage());
			}

			if (err != null) {
				System.err.printf("error opening file: %v", err);
			}

			// TODO
//			mw := io.MultiWriter(os.Stdout, f);
//			config.Lachesis.NodeConfig.getLogger().SetOutput(mw);
		}

//		lachesis_log.NewLocal(config.Lachesis.Logger, config.Lachesis.LogLevel);

		config.Lachesis.getLogger()
			.field("proxy-listen", config.ProxyAddr)
			.field("client-connect", config.ClientAddr)
			.field("standalone", config.Standalone)
			.field("service-only", config.Lachesis.ServiceOnly)

			.field("lachesis.datadir", config.Lachesis.DataDir)
			.field("lachesis.bindaddr", config.Lachesis.BindAddr)
			.field("lachesis.service-listen", config.Lachesis.ServiceAddr)
			.field("lachesis.maxpool", config.Lachesis.MaxPool)
			.field("lachesis.store",  config.Lachesis.Store)
			.field("lachesis.loadpeers", config.Lachesis.LoadPeers)
			.field("lachesis.log", config.Lachesis.LogLevel)

			.field("lachesis.node.heartbeat", config.Lachesis.NodeConfig.HeartbeatTimeout)
			.field("lachesis.node.tcptimeout",config.Lachesis.NodeConfig.TCPTimeout)
			.field("lachesis.node.cachesize",  config.Lachesis.NodeConfig.CacheSize)
			.field("lachesis.node.synclimit",  config.Lachesis.NodeConfig.SyncLimit)
			.debug("RUN");

		if (!config.Standalone) {
			GrpcAppProxy p = new GrpcAppProxy(
				config.ProxyAddr,
				config.Lachesis.NodeConfig.HeartbeatTimeout,
				config.Lachesis.getLogger()
			);

			if (err != null) {
				config.Lachesis.getLogger().error(String.format("Cannot initialize socket AppProxy: %s", err.Error()));
				return null;
			}
			config.Lachesis.setProxy(p);
		} else {
			AppProxy p = dummy.DummyClient.NewInmemDummyApp(config.Lachesis.getLogger());
			config.Lachesis.setProxy(p);
		}

		Lachesis engine = new Lachesis(config.Lachesis);
		err = engine.Init();
		if (err != null) {
			config.Lachesis.getLogger().error(String.format("Cannot initialize engine: %s", err.Error()));
			return null;
		}

		if (config.Lachesis.isTest()) {
			RetResult<Peers> participantsCall = engine.getStore().Participants();
			Peers p = participantsCall.result;
			err = participantsCall.err;
			if (err != null) {
				return error.Errorf(String.format("Failed to acquire participants: %s", err));
			}
			ExecService.go( () -> {
				while (true) {
					try {
						Thread.sleep(10 * time.Second);
						long ct = engine.getNode().getConsensusTransactionsCount();
						// 3 - number of notes in test; 10 - number of transactions sended at once
						if (ct >= 3*10*config.Lachesis.getTestN()) {
							Thread.sleep(10 * time.Second);
							engine.getNode().shutdown();
							break;
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			// TODO
//			go tester.PingNodesN(p.getSorted(), p.getByPubKey(), config.Lachesis.getTestN(), config.Lachesis.getTestDelay(), config.Lachesis.getLogger())
		}

		engine.getNode().register();
		engine.Run();

		return null;
	}
}