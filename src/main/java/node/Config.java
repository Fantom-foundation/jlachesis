package node;

//import common.TestLoggerAdapter;

import java.time.Duration;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Config {
	public Duration HeartbeatTimeout ; // `mapstructure:"heartbeat"`
	private Duration TCPTimeout       ; // `mapstructure:"timeout"`
	int CacheSize; //         `mapstructure:"cache-size"`
	long SyncLimit; //         `mapstructure:"sync-limit"`

	private Logger logger = Logger.getLogger(Config.class);

	private long TestDelay; // `mapstructure:"test_delay"`

	public Config(Duration heartbeatTimeout, Duration tCPTimeout, int cacheSize, long syncLimit, Logger logger, long testDelay) {
		super();
		HeartbeatTimeout = heartbeatTimeout;
		setTCPTimeout(tCPTimeout);
		CacheSize = cacheSize;
		SyncLimit = syncLimit;
		this.setLogger(logger);
		setTestDelay(testDelay);
	}

	public Config(Duration heartbeat, Duration timeout,  int cacheSize, long syncLimit, Logger logger) {
		this( heartbeat, timeout, cacheSize, syncLimit, logger, 0);
	}

	public static Config DefaultConfig() {
		Logger logger = Logger.getLogger(Config.class.getName());
		logger.setLevel(Level.DEBUG);

		return new Config(
			/* HeartbeatTimeout */ Duration.ofMillis(10),
			/* TCPTimeout: */      Duration.ofMillis(180 * 1000),
			/* CacheSize: */       500,
			/* SyncLimit:  */      100,
			/* Logger: */         logger,
			/* TestDelay: */       1
		);
	}

	public int getCacheSize() {
		return CacheSize;
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public long getTestDelay() {
		return TestDelay;
	}

	public void setTestDelay(long testDelay) {
		TestDelay = testDelay;
	}

	public Duration getTCPTimeout() {
		return TCPTimeout;
	}

	public void setTCPTimeout(Duration tCPTimeout) {
		TCPTimeout = tCPTimeout;
	}

	public Duration getHeartbeatTimeout() {
		return HeartbeatTimeout;
	}

	public long getSyncLimit() {
		return SyncLimit;
	}



//	public Config TestConfig(testing.T t) {
//		Config config = DefaultConfig();
//		config.HeartbeatTimeout = java.time.Duration.ofSeconds(1);
//
//		config.logger = TestLoggerAdapter.NewTestLogger(t);
//
//		return config;
//	}
}
