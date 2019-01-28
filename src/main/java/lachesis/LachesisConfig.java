package lachesis;

import java.nio.file.Paths;
import java.security.KeyPair;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import node.Config;

public class LachesisConfig {
	public String DataDir; // `mapstructure:"datadir"`
	public String BindAddr; // string `mapstructure:"listen"`
	public String ServiceAddr; // string `mapstructure:"service-listen"`
	public boolean ServiceOnly; // `mapstructure:"service-only"`
	public int MaxPool; // `mapstructure:"max-pool"`
	public boolean Store; // `mapstructure:"store"`
	public String LogLevel; // `mapstructure:"log"`

	public node.Config NodeConfig; // `mapstructure:",squash"`

	public boolean LoadPeers;
	private proxy.AppProxy Proxy;
	public KeyPair Key;

	Logger logger;

	public boolean Test; //   `mapstructure:"test"`
	public long TestN; // `mapstructure:"test_n"`
	public long TestDelay; // `mapstructure:"test_delay"`

	public LachesisConfig() {
		super();
	}

	public static LachesisConfig NewDefaultConfig(){
		LachesisConfig config = new LachesisConfig();
		config.DataDir = DefaultDataDir();
		config.BindAddr = ":1337";
		config.ServiceAddr= ":8000";
		config.ServiceOnly= false;
		config.MaxPool=     2;
		config.NodeConfig=  Config.DefaultConfig();
		config.Store=       false;
		config.LogLevel=    "info";
		config.setProxy(null);
		config.logger=      Logger.getLogger(LachesisConfig.class);
		config.LoadPeers=   true;
		config.Key=         null;
		config.setTest(false);
		config.setTestN(0);
		config.TestDelay=   1;

		config.logger.setLevel(Level.INFO);
//		lachesis_log.NewLocal(config.Logger, config.LogLevel);
		//config.Proxy = sproxy.NewInmemAppProxy(config.Logger)
		//config.Proxy, _ = sproxy.NewSocketAppProxy("127.0.0.1:1338", "127.0.0.1:1339", 1*time.Second, config.Logger)
		config.NodeConfig.setLogger(config.logger);
		config.NodeConfig.setTestDelay(config.TestDelay);

		return config;
	}

	public String DefaultBadgerDir() {
		String dataDir = DefaultDataDir();
		if (!dataDir.isEmpty()) {
			return Paths.get(dataDir, "badger_db").toString();
		}
		return "";
	}

	public String BadgerDir() {
		return Paths.get(DataDir, "badger_db").toString();
	}

	public static String DefaultDataDir() {
		// Try to place the data folder in the user's home dir
		String home = HomeDir();
		if (!home.isEmpty()) {
			if (System.getenv().equals("darwin")) {
				return Paths.get(home, ".lachesis").toString();
			} else if (System.getenv().equals("windows")) {
				return Paths.get(home, "AppData", "Roaming", "LACHESIS").toString();
			} else {
				return Paths.get(home, ".lachesis").toString();
			}
		}
		// As we cannot guess a stable location, return empty and handle later
		return "";
	}

	public static String HomeDir() {
		String home = System.getenv().get("HOME");
		if (home != null && !home.isEmpty()) {
			return home;
		}

		String usr = System.getProperty("user.home");
		if  (usr != null) {
			return usr;
		}
		return "";
	}

	public long getTestN() {
		return TestN;
	}

	public void setTestN(long testN) {
		TestN = testN;
	}

	public boolean isTest() {
		return Test;
	}

	public void setTest(boolean test) {
		Test = test;
	}

	public proxy.AppProxy getProxy() {
		return Proxy;
	}

	public void setProxy(proxy.AppProxy proxy) {
		Proxy = proxy;
	}

	public String getDataDir() {
		return DataDir;
	}

	public String getBindAddr() {
		return BindAddr;
	}

	public String getServiceAddr() {
		return ServiceAddr;
	}

	public boolean isServiceOnly() {
		return ServiceOnly;
	}

	public int getMaxPool() {
		return MaxPool;
	}

	public boolean isStore() {
		return Store;
	}

	public String getLogLevel() {
		return LogLevel;
	}

	public node.Config getNodeConfig() {
		return NodeConfig;
	}

	public boolean isLoadPeers() {
		return LoadPeers;
	}

	public KeyPair getKey() {
		return Key;
	}

	public Logger getLogger() {
		return logger;
	}

	public long getTestDelay() {
		return TestDelay;
	}

	public void setDataDir(String dataDir) {
		DataDir = dataDir;
	}

	public void setBindAddr(String bindAddr) {
		BindAddr = bindAddr;
	}

	public void setServiceAddr(String serviceAddr) {
		ServiceAddr = serviceAddr;
	}

	public void setServiceOnly(boolean serviceOnly) {
		ServiceOnly = serviceOnly;
	}

	public void setMaxPool(int maxPool) {
		MaxPool = maxPool;
	}

	public void setStore(boolean store) {
		Store = store;
	}

	public void setLogLevel(String logLevel) {
		LogLevel = logLevel;
	}

	public void setNodeConfig(node.Config nodeConfig) {
		NodeConfig = nodeConfig;
	}

	public void setLoadPeers(boolean loadPeers) {
		LoadPeers = loadPeers;
	}

	public void setKey(KeyPair key) {
		Key = key;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public void setTestDelay(long testDelay) {
		TestDelay = testDelay;
	}
}
