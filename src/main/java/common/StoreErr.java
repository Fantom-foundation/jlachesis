package common;

public class StoreErr extends error {
	String dataType;
	StoreErrType errType;
	String key;

	public StoreErr(String dataType, StoreErrType errType, String key) {
		super("");
		this.dataType = dataType;
		this.errType = errType;
		this.key = key;
		super.setErrMessage(Error());
	}

	public StoreErr NewStoreErr(String dataType, StoreErrType errType, String key) {
		return new StoreErr(dataType, errType, key);
	}

	public error storeErr() {
		return new error(Error());
	}

	public String Error() {
		String m = errType.getM();

		return dataType + ", " + key + ", " + m;
	}

	public static error newStoreErr(String dataType, StoreErrType errType, String key) {
		return new StoreErr(dataType, errType, key);
	}

	public static boolean Is(error err, StoreErrType t) {
		if (err == null) {
			return false;
		}

		if (StoreErr.class.isAssignableFrom(err.getClass())) {
			StoreErr storeErr = (StoreErr) err;
			return storeErr.errType == t;
		}

		return false;
	}
}