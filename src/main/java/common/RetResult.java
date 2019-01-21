package common;

/**
 * A generic returned result with a result of type T and a possible returned error
 *
 * @param <T>
 */
public class RetResult<T> {
	/** these fields are made public for faster access */
	public T result;
	public error err;

	public RetResult() {
		this.result = null;
		this.err = null;
	}

	public RetResult(T result, error err) {
		this.result = result;
		this.err = err;
	}
}
