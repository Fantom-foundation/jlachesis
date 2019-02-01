package crypto;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.junit.Test;

import autils.FileUtils;
import common.RetResult;
import common.RetResult3;
import common.error;

/**
 * Tests for Crypto functions
 *
 * @author qn
 *
 */
public class CryptoTest {

	@Test
	public void testPem() {
		// Create the PEM key
		String baseDir = "src/test/java/crypto/";
		String filePath = baseDir + "test_data/lachesis";
		try {
			Path parentDir = Paths.get(filePath);
			if (!Files.exists(parentDir))
				Files.createDirectory(parentDir);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		PemKey pemKey = new PemKey(filePath);
		//System.out.println("path = " +  pemKey.path);
		try {
			Files.delete(Paths.get(pemKey.path));
		} catch (IOException e1) {
			fail("Should not have thrown any exception" + e1);
		}
		FileUtils.createFile(pemKey.path, FileUtils.MOD_700);

		// Try a read, should get nothing
		RetResult<KeyPair> readKey = pemKey.ReadKeyPair();
		KeyPair keyPair = readKey.result;
		error err = readKey.err;
		assertNotNull("ReadKey should generate an error", err);
		assertNull("key should be null", keyPair);

		// Initialize a key
		try {
			keyPair = crypto.ECDHPub.generateECDSAKeyPair();
			err = pemKey.WriteKey(keyPair);
		} catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
			fail("Should not have thrown any exception" + e);
		}

		assertNull("writekey should have no error err =" + err, err);

		// Try a read, should get key
		readKey = pemKey.ReadKeyPair();
		KeyPair nKeyPair = readKey.result;
		err = readKey.err;

		// TODO
		//assertNull("ReadKey should have no error", err);
		//assertEquals("Keys should match", nKeyPair, keyPair);
	}

	@Test
	public void testReadPem() {
		// Create the PEM key
		String baseDir = "src/test/java/crypto/";
		String filePath = baseDir + "test_data/testkey";
		PemKey pemKey = new PemKey(filePath);

		// Try a read
		RetResult<KeyPair> readKey = pemKey.ReadKeyPair();
		KeyPair key = readKey.result;
		error err = readKey.err;
		assertNull("ReadKey should have no error", err);

		// Check that the resulting key is as expected
//		String pub = String.format("%02X", Utils.FromECDSAPub(key.getPublic()));

//		System.out.println("public key= " +  crypto.Utils.keyToString(key.getPublic()));
//		System.out.println("priv key= " +  crypto.Utils.keyToString(key.getPrivate()));
//		byte[] decoded = ECDHPub.getDecodedBytes(key.getPublic().getEncoded());
//		System.out.println("key.getPublic().getEncoded() = " +  Arrays.toString(key.getPublic().getEncoded()));
//		System.out.println("decoded = " +  Arrays.toString(decoded));
//		String pub = Hex.encodeHexString(decoded);

		String pub = new String(Hex.encodeHex(Utils.FromECDSAPub(key.getPublic()), false));
		String expectedPub = "3059301306072A8648CE3D020106082A8648CE3D030107034200046A347F0488ABC7D92E2208794E327ECA15B0C2B27018B2B5B89DD8CB736FD7CC38F37D2D10822530AD97359ACBD837A65C2CA62D44B0CE569BD222C2DABF268F";
		assertEquals("public key should be %s, not %s", expectedPub, pub);

		pub = Utils.keyToHexString(key.getPublic());
		assertEquals("public key should be %s, not %s", expectedPub, pub);

		String msg = "time for beer";
		RetResult3<BigInteger, BigInteger> sign = Utils.Sign(key.getPrivate(), msg.getBytes());

		BigInteger r = sign.result1;
		BigInteger s = sign.result2;
		//r and s are different every time

//		System.out.println("msg: " +  msg);
//		System.out.println("msg bytes: " + Arrays.toString(msg.getBytes()));
//		System.out.println("sig R bytes: " + Arrays.toString(r.toByteArray()));
//		System.out.println("sig S bytes: " + Arrays.toString(s.toByteArray()));
//		System.out.println("sig R: " + r);
//		System.out.println("sig S: " + s);

		String sigEncoded = Utils.encodeSignature(r, s);
//		System.out.printf("sig encoded: %s\n", sigEncoded);
//		System.out.printf("encoded signature length: %d\n", sigEncoded.getBytes().length);

		//(r2, s2) is another valid signature of the msg
		BigInteger r2 = new BigInteger("78441670540129529755648814670619364386938674096127498561112585467726523488290");
		BigInteger s2 = new BigInteger("23563287253252453955330730565106518753652686154399070621327758530430438184785");
//		System.out.println("sig R2: " + r2);
//		System.out.println("sig S2: " + s2);

		boolean verifySig2 = Utils.Verify(key.getPublic(), msg.getBytes(), r2, s2);
		assertTrue("(r2, s2) should also be a valid signature", verifySig2);
	}

	@Test
	public void testSignatureEncoding() {
		KeyPair generateECDSAKeyPair = null;
		try {
			generateECDSAKeyPair = crypto.ECDHPub.generateECDSAKeyPair();
		} catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
			fail("Should not have thrown any exception" + e);
		}
		PrivateKey privKey = generateECDSAKeyPair.getPrivate();

		String msg = "J'aime mieux forger mon ame que la meubler";
		byte[] msgBytes = msg.getBytes();
		byte[] msgHashBytes = crypto.hash.SHA256(msgBytes);

		RetResult3<BigInteger, BigInteger> sign = Utils.Sign(privKey, msgHashBytes);
		BigInteger r = sign.result1;
		BigInteger s = sign.result2;

