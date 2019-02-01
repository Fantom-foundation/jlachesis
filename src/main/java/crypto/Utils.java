package crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemWriter;

import autils.FileUtils;
import common.RetResult;
import common.RetResult3;
import common.error;

public class Utils {
	public static RetResult<PrivateKey> GenerateECDSAKey() {
		PrivateKey privKey;
		try {
			privKey = ECDHPub.generatePrivateKey();
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException
				| InvalidAlgorithmParameterException e) {
			return new RetResult<PrivateKey>(null, error.Errorf(e.getMessage()));
		}

		return new RetResult<PrivateKey>(privKey, null);
	}

	public static RetResult<KeyPair> GenerateECDSAKeyPair() {
		KeyPair privKey;
		try {
			privKey = ECDHPub.generateECDSAKeyPair();
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
			return new RetResult<KeyPair>(null, error.Errorf(e.getMessage()));
		}

		return new RetResult<KeyPair>(privKey, null);
	}

	// TBD the conversion using P256
//	public static PublicKey ToECDSAPub(byte[] pub) {
//		if (pub.length == 0) {
//			return null;
//		}
//		x, y := elliptic.Unmarshal(elliptic.P256(), pub);
//		return new PublicKey{Curve: elliptic.P256(), X: x, Y: y};
//	}

	public static PublicKey ToECDSAPub(byte[] publicBytes) {
		try {
			PublicKey pk = ECDHPub.generatePublicKey(publicBytes);
			return pk;
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e1) {
			e1.printStackTrace();
			return null;
		}
	}

	// TBD not working. Conversion is in progress
//	public static ECPublicKey ToECDSAPub(byte[] pub) {
//	    KeyFactory factory = KeyFactory.getInstance("ECDSA", "BC");
//	    java.security.PublicKey ecPublicKey = (ECPublicKey) factory
//	            .generatePublic(new X509EncodedKeySpec(Helper
//	                    .toByte(ecRemotePubKey)));
//	    return (ECPublicKey) ecPublicKey;
//	}

	public static PublicKey decodeRSAPublicKey(byte[] pubKeyBits)
			throws InvalidKeySpecException, NoSuchAlgorithmException {
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		// build the public key
		PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(pubKeyBits));
		return pubKey;
	}

	public static PrivateKey decodeRSAPrivateKey(byte[] privKeyBits)
			throws InvalidKeySpecException, NoSuchAlgorithmException {
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		// build the private key
		PrivateKey privKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privKeyBits));
		return privKey;
	}

