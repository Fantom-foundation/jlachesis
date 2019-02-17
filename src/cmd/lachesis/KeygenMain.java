package lachesis;

import common.error;
import lachesis.commands.KeygenCmd;

public class KeygenMain {
	public static void main(String[] args) {
		KeygenCmd keygen = new KeygenCmd();
		error err = keygen.run(keygen, args);
		if (err != null) {
			System.out.println("error =" + err);
			System.exit(1);
		}
	}
}