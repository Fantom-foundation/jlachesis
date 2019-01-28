package lachesis;

import org.apache.log4j.Level;

public class ConfigUtils {
	public static Level LogLevel(String l) {
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
