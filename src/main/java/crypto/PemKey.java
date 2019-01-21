package crypto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;

import autils.FileUtils;
import common.RetResult;
import common.error;

//import (
//		"crypto/ecdsa"
//		"crypto/x509"
//		"encoding/pem"
//		"io/ioutil"
//		"sync"
//	)

public class PemKey {
	String path;

	public static final String pemKeyPath = "priv_key.pem";

	public PemKey (String base){
		path = Paths.get(base, pemKeyPath).toString();
	}

	public synchronized RetResult<PrivateKey> ReadKey() {
		RSAPrivateKey privateKey;
		error err;
		try {
			privateKey = RSA.getPrivateKey(path);
		} catch (IOException | GeneralSecurityException e) {
			err = error.Errorf(e.getMessage());
			return new RetResult<PrivateKey>(null, err);
		}

		return new RetResult<PrivateKey>(privateKey, null);
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

	public synchronized error WriteKey(PrivateKey key) {
		RetResult<PemDump> toPemKey = ToPemKey(key);
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
		RetResult<PrivateKey> ecdsa = Utils.GenerateECDSAKey();
		PrivateKey key = ecdsa.result;
		error err = ecdsa.err;
		if (err != null) {
			return new RetResult<PemDump>(null, err);
		}

		return ToPemKey(key);
	}

	public static RetResult<PemDump> ToPemKey(PrivateKey priv) {
		// TODO
//		String pub = String.format("0x%X", Utils.FromECDSAPub(priv.PublicKey));;
//		b, err := x509.MarshalECPrivateKey(priv);
//		if (err != null) {
//			return new RetResult<PemDump>(null, err);
//		}
//		pem.Block pemBlock = pem.newBlock("EC PRIVATE KEY", b);
//		data = EncodeToMemory(pemBlock);

		String publicKey = String.format("0x%X", Utils.FromECDSAPub(crypto.Utils.getPublicFromPrivate(priv)));
		String privateKey = Arrays.toString(priv.getEncoded());
		error err = null;
		return new RetResult<PemDump>(new PemDump(publicKey, privateKey), err);
	}
}
