package common;

import org.jcsp.lang.Alternative;
import org.jcsp.lang.CSTimer;
import org.jcsp.lang.Guard;

/**
 *
 * @param <T>
 */
public class SingSelectors {
	protected final CSTimer tim;
	protected final int TIM = 0;

	public SingSelectors(CSTimer tim) {
		this.tim = tim;
	}

	public SingSelectors() {
		this.tim = new CSTimer();
	}

	public void run() {
		final Alternative alt = new Alternative (new Guard[] {tim});
		switch (alt.priSelect ()) {
			case TIM:
				onTimeOut();
			// fall through
			default:
				onDefault();
		}
	}

	public void onTimeOut() { }

	public void onDefault() { }
}