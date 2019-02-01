package autils;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;

/**
 * A logger type to allow us to put the field in
 *
 * @author qn
 */
public class Logger {
	org.apache.log4j.Logger logger;
	Map<String,Object> map;

	public Logger(org.apache.log4j.Logger logger) {
		this.logger = logger;
		this.map = null;
	}

	public static Logger getLogger(Class clz) {
		return new Logger(org.apache.log4j.Logger.getLogger(clz));
	}

	public void setLevel(Level lvl) {
		logger.setLevel(lvl);
	}

	public Logger field(String s, Object o) {
		if (map == null) {
			map = new HashMap<>();
		}
		map.put(s, o);
		return this;
	}

	public void debug(Object message) {
		logger.debug(message);
		map = null;
	}

	public void info(Object message) {
		logger.info(message);
		map = null;
	}

	public void error(Object message) {
		logger.error(message);
		map = null;
	}

	public void fatal(Object message) {
		logger.fatal(message);
		map = null;
	}
}
