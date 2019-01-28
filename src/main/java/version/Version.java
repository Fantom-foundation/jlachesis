package version;

/**
 * Current version of jLachesis
 */
public class Version {
	private int major = 0;
	private int min = 4;
	private int fix = 3;

	// GitCommit is set with: -ldflags "-X main.gitCommit=$(git rev-parse HEAD)"
	String gitCommit = "";
	String version;

	private Version() {
		version = String.join(".",  "major", "min", "fix") + gitCommit;
	}

	public static final Version instance = new Version();

	public static Version getInstance() {
		return instance;
	}

	public String getVersion() {
		return version;
	}
}
