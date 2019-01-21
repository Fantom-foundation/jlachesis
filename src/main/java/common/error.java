package common;

public class error {

	private String errMessage;

	public error(String errMessage) {
		this.errMessage = errMessage;
	}

	public String getErrMessage() {
		return errMessage;
	}

	public void setErrMessage(String errMessage) {
		this.errMessage = errMessage;
	}

	public String Error() {
		return errMessage;
	}

	public static error Errorf(String string) {
		return new error(string);
	}

	public static void panic(error err) {
		System.err.println(err);
		System.exit(-1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((errMessage == null) ? 0 : errMessage.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		error other = (error) obj;
		if (errMessage == null) {
			if (other.errMessage != null)
				return false;
		} else if (!errMessage.equals(other.errMessage))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "error [errMessage=" + errMessage + "]";
	}
}
