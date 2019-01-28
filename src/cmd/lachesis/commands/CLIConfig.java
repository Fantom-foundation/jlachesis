package lachesis.commands;

/**
 * CLIConfig contains configuration for running commands
 */
public class CLIConfig {
	lachesis.LachesisConfig Lachesis;
	String ProxyAddr; //  `mapstructure:"proxy-listen"`
	String ClientAddr; // `mapstructure:"client-connect"`
	boolean Standalone; // `mapstructure:"standalone"`
	boolean Log2file; // `mapstructure:"log2file"`

	//creates new CLIConfig creates a CLIConfig with default values
	public CLIConfig() {
		Lachesis =  lachesis.LachesisConfig.NewDefaultConfig();
		ProxyAddr =  "127.0.0.1:1338";
		ClientAddr = "127.0.0.1:1339";
		Standalone = false;
		Log2file =   false;
	}
}

