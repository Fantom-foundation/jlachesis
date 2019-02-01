package autils;

public class time {
	public static final int Nanosecond = 1;
	public static final int Microsecond = 1000 * Nanosecond;
	public static final int Millisecond = 1000 * Microsecond;
	public static final int Second = 1000 * Millisecond;
	public static final long Minute = 60 * Second;
	public static final long Hour = 60 * Minute;


	public static long Since(long start) {
		long current = System.nanoTime();
		return current - start;
	}
}
