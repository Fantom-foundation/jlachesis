package crypto;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

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

	public PemKey (String base){
		path = Paths.get(base, pemKeyPath).toString();
	}

	public synchronized RetResult<KeyPair> ReadKeyPair() {
		try {
			KeyPair keyPairFromPEM = keyPairFromPEM(path);
			return new RetResult<>(keyPairFromPEM, null);
		} catch (IllegalArgumentException | IOException e) {
			return new RetResult<>(null, error.Errorf(e.getMessage()));
		}
	}

	private static KeyPair keyPairFromPEM(String pem) throws IllegalArgumentException, IOException {
			Security.addProvider(new BouncyCastleProvider());

			PEMParser pemParser = new PEMParser(new StringReader(pem));
			Object keyObject = pemParser.readObject();
			pemParser.close();

			JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
			KeyPair keys = converter.getKeyPair((PEMKeyPair) keyObject);
			return keys;
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
		RetResult<PemDump> toPemKey = ToPemKey(keyPair);
		PemDump pemKey = toPemKey.result;
		error err = toPemKey.err;
		if (err != null) {
			return err;
		}

		RetResult<File> fileCreation = FileUtils.createFile(path, 700);
		err = fileCreation.err;
		File file = fileCreation.result;
		if (err != null) {
			return err;
		}
		err = FileUtils.writeToFile(file, pemKey.PrivateKey.getBytes());
		return err;
	}

	public RetResult<PemDump> GeneratePemKey() {
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

		String publicKey = String.format("0x%X", Utils.FromECDSAPub(keyPair.getPublic()));
		String privateKey = Arrays.toString(keyPair.getPrivate().getEncoded());
		error err = null;
		return new RetResult<PemDump>(new PemDump(publicKey, privateKey), err);
	}
}
