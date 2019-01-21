package crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import autils.Appender;

public class hash {
	public static byte[] SHA256(byte[] hashBytes) {
		// TBD: is the transformation ok?
//		hasher := sha256.New();
//		hasher.Write(hashBytes);
//		hash := hasher.Sum(null);
//		return hash;

		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] encodedhash = digest.digest(hashBytes);
			return encodedhash;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] SimpleHashFromTwoHashes(byte[] left, byte[] right) {
		// TBD: is the transformation ok?
//		hasher = sha256.New();
//		hasher.Write(left);
//		hasher.Write(right);
//		return hasher.Sum(null);

		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] encodedLeft = digest.digest(left);
			byte[] encodedRight = digest.digest(right);
			return Appender.append(encodedLeft, encodedRight);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public byte[] SimpleHashFromHashes(byte[][] hashes) {
		// Recursive impl.
		switch (hashes.length) {
		case 0:
			return null;
		case 1:
			return hashes[0];
		default:
			byte[] left = SimpleHashFromHashes(
					Arrays.copyOfRange(hashes, 0, (hashes.length+1)/2));
			byte[] right = SimpleHashFromHashes(
					Arrays.copyOfRange(hashes, (hashes.length+1)/2, hashes.length-1));
			return SimpleHashFromTwoHashes(left, right);
		}
	}
}
