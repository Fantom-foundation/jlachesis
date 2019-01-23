package lachesis;

import java.nio.file.*;
import java.security.KeyPair;
import java.security.PrivateKey;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import node.Config;

public class LachesisConfig {
	String DataDir; // `mapstructure:"datadir"`
	String BindAddr; // string `mapstructure:"listen"`
	String ServiceAddr; // string `mapstructure:"service-listen"`
	boolean ServiceOnly; // `mapstructure:"service-only"`
	int MaxPool; // `mapstructure:"max-pool"`
	boolean Store; // `mapstructure:"store"`
	String LogLevel; // `mapstructure:"log"`

	node.Config NodeConfig; // `mapstructure:",squash"`

	boolean LoadPeers;
	proxy.AppProxy Proxy;
	KeyPair Key;

	Logger logger;

	boolean Test; //   `mapstructure:"test"`
	long TestN; // `mapstructure:"test_n"`
	long TestDelay; // `mapstructure:"test_delay"`

	public LachesisConfig() {
		super();
	}

	public LachesisConfig NewDefaultConfig(){
		LachesisConfig config = new LachesisConfig();
		config.DataDir = DefaultDataDir();
		config.BindAddr = ":1337";
		config.ServiceAddr= ":8000";
		config.ServiceOnly= false;
		config.MaxPool=     2;
		config.NodeConfig=  Config.DefaultConfig();
		config.Store=       false;
		config.LogLevel=    "info";
		config.Proxy=       null;
		config.logger=      Logger.getLogger(LachesisConfig.class);
		config.LoadPeers=   true;
		config.Key=         null;
		config.Test=        false;
		config.TestN=       0;
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

	public String DefaultDataDir() {
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

	public String HomeDir() {
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

	public Level LogLevel(String l) {
		switch (l) {
		case "debug":
			return Level.DEBUG;
		case "info":
			return Level.INFO;
		case "warn":
			return Level.WARN;
		case "error":
			return Level.ERROR;
		case "fatal":
			return Level.FATAL;
		case "panic":
			return Level.FATAL; // panic
		default:
			return Level.DEBUG;
		}
	}
}
