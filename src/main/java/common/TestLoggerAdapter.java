package common;

import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

// This can be used as the destination for a logger and it'll
// map them into calls to testing.T.Log, so that you only see
// the logging for failed tests.
public class TestLoggerAdapter {
	Class<?> clazz;
	String prefix;

	public TestLoggerAdapter() {

	}

	public RetResult<Integer> Write(byte[] d) {
		if (d[d.length - 1] == '\n') {
			d = Arrays.copyOfRange(d, 0, d.length - 1);
		}

		if (prefix != null && !prefix.isEmpty()) {
			String l = prefix + ": " + Arrays.toString(d);
			// TBD
//			t.Log(l);
			return new RetResult<Integer>(l.length(), null);
		}

//		t.Log(Arrays.toString(d));
		return new RetResult<Integer>(d.length, null);
	}

	public static Logger NewTestLogger(Class<?> clazz) {
		Logger logger = Logger.getLogger(clazz);
		logger.setLevel(Level.FATAL);
		return logger;
	}
}