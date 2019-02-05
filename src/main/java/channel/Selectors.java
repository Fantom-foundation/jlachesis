package channel;

import org.jcsp.lang.Alternative;
import org.jcsp.lang.CSTimer;
import org.jcsp.lang.Guard;
import org.jcsp.lang.One2OneChannel;

/**
 *
 * @param <T>
 */
public class Selectors<T> {
	protected final One2OneChannel<T> ch;
	protected final CSTimer tim = new CSTimer();
	protected final int EVENT = 0, TIM = 1;

	public Selectors(One2OneChannel<T> ch) {
		this.ch = ch;
	}

	public void run() {
		final Alternative alt = new Alternative (new Guard[] {ch.in(), tim});
		switch (alt.priSelect ()) {
			case EVENT:
				onEvent();
			// fall through
			case TIM:
				onTimeOut();
		}
	}

	public void onEvent() {	}

	public void onTimeOut() { }
}