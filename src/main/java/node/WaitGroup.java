package node;

public class WaitGroup {
	private int jobs = 0;

	public WaitGroup() {

	}

	public synchronized void add(int i) {
		jobs += i;
	}

	public synchronized void done() {
		if (--jobs == 0) {
			notifyAll();
		}
	}

	public synchronized void await() throws InterruptedException {
		while (jobs > 0) {
			wait();
		}
	}
}