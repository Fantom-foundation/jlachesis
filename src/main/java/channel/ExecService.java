package channel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecService {
	private static ExecutorService executor = Executors.newCachedThreadPool();

	private ExecService() {
	}

	public static Future<?> go(Runnable runnable) {
		return executor.submit(runnable);
	}
}
