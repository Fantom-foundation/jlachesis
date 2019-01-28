package dummy.commands;

/**
 * CLIConfig contains configuration for the Dummy command
 */
public class CLIConfig {
	final String Name; // `mapstructure:"name"`
	final String ClientAddr; // `mapstructure:"client-listen"`
	final String ProxyAddr; // `mapstructure:"proxy-connect"`
	final boolean Discard; //  `mapstructure:"discard"`
	final String LogLevel; // `mapstructure:"log"`

	//NewDefaultCLIConfig creates a CLIConfig with default values
	public CLIConfig() {
		Name = "Dummy";
		ClientAddr = "127.0.0.1:1339";
		ProxyAddr = "127.0.0.1:1338";
		Discard = false;
		LogLevel= "debug";
	}
}
