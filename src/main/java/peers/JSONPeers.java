package peers;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.Semaphore;

import autils.FileUtils;
import autils.JsonUtils;
import common.RetResult;
import common.error;

// JSONPeers is used to provide peer persistence on disk in the form
// of a JSON file. This allows human operators to manipulate the file.
public class JSONPeers {
	Semaphore l; //    sync.Mutex
	String path;

	// NewJSONPeers creates a new JSONPeers store.
	public JSONPeers(String base) {
		this.l = new Semaphore(1);
		this.path = Paths.get(base, Peer.jsonPeerPath).toString();
	}

	// Peers implements the PeerStore interface.
	public RetResult<Peers> Peers() {
		try {
			l.acquire();

			// Read the file
			RetResult<byte[]> readResult = FileUtils.readFileToByteArray(path);
			byte[] buf = readResult.result;
			error err = readResult.err;

			if (err != null) {
				return new RetResult<Peers>(null, err);
			}

			// Check for no peers
			if (buf.length == 0) {
				return new RetResult<Peers>(null, null);
			}

			// Decode the peers
			Peer[] peerSet;

			// TODO: is the transformation correct?
	//		dec := json.NewDecoder(bytes.NewReader(buf));
	//		err := dec.Decode(peerSet);

			byte[] dec = Base64.getDecoder().decode(buf);
			peerSet = JsonUtils.StringToObject(Arrays.toString(dec), Peer[].class);

			return new RetResult<Peers>(Peers.NewPeersFromSlice(peerSet), null);
		} catch (InterruptedException e) {
			error err = new error(e.getMessage());
			return new RetResult<Peers>(null, err);

//			e.printStackTrace();
		} finally {
			l.release();
		}
	}

	// SetPeers implements the PeerStore interface.
	public error SetPeers(Peer[] peers) {
		try
		{
			l.acquire();
//			enc = NewEncoder(buf);
//			error err = enc.Encode(peers);

			String peersString = JsonUtils.ObjectToString(peers);
			byte[] encode = Base64.getEncoder().encode(peersString.getBytes());

			// Write out as JSON
			RetResult<File> fileCreation = FileUtils.createFile(path, 755);
			error err = fileCreation.err;
			if (err != null) {
				return err;
			}
			FileUtils.writeToFile(fileCreation.result, encode);
		} catch (InterruptedException e) {
			return error.Errorf(e.getMessage());
		} finally {
			l.release();
		}
		return null;
	}
}