//	public static PublicKey ToECDSAPub_old(byte[] publicBytes) {
//		ECNamedCurveSpec params = ECDHPub.getP256Spec();
//		ECPoint point = ECPointUtil.decodePoint(params.getCurve(), publicBytes);
//		ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, params);
//
//		KeyFactory kf;
//		ECPublicKey pk = null;
//		try {
//			kf = KeyFactory.getInstance("ECDSA", BouncyCastleProvider.PROVIDER_NAME);
//			pk = (ECPublicKey) kf.generatePublic(pubKeySpec);
//		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
//			e.printStackTrace();
//			return null;
//		} catch (InvalidKeySpecException e) {
//			e.printStackTrace();
//			return null;
//		}
//
//		return pk;
//	}

	public static byte[] FromECDSAPub(PublicKey pub) {
		// TODO simple?
//		if (pub == null || pub.X == null || pub.Y == null) {
//			return null;
//		}
//		return elliptic.Marshal(elliptic.P256(), pub.X, pub.Y);

		return pub.getEncoded();
	}

	public static RetResult3<BigInteger, BigInteger> Sign(PrivateKey priv, byte[] hash) {
		Signature ver;
		try {
//			ver = Signature.getInstance("SHA256withECDSA");
			ver = Signature.getInstance("ECDSA", "BC");
			ver.initSign(priv, new SecureRandom());
			ver.update(hash);
			byte[] signature = ver.sign();

			BigInteger R = ECDHPub.extractR(signature);
			BigInteger S = ECDHPub.extractS(signature);
			return new RetResult3<BigInteger, BigInteger>(R, S, null);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchProviderException e) {
			return new RetResult3<BigInteger, BigInteger>(null, null,
					error.Errorf("Signature Verification failed" + e.getMessage()));
			// throw new Exception("Signature Verification failed");
		}
	}

	public static boolean Verify(PublicKey pub, byte[] hash, BigInteger r, BigInteger s) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DEROutputStream derOutputStream = new DEROutputStream(byteArrayOutputStream);
		ASN1EncodableVector v = new ASN1EncodableVector();
		v.add(new ASN1Integer(r));
		v.add(new ASN1Integer(s));
		try {
			derOutputStream.writeObject(new DERSequence(v));
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] derSignature = byteArrayOutputStream.toByteArray();

		Signature ver;
		try {
//			ver = Signature.getInstance("SHA256withECDSA");
			ver = Signature.getInstance("ECDSA", "BC");
			ver.initVerify(pub);
			ver.update(hash);
			boolean verify = ver.verify(derSignature);
			return verify;
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchProviderException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String encodeSignature(BigInteger r, BigInteger s) {
		return String.format("%s|%s", r.toString(36), s.toString(36));
	}

	public static RetResult3<BigInteger, BigInteger> DecodeSignature(String sig) {
		BigInteger r = null;
		BigInteger s = null;

		String[] values = sig.split("\\|");
		if (values.length != 2) {
			return new RetResult3<BigInteger, BigInteger>(r, s,
					error.Errorf(String.format("wrong number of values in signature: got %d, want 2", values.length)));
		}

		r = new BigInteger(values[0], 36);
		s = new BigInteger(values[1], 36);
		return new RetResult3<BigInteger, BigInteger>(r, s, null);
	}


	/**
	 * Get the output string of the following forms
	 * -----BEGIN EC PRIVATE KEY-----
	 * MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgJCBV6nJJ4xJAP4In
	 * m1BGbfLTMBUJ7u5gv7IICwndt/agCgYIKoZIzj0DAQehRANCAARqNH8EiKvH2S4i
	 * CHlOMn7KFbDCsnAYsrW4ndjLc2/XzDjzfS0QgiUwrZc1msvYN6ZcLKYtRLDOVpvS
	 * IsLavyaP
	 * -----END EC PRIVATE KEY-----
	 *
	 * or
	 *
	 * -----BEGIN EC PUBLIC KEY-----
	 * MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEajR/BIirx9kuIgh5TjJ+yhWwwrJw
	 * GLK1uJ3Yy3Nv18w4830tEIIlMK2XNZrL2DemXCymLUSwzlab0iLC2r8mjw==
	 * -----END EC PUBLIC KEY-----
	 *
	 * @param key
	 * @param ec
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public static String getOutputString(Key key, String ec, String type) throws IOException {
	    StringWriter stringWriter = new StringWriter();
	    PemWriter pemWriter = new PemWriter(stringWriter);
	    PemObjectGenerator pemObject = new PemObject(ec.toUpperCase() + " " + type.toUpperCase() + " KEY", key.getEncoded());
    	pemWriter.writeObject(pemObject);
	    pemWriter.flush();
	    pemWriter.close();
	    return stringWriter.toString();
	}

	/**
	 * Public:
	 * MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgJCBV6nJJ4xJAP4Inm1BGbfLTMBUJ
	 * 7u5gv7IICwndt/agCgYIKoZIzj0DAQehRANCAARqNH8EiKvH2S4iCHlOMn7KFbDCsnAYsrW4ndjL
	 * c2/XzDjzfS0QgiUwrZc1msvYN6ZcLKYtRLDOVpvSIsLavyaP
	 *
	 * Private:
	 * MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEajR/BIirx9kuIgh5TjJ+yhWwwrJwGLK1uJ3Yy3Nv
	 * 18w4830tEIIlMK2XNZrL2DemXCymLUSwzlab0iLC2r8mjw==
	 *
	 * @param key
	 * @return
	 */
	public static String getMimeString(Key key) {
		return Base64.getMimeEncoder().encodeToString(key.getEncoded());
	}

	public static RetResult<byte[]> decodeString(String s) {
		BigInteger bigInteger = null;
		try {
			bigInteger = new BigInteger(s, 16);
		} catch (NumberFormatException nfe) {
			return new RetResult<byte[]>(bigInteger.toByteArray(), error.Errorf(nfe.toString()));
		}
		return new RetResult<byte[]>(bigInteger.toByteArray(), null);
	}

	public static String keyToString(Key key) {
		byte encoded[] = key.getEncoded();
		String encodedKey = Base64.getEncoder().encodeToString(encoded);
		return encodedKey;
	}

	public static String keyToHexString(Key key) {
		return toHexString(key.getEncoded());
	}

	public static String toHexString(byte[] encoded) {
//		String encodedKey = String.format("0x%X", encoded);
		String encodedKey = Base64.getEncoder().encodeToString(encoded);
		byte[] decoded = Base64.getDecoder().decode(encodedKey);
		String hexString = String.format("%040X", new BigInteger(1, decoded));
		return hexString;
	}
}
