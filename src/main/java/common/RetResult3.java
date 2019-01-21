package common;

public class RetResult3<T, K> {

	public RetResult3(T result1, K result2, error err) {
		this.result1 = result1;
		this.result2 = result2;
		this.err = err;
	}

	public RetResult3<T, K> newRetResult3(T r, K s, error error) {
		return new RetResult3<T, K>(r, s, error);
	}

	public T result1;
	public K result2;
	public error err;
}
