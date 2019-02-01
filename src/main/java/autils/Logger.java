package autils;

import java.util.Arrays;
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
		logger.debug(getOutput(message));
		map = null;
	}

	public void info(Object message) {
		logger.info(getOutput(message));
		map = null;
	}

	public void error(Object message) {
		logger.error(getOutput(message));
		map = null;
	}

	public void fatal(Object message) {
		logger.fatal(getOutput(message));
		map = null;
	}

	public void warn(String message) {
		logger.warn(getOutput(message));
		map = null;
	}

	public void debugf(String format, Object... objs) {
		logger.debug(getOutput(String.format(format, objs)));
		map = null;
	}

	public void warnf(String format, Object... objs) {
		logger.warn(getOutput(String.format(format, objs)));
		map = null;
	}

	public void errorf(String format, Object... objs) {
		logger.error(getOutput(String.format(format, objs)));
		map = null;
	}

	public void fatalf(String format, Object... objs) {
		logger.fatal(getOutput(String.format(format, objs)));
		map = null;
	}

	private String getOutput(Object message) {
		return message + " :: " + mapToString();
	}

	private String mapToString() {
		return Arrays.toString(map.entrySet().toArray());
	}
}
