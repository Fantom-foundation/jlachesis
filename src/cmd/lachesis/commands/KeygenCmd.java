package lachesis.commands;

import java.io.IOException;
import java.security.KeyPair;

import autils.FileUtils;
import common.Cmd;
import common.RResult;
import common.error;
import crypto.PemKey;
import crypto.Utils;
import picocli.CommandLine.Option;

public class KeygenCmd extends Cmd {
	String privKeyFile;
	String pubKeyFile;
	CLIConfig config = new CLIConfig();
	String defaultPrivateKeyFile = String.format("%s/priv_key.pem", config.Lachesis.DataDir);
	String defaultPublicKeyFile  = String.format("%s/key.pub", config.Lachesis.getDataDir());

	// NewKeygenCmd produces a KeygenCmd which creates a key pair
	public KeygenCmd() {
		setUse("keygen");
		setShort("Create new key pair");
	}

	public error run(Cmd cmd, String[] args) {
		// Create a new Options class, which holds our flags.
        KeyGenOptions options = new KeyGenOptions();
        error err = common.Utils.populateOptions(options, args);
		if (err != null)
			return err;
		privKeyFile = options.getPemFile();
        pubKeyFile = options.getPubFile();

		return keygen(cmd, args);
	}

	/**
	 * Main container of Keygen command's options
	 */
	public class KeyGenOptions {
		CLIConfig config = new CLIConfig();

		@Option(names = { "-pem", "--pem"}, description = "File where the private key will be written")
	    private String pemFile = defaultPrivateKeyFile;

	    @Option(names = {"-pub", "--pub"}, description= "File where the public key will be written")
	    private String pubFile = defaultPublicKeyFile;

		public String getPemFile() {
			return pemFile;
		}

		public String getPubFile() {
			return pubFile;
		}
	}

	public error keygen(Cmd cmd, String[] args) {
		RResult<KeyPair> generateKey = crypto.Utils.GenerateECDSAKeyPair();
		KeyPair keyPair = generateKey.result;
		error err = generateKey.err;
		if (err != null) {
			return error.Errorf("error generating PemDump");
		}
		// 0700
		err = FileUtils.mkdirs(privKeyFile, FileUtils.MOD_700).err;
		if (err != null) {
			return error.Errorf(String.format("writing private key: %s", err));
		}

		boolean fileExist = FileUtils.fileExist(privKeyFile);
		if (fileExist) {
			return error.Errorf(String.format("A key already lives under: %s", privKeyFile));
		}

		String ecString = "";
		try {
			ecString = PemKey.toECString(keyPair.getPrivate());
		} catch (IOException e) {
			return error.Errorf("error output private key's string" + e.getMessage());
		}

		err = FileUtils.writeToFile(privKeyFile, ecString.getBytes(), FileUtils.MOD_666);
		if (err != null) {
			return error.Errorf(String.format("writing private key: %s", err));
		}
		System.out.printf("Your private key has been saved to: %s\n", privKeyFile);

		err = FileUtils.mkdirs(pubKeyFile, FileUtils.MOD_700).err;
		if  (err != null) {
			return error.Errorf(String.format("writing public key: %s", err));
		}

		err = FileUtils.writeToFile(pubKeyFile, Utils.keyToHexString(keyPair.getPublic()).getBytes(), FileUtils.MOD_666);
		if  (err != null) {
			return error.Errorf(String.format("writing public key: %s", err));
		}
		System.out.printf("Your public key has been saved to: %s\n", pubKeyFile);
		return null;
	}
}
