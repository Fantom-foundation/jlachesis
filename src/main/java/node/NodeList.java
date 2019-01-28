package node;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.LinkedHashMap;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jcsp.lang.Alternative;
import org.jcsp.lang.CSTimer;
import org.jcsp.lang.Channel;
import org.jcsp.lang.Guard;
import org.jcsp.lang.One2OneChannelInt;

import autils.time;
import channel.ChannelUtils;
import channel.ExecService;
import dummy.DummyClient;
import net.InmemTransport;
import peers.Peer;
import peers.Peers;
import poset.Block;

// NodeList is a list of connected nodes for tests purposes
public class NodeList extends LinkedHashMap<PrivateKey, Node> {
	private static final long serialVersionUID = 1848444769246800729L;

	public static final int delay = 100 * time.Millisecond;

	// NewNodeList makes, fills and runs NodeList instance
	public NodeList(int count, Logger logger) {
		super(count);

		Peers participants = new peers.Peers();
		Config config = Config.DefaultConfig();
		config.setLogger(logger);

		for (int i = 0; i < count; i++) {
			InmemTransport transp = new net.InmemTransport("");
			String addr = transp.LocalAddr();
			KeyPair key = crypto.Utils.GenerateECDSAKeyPair().result;
			String pubKey = String.format("0x%X", crypto.Utils.FromECDSAPub(key.getPublic()));
			Peer peer = new peers.Peer(pubKey, addr);

			Node n = new Node(
				config,
				peer.GetID(),
				key,
				participants,
				new poset.InmemStore(participants, config.CacheSize),
				transp,
				DummyClient.NewInmemDummyApp(logger));

			participants.AddPeer(peer);
			this.put(key.getPrivate(), n);
		}

		for (Node n : this.values()) {
			n.Init();
			n.RunAsync(true);
		}
	}

	// Keys returns the all PrivateKeys slice
	public PrivateKey[]  Keys(){
		PrivateKey[] keys = new PrivateKey[this.size()];
		int i = 0;
		for (PrivateKey key : this.keySet()) {
			keys[i] = key;
			i++;
		}
		return keys;
	}

	// Values returns the all nodes slice
	public Node[] Values() {
		Node[] nodes = new Node[this.size()];
		int i = 0;
		for (PrivateKey key : this.keySet()) {
			nodes[i] = this.get(key);
			i++;
		}
		return nodes;
	}

	// StartRandTxStream sends random txs to nodes until stop() called
	public Stoppable StartRandTxStream() {
		One2OneChannelInt stopCh = Channel.one2oneInt(); //make(chan struct{});

		Stoppable stop = new Stoppable() {
			public void stop() {
				ChannelUtils.close(stopCh);
			}
		};

		ExecService.go(() ->
		{
			int seq = 0;
			while(true) {

				// TBD conversion ok?
//					select {
//					case <-stopCh:
//						return
//					case <-time.After(delay):
//						keys = n.Keys();
//						count = len(n);
//						for (int i = 0; i < count; i++) {
//							j := rand.Intn(count);
//							node := n[keys[j]];
//							byte[] tx = String.format("node#%d transaction %d", node.ID(), seq).bytes();
//							node.PushTx(tx);
//							seq++;
//						}
//					}

				final CSTimer tim = new CSTimer ();
				final Alternative alt = new Alternative (new Guard[] {stopCh.in(), tim});
				final int STOP = 0, TIM = 1;
				tim.setAlarm(tim.read() + delay);

				switch (alt.priSelect ()) {
					case STOP:
						stopCh.in().read(); // <-stopCh:
						return;
					case TIM:
						PrivateKey[] keys = Keys();
						Random rand = new Random();

						int count = size();
						for (int i = 0; i < count; i++) {
							int j = rand.nextInt(count);
							node.Node node = get(keys[j]);
							byte[] tx = String.format("node#%d transaction %d", node.ID(), seq).getBytes();
							node.PushTx(tx);
							seq++;
						}
						break;
				}
			}
		});

		return stop;
	}

	// WaitForBlock waits until the target block has retrieved a state hash from the app
	public void WaitForBlock(long target) {
	LOOP:
		while (true) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for (Node node : this.values()) {
				if (target > node.GetLastBlockIndex()) {
					continue LOOP;
				}
				Block block = node.GetBlock(target).result;
				if (block.GetStateHash().length == 0) {
					continue LOOP;
				}
			}
			return;
		}
	}

	interface Stoppable {
		void stop();
	}
}
