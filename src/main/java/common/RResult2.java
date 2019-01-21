package common;

public class RResult2<T, K> {

	public RResult2(T result1, K result2) {
		this.result1 = result1;
		this.result2 = result2;
	}

	public RResult2<T, K> newRetResult3(T r, K s) {
		return new RResult2<T, K>(r, s);
	}

	public T result1;
	public K result2;
}
