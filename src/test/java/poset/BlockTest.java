package poset;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import org.junit.Test;

import common.RResult;
import common.error;
import crypto.Utils;

/**
 * Test of block
 *
 * @author qn
 *
 */
public class BlockTest {
	@Test
	public void TestSignBlock() {
		KeyPair privateKey = null;
		try {
			privateKey = crypto.ECDHPub.generateECDSAKeyPair();
		} catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
			fail("Should be no exception when creating key");
		}

		Block block = new Block(0, 1,
			"framehash".getBytes(),
			new byte[][]{
				"abc".getBytes(),
				"def".getBytes(),
				"ghi".getBytes(),
			});

		RResult<BlockSignature> signCall = block.sign(privateKey);
		BlockSignature sig = signCall.result;
		error err = signCall.err;
		assertNull("No error when signing", err);

		RResult<Boolean> verifyCall = block.verify(sig);
		boolean res = verifyCall.result;
		err = verifyCall.err;
		assertNull("No error when verifying", err);

		assertTrue("Verify result should be true", res);
	}

	@Test
	public void TestAppendSignature() {

		KeyPair key = null;
		try {
			key = crypto.ECDHPub.generateECDSAKeyPair();
		} catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
			fail("Should be no exception when creating key");
		}

		byte[] pubKeyBytes = crypto.Utils.FromECDSAPub(key.getPublic());

		Block block = new Block(0, 1,
				"framehash".getBytes(),
				new byte[][]{
					"abc".getBytes(),
					"def".getBytes(),
					"ghi".getBytes(),
				});

		RResult<BlockSignature> signCall = block.sign(key);
		BlockSignature sig = signCall.result;
		error err = signCall.err;
		assertNull("No error when signing", err);

		err = block.setSignature(sig);
		assertNull("No error when SetSignature", err);

//		blockSignature, err := block.GetSignature(fmt.Sprintf("0x%X", pubKeyBytes));
		String pub = Utils.toHexString(pubKeyBytes);
//		System.out.println(" pub toHexString = " + pub);
//
		RResult<BlockSignature> getSignature = block.getSignature(pub);
		BlockSignature blockSignature = getSignature.result;
		err = getSignature.err;
		assertNull("No error when GetSignature", err);

		RResult<Boolean> verifyCall = block.verify(blockSignature);
		boolean res = verifyCall.result;
		err = verifyCall.err;
		assertNull("No error when verifying signature", err);
		assertTrue("Verify result should be true", res);
	}
}