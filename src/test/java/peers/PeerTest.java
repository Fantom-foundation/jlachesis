package peers;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.HashMap;

import org.junit.Test;

import autils.FileUtils;
import common.RetResult;
import common.error;

/**
 * Test for Peer
 * @author qn
 *
 */
public class PeerTest {

	@Test
	public void TestJSONPeers() {
		// Create a test dir
		File file = Paths.get("/tmp/lachesis_test/" + "peers.json").toFile();
		FileUtils.mkdirs(file.toString(), FileUtils.MOD_755);
		if (file.exists()) {
			file.delete();
		}

		Path path = FileUtils.createFile(file.getPath(), FileUtils.MOD_700).result;

		// Create the store
		JSONPeers store = new JSONPeers(path.getParent().toString());

		//System.out.println("path =" +  path.toString());
		//System.out.println("store.path =" +  store.path);

		// Try a read, should get nothing
		RetResult<Peers> peersCall = store.peers();
		Peers peers = peersCall.result;
		error err = peersCall.err;

		assertNull("store.Peers() should have no error", err);
		assertNull("Peers is null", peers);

		HashMap<String, KeyPair> keys = new HashMap<String,KeyPair>();
		Peers newPeers = new Peers();

		for (int i = 0; i < 3; i++) {
			KeyPair key = crypto.Utils.GenerateECDSAKeyPair().result;
			Peer peer = new Peer();
			peer.netAddr = String.format("addr%d", i);
//			peer.PubKeyHex = fmt.Sprintf("0x%X", scrypto.FromECDSAPub(&key.PublicKey)),
			peer.pubKeyHex = crypto.Utils.keyToHexString(key.getPublic());

			newPeers.addPeer(peer);
			keys.put(peer.netAddr, key);
		}

		Peer[] newPeersSlice = newPeers.toPeerSlice();
		err = store.setPeers(newPeersSlice);
		assertNull("No error when slice", err);

		// Try a read, should find 3 peers
		peersCall = store.peers();
		peers = peersCall.result;
		err = peersCall.err;
		assertNull("No error when slice", err);
		assertEquals("peers.length is 3", 3, peers.length());

		Peer[] peersSlice = peers.toPeerSlice();

		for (int i = 0; i < 3; i++) {
			assertEquals(String.format("peers[%d] NetAddr should match", i),
				newPeersSlice[i].netAddr, peersSlice[i].netAddr);

			assertEquals(String.format("peers[%d] PubKeyHex should match", i),
				newPeersSlice[i].pubKeyHex, peersSlice[i].pubKeyHex);

			RetResult<byte[]> pubKeyBytesCall = peersSlice[i].PubKeyBytes();
			byte[] pubKeyBytes = pubKeyBytesCall.result;
			err = pubKeyBytesCall.err;

			assertNull("No error when calling PubKeyBytes", err);

			PublicKey pubKey = crypto.Utils.ToECDSAPub(pubKeyBytes);
			assertEquals(String.format("peers[%d] PublicKey not parsed correctly", i), pubKey,  keys.get(peersSlice[i].netAddr).getPublic());
		}
	}

}