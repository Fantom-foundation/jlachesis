package node;

import java.time.Duration;
import java.util.Random;

import org.jcsp.lang.Alternative;
import org.jcsp.lang.CSTimer;
import org.jcsp.lang.Channel;
import org.jcsp.lang.Guard;
import org.jcsp.lang.One2OneChannel;
import org.jcsp.lang.One2OneChannelInt;

import channel.ChannelUtils;

public class ControlTimer {
	timerFactory timerFactory;
	One2OneChannelInt tickCh; //       chan struct{} //sends a signal to listening process
	One2OneChannel<Duration> resetCh; //     chan time.Duration //receives instruction to reset the heartbeatTimer
	One2OneChannelInt stopCh; //       chan struct{} //receives instruction to stop the heartbeatTimer
	One2OneChannelInt shutdownCh; //   chan struct{} //receives instruction to exit Run loop
	boolean set; //          bool


	public interface timerFactory {
		public CSTimer read(Duration t); // <-chan time.Time;
	}

	public ControlTimer(timerFactory timerFactory) {
		this.timerFactory = timerFactory;
		this.tickCh = Channel.one2oneInt(); // make(chan struct{});
		this.resetCh = Channel.one2one();// make(chan time.Duration);
		this.stopCh = Channel.one2oneInt();//  make(chan struct{});
		this.shutdownCh = Channel.one2oneInt(); //  make(chan struct{}),;
	}

	public static ControlTimer RandomControlTimer() {
		CSTimer tim = new CSTimer();
		Random random = new Random();

		timerFactory randomTimeout = new timerFactory() {
			public CSTimer read(Duration min) /* <-chan time.Time */ {
				if (min == null || min.isNegative()) {
					return null;
				}

				long extra = random.nextLong() % min.toMillis();
				long alarmTime = tim.read() + min.toMillis() + extra;
				tim.setAlarm(alarmTime);
				return tim;
			}
		};

		return new ControlTimer(randomTimeout);
	}

	public void Run(Duration init) {
		timerFactory setTimer = new timerFactory() {
			public CSTimer read(Duration t) /* <-chan time.Time */ {
				set = true;
				return timerFactory.read(t);
			}
		};

		CSTimer timer = setTimer.read(init);

		while (true) {
			// TODO
//			select {
//			case <-timer:
//				tickCh <- struct{}{};
//				set = false;
//			case t:= <-resetCh:
//				timer = setTimer(t);
//			case <-stopCh:
//				timer = null;
//				set = false;
//			case <-shutdownCh:
//				set = false;
//				return;
//			}

			final Alternative alt = new Alternative (new Guard[] {timer, resetCh.in(), stopCh.in(), shutdownCh.in()});
			final int TIM = 0, RESET = 1, STOP = 2, SHUTDOWN = 3;
			switch (alt.priSelect ()) {
				// fall through
				case TIM:
					timer.read();
					tickCh.out().write(1); // <- struct{}{};
					break;
				case RESET:
					Duration t = resetCh.in().read();
					timer = setTimer.read(t);
					break;
				case STOP:
					stopCh.in().read();
					timer = null;
					set = false;
					break;
				case SHUTDOWN:
					shutdownCh.in().read();
					set = false;
					return;
			}
		}
	}

	public void Shutdown() {
		ChannelUtils.close(shutdownCh);
	}
}