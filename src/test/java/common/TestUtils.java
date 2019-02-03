package common;

import org.apache.log4j.Level;

import autils.Logger;
import node.Config;

public class TestUtils {

	public static Config TestConfig(Class testClz) {
		Config config = Config.DefaultConfig();
		config.HeartbeatTimeout = java.time.Duration.ofSeconds(1);
		config.setLogger(NewTestLogger(testClz));
		return config;
	}

	public static Logger NewTestLogger(Class<?> clazz) {
		Logger logger = Logger.getLogger(clazz);
		logger.setLevel(Level.FATAL);
		return logger;
	}
}
