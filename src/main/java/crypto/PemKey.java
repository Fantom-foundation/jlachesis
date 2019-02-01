package crypto;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemWriter;

import autils.FileUtils;
import common.RetResult;
import common.error;

/**
 * PemKey loads from the .pem file.
 *
 */
public class PemKey {
	String path;

	public static final String pemKeyPath = "priv_key.pem";

	public PemKey(String base) {
		path = Paths.get(base, pemKeyPath).toString();
	}

	public synchronized RetResult<KeyPair> ReadKeyPair() {
		try {
			KeyPair keyPairFromPEM = keyPairFromPEM(path);
			return new RetResult<>(keyPairFromPEM, null);
		} catch (IllegalArgumentException | IOException | NullPointerException e) {
			return new RetResult<>(null, error.Errorf(e.getMessage()));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			return new RetResult<>(null, error.Errorf(e.getMessage()));
		}
	}

	private static KeyPair keyPairFromPEM(String pem) throws IllegalArgumentException, IOException, NullPointerException, InvalidKeySpecException, NoSuchAlgorithmException {
		Security.addProvider(new BouncyCastleProvider());

		PEMParser pemParser = new PEMParser(new FileReader(pem));
		PEMKeyPair bcKeyPair = (PEMKeyPair) pemParser.readObject();
		pemParser.close();

	    KeyFactory kf;
//			kf = KeyFactory.getInstance("ECDSA", "BC");
		kf = KeyFactory.getInstance("ECDSA");
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bcKeyPair.getPrivateKeyInfo().getEncoded());
	    PrivateKey key = kf.generatePrivate(keySpec);

	    X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(bcKeyPair.getPublicKeyInfo().getEncoded());
	    PublicKey ecPublicKey = kf.generatePublic(x509EncodedKeySpec);

		return new KeyPair(ecPublicKey, key);
	}

	public synchronized RetResult<PrivateKey> ReadKey() {
		try {
			RSAPrivateKey privateKey = RSA.getPrivateKey(path);
			return new RetResult<>(privateKey, null);
		} catch (IOException | GeneralSecurityException e) {
			return new RetResult<>(null, error.Errorf(e.getMessage()));
		}
	}

//	public synchronized RetResult<PrivateKey> ReadKey() {
//		RetResult<byte[]> readResult = FileUtils.readFileToByteArray(path);
//
//		byte[] buf = readResult.result;
//		error err = readResult.err;
//
//		if (err != null) {
//			return new RetResult<PrivateKey>(null, err);
//		}
//
//		return ReadKeyFromBuf(buf);
//	}

//	public synchronized RetResult<PrivateKey> ReadKeyFromBuf(byte[] buf) {
//		if (buf.length == 0) {
//			return new RetResult<PrivateKey>(null, null);
//		}
//
//		PrivateKey generatePrivateKey;
//		try {
//			generatePrivateKey = ECDHPub.generatePrivateKey(buf);
//		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
//			return new RetResult<PrivateKey>(null, error.Errorf(e.getMessage()));
//		}
//
////		block, _ = crypto.Utils.Decode(buf);
////		if (block == null) {
////			return new RetResult<PrivateKey>(null, error.Errorf("error decoding PEM block from data"));
////		}
////		return x509.ParseECPrivateKey(blocBytes);
//
//		return new RetResult<PrivateKey>(generatePrivateKey, null);
//	}

	public synchronized error WriteKey(KeyPair keyPair) {
	    StringWriter stringWriter = new StringWriter();
	    PemWriter pemWriter = new PemWriter(stringWriter);
	    PemObjectGenerator priPemObject = new PemObject("EC " + "private".toUpperCase() + " KEY", keyPair.getPrivate().getEncoded());
	    // PemObjectGenerator pubPemObject = new PemObject("EC " + "public".toUpperCase() + " KEY", keyPair.getPublic().getEncoded());

	    error err = null;
	    try {
	    	pemWriter.writeObject(priPemObject);
//			pemWriter.writeObject(pubPemObject);
		    pemWriter.flush();
		    pemWriter.close();
		} catch (IOException e) {
			err = error.Errorf(e.getMessage());
		}

	    String keyString = stringWriter.toString();
	    // 700
	    err = FileUtils.writeToFile(path, keyString.getBytes(), FileUtils.MOD_700);
	    return err;
	}

	public static String toPEMString(Key key) throws IOException {
		StringWriter stringWriter = new StringWriter();
	    PemWriter pemWriter = new PemWriter(stringWriter);
	    PemObjectGenerator priPemObject = new PemObject("EC " + "private".toUpperCase() + " KEY", key.getEncoded());
    	pemWriter.writeObject(priPemObject);
	    pemWriter.flush();
	    pemWriter.close();
	    return stringWriter.toString();
	}

	public static String toECString(PrivateKey key) throws IOException {
		StringWriter stringWriter = new StringWriter();
        PemWriter w = new PemWriter (stringWriter);
        PrivateKeyInfo i = PrivateKeyInfo.getInstance(ASN1Sequence.getInstance(key.getEncoded()));
//        if( ! i.getPrivateKeyAlgorithm().getAlgorithm().equals(X9ObjectIdentifiers.id_ecPublicKey) ){
//            throw new Exception ("not EC key");
//        }
        ASN1Object o = (ASN1Object) i.parsePrivateKey();
        w.writeObject (new PemObject ("EC PRIVATE KEY", o.getEncoded("DER")));
        w.close();
        return stringWriter.toString();
	}

	public static RetResult<PemDump> GeneratePemKey() {
		RetResult<KeyPair> ecdsa = Utils.GenerateECDSAKeyPair();
		KeyPair keyPair = ecdsa.result;
		error err = ecdsa.err;
		if (err != null) {
			return new RetResult<PemDump>(null, err);
		}
		return ToPemKey(keyPair);
	}

	public static RetResult<PemDump> ToPemKey(KeyPair keyPair) {
		// TODO
//		String pub = String.format("0x%X", Utils.FromECDSAPub(priv.PublicKey));;
//		b, err := x509.MarshalECPrivateKey(priv);
//		if (err != null) {
//			return new RetResult<PemDump>(null, err);
//		}
//		pem.Block pemBlock = pem.newBlock("EC PRIVATE KEY", b);
//		data = EncodeToMemory(pemBlock);
//		String publicKey = String.format("0x%X", Utils.FromECDSAPub(keyPair.getPublic()));

		String publicKey = Utils.keyToHexString(keyPair.getPublic());
		String privateKey = Utils.keyToHexString(keyPair.getPrivate());

		//System.out.println("PemDump publicKey = " + publicKey);
		//System.out.println("PemDump privateKey = " + privateKey);

		error err = null;
		return new RetResult<PemDump>(new PemDump(publicKey, privateKey), err);
	}
}
