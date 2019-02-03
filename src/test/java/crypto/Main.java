package crypto;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.Security;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

public class Main {
	public static void main(String[] args) throws Exception {
	    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	    Reader rdr = new StringReader (
	    "-----BEGIN EC PRIVATE KEY-----\n" +
	    "MHcCAQEEICQgVepySeMSQD+CJ5tQRm3y0zAVCe7uYL+yCAsJ3bf2oAoGCCqGSM49\n" +
	    "AwEHoUQDQgAEajR/BIirx9kuIgh5TjJ+yhWwwrJwGLK1uJ3Yy3Nv18w4830tEIIl\n" +
	    "MK2XNZrL2DemXCymLUSwzlab0iLC2r8mjw==\n" +
	    "-----END EC PRIVATE KEY-----");

	    Object parsed = new PEMParser(rdr).readObject();
	    KeyPair pair = new JcaPEMKeyConverter().getKeyPair((PEMKeyPair)parsed);

	    //System.out.println ("priv = " + pair.getPrivate().getAlgorithm() + " : " + pair.getPrivate().getFormat());
	    //System.out.println ("pub = " +  pair.getPublic().getAlgorithm() + " : " + pair.getPublic().getFormat());

	    //System.out.println ("private from Utils.keyToHexString: " + Utils.keyToHexString(pair.getPrivate()));
	    //System.out.println ("public from Utils.keyToHexString: " + Utils.keyToHexString(pair.getPublic()));

	    String expectedPrivHex = "308193020100301306072A8648CE3D020106082A8648CE3D030107047930770201010420242055EA7249E312403F82279B50466DF2D3301509EEEE60BFB2080B09DDB7F6A00A06082A8648CE3D030107A144034200046A347F0488ABC7D92E2208794E327ECA15B0C2B27018B2B5B89DD8CB736FD7CC38F37D2D10822530AD97359ACBD837A65C2CA62D44B0CE569BD222C2DABF268F";
	    String expectedPubHex = "3059301306072A8648CE3D020106082A8648CE3D030107034200046A347F0488ABC7D92E2208794E327ECA15B0C2B27018B2B5B89DD8CB736FD7CC38F37D2D10822530AD97359ACBD837A65C2CA62D44B0CE569BD222C2DABF268F";

//	    assertEquals("private key's hex",  Utils.keyToHexString(pair.getPrivate()), expectedPrivHex);
//	    assertEquals("public key's hex",  Utils.keyToHexString(pair.getPublic()), expectedPubHex);

		JcaPKCS8Generator pkcsGenerator = new JcaPKCS8Generator(pair.getPrivate(), null);
        PemObject pemObj = pkcsGenerator.generate();
        StringWriter stringWriter = new StringWriter();
    	JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter);
        pemWriter.writeObject(pemObj);
        pemWriter.flush();
        pemWriter.close();

        String pkcs8Key = stringWriter.toString();
        System.out.println( "pkcs8Key = "  + pkcs8Key);


        String mimeString  = Utils.getMimeString(pair.getPrivate());
        String expectedMime = "MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgJCBV6nJJ4xJAP4Inm1BGbfLTMBUJ\n" +
        		"7u5gv7IICwndt/agCgYIKoZIzj0DAQehRANCAARqNH8EiKvH2S4iCHlOMn7KFbDCsnAYsrW4ndjL\n" +
        		"c2/XzDjzfS0QgiUwrZc1msvYN6ZcLKYtRLDOVpvSIsLavyaP";
//        assertEquals("mimeString equals", mimeString, expectedMime);
//        String pkcs8Key  = Utils.getOutputString(pair.getPrivate(), "ec", "private");
//        assertTrue("Output string contains", pkcs8Key.contains(expectedMime));
////
//        mimeString = Utils.getMimeString(pair.getPublic());
//        expectedMime = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEajR/BIirx9kuIgh5TjJ+yhWwwrJw\n" +
//        		"GLK1uJ3Yy3Nv18w4830tEIIlMK2XNZrL2DemXCymLUSwzlab0iLC2r8mjw==";
//        assertEquals("mimeString equals", mimeString, expectedMime);
//        String pubKey  = Utils.getOutputString(pair.getPublic(), "ec", "public");
//        assertTrue("Output string contains", pubKey.contains(expectedMime));

        stringWriter = new StringWriter();
        PemWriter w = new PemWriter (stringWriter);
        PrivateKeyInfo i = PrivateKeyInfo.getInstance(ASN1Sequence.getInstance(pair.getPrivate().getEncoded()));
//        if( ! i.getPrivateKeyAlgorithm().getAlgorithm().equals(X9ObjectIdentifiers.id_ecPublicKey) ){
//            throw new Exception ("not EC key");
//        }
        ASN1Object o = (ASN1Object) i.parsePrivateKey();
        w.writeObject (new PemObject ("EC PRIVATE KEY", o.getEncoded("DER")));
        // DER may already be the default but safer to (re)specify it
        w.close();
        System.out.println( "DER key = "  + stringWriter.toString());


        Map map = new HashMap<String,Object>();

        map.put("aaaa", new Object());
        map.put("abc", 1);
        map.put("dddd", "AAAAAA");

        System.out.println(Arrays.toString(map.entrySet().toArray()));
        System.out.println(map);
	}
}