		String encodedSig = Utils.encodeSignature(r, s);
		RetResult3<BigInteger, BigInteger> decodeSignature = Utils.DecodeSignature(encodedSig);

		BigInteger dr = decodeSignature.result1;
		BigInteger ds = decodeSignature.result2;
		error err = decodeSignature.err;

		assertNull("no error in decoding", err);
		//System.out.println("r: " + dr);
		//System.out.println("s: " + ds);
		//System.out.println("error decoding " + encodedSig);

		assertEquals("Signature Rs should be the same", r, dr);
		assertEquals("Signature Ss should be the same", s, ds);
	}

	@Test
	public void testECDHKeyRegeneration() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	    Reader rdr = new StringReader (
	    "-----BEGIN EC PRIVATE KEY-----\n" +
	    "MHcCAQEEICQgVepySeMSQD+CJ5tQRm3y0zAVCe7uYL+yCAsJ3bf2oAoGCCqGSM49\n" +
	    "AwEHoUQDQgAEajR/BIirx9kuIgh5TjJ+yhWwwrJwGLK1uJ3Yy3Nv18w4830tEIIl\n" +
	    "MK2XNZrL2DemXCymLUSwzlab0iLC2r8mjw==\n" +
	    "-----END EC PRIVATE KEY-----");

	    Object parsed = new PEMParser(rdr).readObject();
	    KeyPair pair = new JcaPEMKeyConverter().getKeyPair((PEMKeyPair)parsed);
	    //System.out.println (pair.getPrivate().getAlgorithm() + " : "  + pair.getPrivate().getFormat());

	    //converting public key to byte
	    byte[] pubkeyBytes = pair.getPublic().getEncoded();
	    ECPublicKey pubKey = ECDHPub.generateECDSAPublicKey(pubkeyBytes);
	    assertEquals("Regenerated keys are the same", pair.getPublic(), pubKey);

	    // Convert to Bytes then Hex for new account params
	    byte[] privkeyBytes = pair.getPrivate().getEncoded();
	    PrivateKey privKey = ECDHPub.generateECDSAPrivateKey(privkeyBytes);
	    assertEquals("Regenerated keys are the same", pair.getPrivate(), privKey);
	}

	@Test
	public void testKeyOutput() throws IOException {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	    final String originalPem = "-----BEGIN EC PRIVATE KEY-----\n" +
	    	    "MHcCAQEEICQgVepySeMSQD+CJ5tQRm3y0zAVCe7uYL+yCAsJ3bf2oAoGCCqGSM49\n" +
	    	    "AwEHoUQDQgAEajR/BIirx9kuIgh5TjJ+yhWwwrJwGLK1uJ3Yy3Nv18w4830tEIIl\n" +
	    	    "MK2XNZrL2DemXCymLUSwzlab0iLC2r8mjw==\n" +
	    	    "-----END EC PRIVATE KEY-----";
		Reader rdr = new StringReader (originalPem);
	    Object parsed = new PEMParser(rdr).readObject();
	    KeyPair pair = new JcaPEMKeyConverter().getKeyPair((PEMKeyPair)parsed);

	    //System.out.println ("priv = " + pair.getPrivate().getAlgorithm() + " : " + pair.getPrivate().getFormat());
	    //System.out.println ("pub = " +  pair.getPublic().getAlgorithm() + " : " + pair.getPublic().getFormat());

	    String expectedPrivHex = "308193020100301306072A8648CE3D020106082A8648CE3D030107047930770201010420242055EA7249E312403F82279B50466DF2D3301509EEEE60BFB2080B09DDB7F6A00A06082A8648CE3D030107A144034200046A347F0488ABC7D92E2208794E327ECA15B0C2B27018B2B5B89DD8CB736FD7CC38F37D2D10822530AD97359ACBD837A65C2CA62D44B0CE569BD222C2DABF268F";
	    String expectedPubHex = "3059301306072A8648CE3D020106082A8648CE3D030107034200046A347F0488ABC7D92E2208794E327ECA15B0C2B27018B2B5B89DD8CB736FD7CC38F37D2D10822530AD97359ACBD837A65C2CA62D44B0CE569BD222C2DABF268F";
	    assertEquals("private key's hex", Utils.keyToHexString(pair.getPrivate()), expectedPrivHex);
	    assertEquals("public key's hex", Utils.keyToHexString(pair.getPublic()), expectedPubHex);

        String expectedMime =
        		"MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgJCBV6nJJ4xJAP4In\n" +
				"m1BGbfLTMBUJ7u5gv7IICwndt/agCgYIKoZIzj0DAQehRANCAARqNH8EiKvH2S4i\n" +
				"CHlOMn7KFbDCsnAYsrW4ndjLc2/XzDjzfS0QgiUwrZc1msvYN6ZcLKYtRLDOVpvS\n" +
				"IsLavyaP";
        String outputKey  = Utils.getOutputString(pair.getPrivate(), "ec", "private");
        assertTrue("Output string contains", outputKey.contains(expectedMime));

        expectedMime = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEajR/BIirx9kuIgh5TjJ+yhWwwrJw\n" +
        		"GLK1uJ3Yy3Nv18w4830tEIIlMK2XNZrL2DemXCymLUSwzlab0iLC2r8mjw==";
        outputKey  = Utils.getOutputString(pair.getPublic(), "ec", "public");
        assertTrue("PEM output string contains", outputKey.contains(expectedMime));

        outputKey = PemKey.toECString(pair.getPrivate());
        assertTrue("EC output  string contains", outputKey.contains(originalPem));
	}
}