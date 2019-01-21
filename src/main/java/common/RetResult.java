package common;

public class RetResult<T> {

	public <T> RetResult() {
		this.result = null;
		this.err = null;
	}

	public RetResult(T result, error err) {
		this.result = result;
		this.err = err;
	}

	public T result;
	public error err;
}
