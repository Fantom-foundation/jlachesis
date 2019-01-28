package common;

/**
 * A command
 */
public class Cmd implements Runnable {
	private String Use;
	private String Short;

	public Cmd() {
	}

	public String getUse() {
		return Use;
	}

	public void setUse(String use) {
		Use = use;
	}

	public String getShort() {
		return Short;
	}

	public void setShort(String s) {
		Short = s;
	}

	public void run() {
		// TODO Auto-generated method stub
	}
}
