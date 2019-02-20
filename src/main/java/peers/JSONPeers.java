package peers;

import java.nio.file.Paths;
import java.util.concurrent.Semaphore;

import autils.FileUtils;
import autils.JsonUtils;
import common.RResult;
import common.error;

/**
 * JSONPeers is used to provide peer persistence on disk in the form
 * of a JSON file. This allows human operators to manipulate the file.
 */
public class JSONPeers  implements PeerStore {
	Semaphore l; //    sync.Mutex
	String path;

	/**
	 * Creates a new JSONPeers store.
	 * @param base
	 */
	public JSONPeers(String base) {
		this.l = new Semaphore(1);
		this.path = Paths.get(base, Peer.jsonPeerPath).toString();
	}

	public RResult<Peers> peers() {
		try {
			l.acquire();

			// Read the file
			RResult<byte[]> readResult = FileUtils.readFileToByteArray(path);
			byte[] buf = readResult.result;
			error err = readResult.err;

			if (err != null) {
				return new RResult<Peers>(null, err);
			}

			// Check for no peers
			if (buf == null || buf.length == 0) {
				return new RResult<Peers>(null, null);
			}

			// Decode the peers
			 Peer[] peerSet = JsonUtils.StringToObject(new String(buf), Peer[].class);
			return new RResult<Peers>(Peers.newPeersFromSlice(peerSet), null);
		} catch (InterruptedException e) {
			error err = new error(e.getMessage());
			return new RResult<Peers>(null, err);
		} finally {
			l.release();
		}
	}

	public error setPeers(Peer[] peers) {
		try
		{
			l.acquire();
			String peersString = JsonUtils.ObjectToString(peers);
			//System.out.println("peers string = " +  peersString);
			//System.out.println("write to file path = " +  path);

			byte[] encode = peersString.getBytes();

			// Write out as JSON
			error err = FileUtils.writeToFile(path, encode, FileUtils.MOD_755);
		} catch (InterruptedException e) {
			return error.Errorf(e.getMessage());
		} finally {
			l.release();
		}
		return null;
	}
}