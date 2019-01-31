package crypto;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;

public class ECDHPub {

	static {
		// you need to add the BouncyCastle provider to use its functionalities
		Security.addProvider(new BouncyCastleProvider());
	}

	public static ECNamedCurveSpec getP256Spec() {
		// spec for P-256 curve
		ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("prime256v1");

		// creates curve parameters (spec variable)
		ECNamedCurveSpec params = new ECNamedCurveSpec("prime256v1", spec.getCurve(), spec.getG(), spec.getN());
		return params;
	}

	public static KeyPair generateECDSAKeyPair()
			throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
//		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "SC");
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
//		ECGenParameterSpec spec = new ECGenParameterSpec("secp256r1");
		ECGenParameterSpec spec = new ECGenParameterSpec("secp256k1");
		keyPairGenerator.initialize(spec, new SecureRandom());
		return keyPairGenerator.generateKeyPair();
	}

	public static PrivateKey generatePrivateKey() throws InvalidKeySpecException, NoSuchAlgorithmException,
			NoSuchProviderException, InvalidAlgorithmParameterException {
		KeyPair generateECDSAKeyPair = generateECDSAKeyPair();
		return generateECDSAKeyPair.getPrivate();
	}

	public static PrivateKey generatePrivateKey(byte[] keyBin)
			throws InvalidKeySpecException, NoSuchAlgorithmException {
		ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");
		KeyFactory kf = KeyFactory.getInstance("ECDSA", new BouncyCastleProvider());
		ECNamedCurveSpec params = new ECNamedCurveSpec("secp256k1", spec.getCurve(), spec.getG(), spec.getN());
		ECPrivateKeySpec privKeySpec = new ECPrivateKeySpec(new BigInteger(keyBin), params);
		return kf.generatePrivate(privKeySpec);
	}

	public static PublicKey generatePublicKey(byte[] keyBin) throws InvalidKeySpecException, NoSuchAlgorithmException {
		ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");
		KeyFactory kf = KeyFactory.getInstance("ECDSA", new BouncyCastleProvider());
		ECNamedCurveSpec params = new ECNamedCurveSpec("secp256k1", spec.getCurve(), spec.getG(), spec.getN());
		ECPoint point = ECPointUtil.decodePoint(params.getCurve(), keyBin);
		ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, params);
		return kf.generatePublic(pubKeySpec);
	}

	////////////
	// Use these methods to regenerate PublicKey and PrivateKey
	//
	//////////

	public static ECPublicKey generateECDSAPublicKey(byte[] byte_pubkey) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
	    String str_key = Base64.getEncoder().encodeToString(byte_pubkey);
	    byte_pubkey  = Base64.getDecoder().decode(str_key);
	    KeyFactory factory = KeyFactory.getInstance("ECDSA", "BC");
	    ECPublicKey public_key = (ECPublicKey) factory.generatePublic(new X509EncodedKeySpec(byte_pubkey));
	    return public_key;
	}

	public static ECPrivateKey generateECDSAPrivateKey(byte[] byte_privkey) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
	    String str_key = Base64.getEncoder().encodeToString(byte_privkey);
	    byte_privkey  = Base64.getDecoder().decode(str_key);
	    KeyFactory factory = KeyFactory.getInstance("ECDSA", "BC");
	    ECPrivateKey public_key = (ECPrivateKey) factory.generatePrivate(new PKCS8EncodedKeySpec(byte_privkey));
	    return public_key;
	}

	public static byte[] getDecodedBytes(byte[] bytes) {
		String str_key = Base64.getEncoder().encodeToString(bytes);
	    byte[] byteArray  = Base64.getDecoder().decode(str_key);
	    return byteArray;
	}

	/////

	public static byte[] sign(PrivateKey privateKey, String message) throws Exception {
		Signature signature = Signature.getInstance("SHA1withECDSA");
		signature.initSign(privateKey);
		signature.update(message.getBytes());

		return signature.sign();
	}

	public static boolean verify(PublicKey publicKey, byte[] signed, String message) throws Exception {
		Signature signature = Signature.getInstance("SHA1withECDSA");
		signature.initVerify(publicKey);
		signature.update(message.getBytes());

		return signature.verify(signed);
	}

	public static BigInteger extractR(byte[] signature) {
		int startR = (signature[1] & 0x80) != 0 ? 3 : 2;
		int lengthR = signature[startR + 1];
		return new BigInteger(Arrays.copyOfRange(signature, startR + 2, startR + 2 + lengthR));
	}

	public static BigInteger extractS(byte[] signature) {
		int startR = (signature[1] & 0x80) != 0 ? 3 : 2;
		int lengthR = signature[startR + 1];
		int startS = startR + 2 + lengthR;
		int lengthS = signature[startS + 1];
		return new BigInteger(Arrays.copyOfRange(signature, startS + 2, startS + 2 + lengthS));
	}

	/**
	 * Converts a private key into its corresponding public key.
	 */
	public static byte[] getPublicKey(byte[] privateKey) {
		try {
			ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");
			org.bouncycastle.math.ec.ECPoint pointQ = spec.getG().multiply(new BigInteger(1, privateKey));

			return pointQ.getEncoded(false);
		} catch (Exception e) {
			return new byte[0];
		}
	}

	public static ECPublicKey publicFromPrivate(final ECPrivateKey privateKey) throws Exception {

		KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
		ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");

		org.bouncycastle.math.ec.ECPoint Q = spec.getG()
				.multiply(((org.bouncycastle.jce.interfaces.ECPrivateKey) privateKey).getD());

		org.bouncycastle.jce.spec.ECPublicKeySpec pubSpec = new org.bouncycastle.jce.spec.ECPublicKeySpec(Q, spec);
		PublicKey publicKeyGenerated = keyFactory.generatePublic(pubSpec);
		return (ECPublicKey) publicKeyGenerated;
	}

	////

	private static ECPublicKey decodeECPublicKey(ECParameterSpec params, final byte[] pubkey)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		int keySizeBytes = params.getOrder().bitLength() / Byte.SIZE;

		int offset = 0;
		BigInteger x = new BigInteger(1, Arrays.copyOfRange(pubkey, offset, offset + keySizeBytes));
		offset += keySizeBytes;
		BigInteger y = new BigInteger(1, Arrays.copyOfRange(pubkey, offset, offset + keySizeBytes));
		ECPoint w = new ECPoint(x, y);

		ECPublicKeySpec otherKeySpec = new ECPublicKeySpec(w, params);
		KeyFactory keyFactory = KeyFactory.getInstance("EC");
		ECPublicKey otherKey = (ECPublicKey) keyFactory.generatePublic(otherKeySpec);
		return otherKey;
	}

	private static byte[] encodeECPublicKey(ECPublicKey pubKey) {
		int keyLengthBytes = pubKey.getParams().getOrder().bitLength() / Byte.SIZE;
		byte[] publicKeyEncoded = new byte[2 * keyLengthBytes];

		int offset = 0;

		BigInteger x = pubKey.getW().getAffineX();
		byte[] xba = x.toByteArray();
		if (xba.length > keyLengthBytes + 1 || xba.length == keyLengthBytes + 1 && xba[0] != 0) {
			throw new IllegalStateException("X coordinate of EC public key has wrong size");
		}

		if (xba.length == keyLengthBytes + 1) {
			System.arraycopy(xba, 1, publicKeyEncoded, offset, keyLengthBytes);
		} else {
			System.arraycopy(xba, 0, publicKeyEncoded, offset + keyLengthBytes - xba.length, xba.length);
		}
		offset += keyLengthBytes;

		BigInteger y = pubKey.getW().getAffineY();
		byte[] yba = y.toByteArray();
		if (yba.length > keyLengthBytes + 1 || yba.length == keyLengthBytes + 1 && yba[0] != 0) {
			throw new IllegalStateException("Y coordinate of EC public key has wrong size");
		}

		if (yba.length == keyLengthBytes + 1) {
			System.arraycopy(yba, 1, publicKeyEncoded, offset, keyLengthBytes);
		} else {
			System.arraycopy(yba, 0, publicKeyEncoded, offset + keyLengthBytes - yba.length, yba.length);
		}

		return publicKeyEncoded;
	}
}
