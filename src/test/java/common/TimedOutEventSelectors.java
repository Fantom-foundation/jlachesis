package common;

import static org.junit.Assert.fail;

import org.jcsp.lang.CSTimer;
import org.jcsp.lang.One2OneChannel;

import channel.Selectors;

public class TimedOutEventSelectors<T> extends Selectors<T> {
		long timeout;
		String errTimeout;

		public TimedOutEventSelectors(One2OneChannel<T> ch) {
			super(ch);
		}
		public TimedOutEventSelectors(One2OneChannel<T> ch, CSTimer timer) {
			super(ch);
			this.tim = timer;
		}

		public TimedOutEventSelectors<T> setTimeout(long timeout, String errTimeout) {
			this.timeout = timeout;
			this.errTimeout = errTimeout;
			tim.setAlarm(tim.read() + timeout);
			return this;
		}

		public void onTimeOut() {
			tim.setAlarm(tim.read() + timeout);
			fail(errTimeout);
		}
	}
