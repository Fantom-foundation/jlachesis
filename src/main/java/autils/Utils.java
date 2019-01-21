package autils;

import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.Random;

/**
 * Utils file
 */
public class Utils {

	private static Random random = new Random();

	public static Random random() {
		return random;
	}

	/**
	 * Function referred from: http://www.baeldung.com/sha-256-hashing-java
	 */
	public static String hash(String input) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			byte[] hash = digest.digest(input.getBytes("UTF-8"));
			StringBuilder hexString = new StringBuilder();

			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String getStringFromKey(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}

	public static boolean verifyECDSASig(PublicKey pb, String data, byte[] signature) throws Exception {
		Signature ecdsa = Signature.getInstance("ECDSA", "BC");
		ecdsa.initVerify(pb);
		ecdsa.update(data.getBytes());
		return ecdsa.verify(signature);
	}

	public static byte[] applyECDSASig(PrivateKey pr, String data) throws Exception {
		Signature ecdsa = Signature.getInstance("ECDSA", "BC");
		ecdsa.initSign(pr);
		ecdsa.update(data.getBytes());
		return ecdsa.sign();
	}

	public static String encode(byte[] bytes) {
		BigInteger bigInteger = new BigInteger(1, bytes);
		return bigInteger.toString(16);
	}

	public static byte[] decode(String hexString) {
		byte[] byteArray = new BigInteger(hexString, 16).toByteArray();
		if (byteArray[0] == 0) {
			byte[] output = new byte[byteArray.length - 1];
			System.arraycopy(byteArray, 1, output, 0, output.length);
			return output;
		}
		return byteArray;
	}
}
