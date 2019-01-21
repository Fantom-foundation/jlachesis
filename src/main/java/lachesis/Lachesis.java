package lachesis;

import java.security.PrivateKey;

import org.apache.log4j.Logger;

import channel.ExecService;
import common.RetResult;
import common.error;
import crypto.PemDump;
import crypto.PemKey;
import net.NetworkTransport;
import net.TCPTransport;
import peers.JSONPeers;
import peers.Peer;
import poset.BadgerStore;
import poset.InmemStore;

public class Lachesis {
	LachesisConfig Config;
	node.Node Node;
	net.Transport Transport;
	poset.Store Store;
	peers.Peers Peers;
	service.Service Service;

	public Lachesis(LachesisConfig config) {
		this.Config = config;
	}

	public error initTransport() {
		RetResult<NetworkTransport> newTCPTransport = TCPTransport.NewTCPTransport(
				Config.BindAddr,
				null,
				Config.MaxPool,
				Config.NodeConfig.getTCPTimeout(),
				Config.logger
			);
		NetworkTransport transport = newTCPTransport.result;
		error err = newTCPTransport.err;

		if (err != null) {
			return err;
		}

		this.Transport = transport;

		return null;
	}

	public error initPeers() {
		if (!Config.LoadPeers) {
			if (Peers == null) {
				return error.Errorf("did not load peers but none was present");
			}
			return null;
		}

		JSONPeers peerStore = new peers.JSONPeers(Config.DataDir);
		RetResult<peers.Peers> peersCall = peerStore.Peers();
		peers.Peers participants = peersCall.result;
		error err = peersCall.err;

		if (err != null) {
			return err;
		}

		if (participants.Len() < 2) {
			return error.Errorf("peers.json should define at least two peers");
		}

		Peers = participants;

		return null;
	}

public  error initStore() {
	String dbDir = String.format("%s/badger", Config.DataDir);

	if (!Config.Store) {
		Store = new InmemStore(Peers, Config.NodeConfig.getCacheSize());

		Config.logger.debug("created new in-mem store");
	} else {
		error err;

//		Config.logger.WithField("path", Config.BadgerDir()).debug("Attempting to load or create database");
		RetResult<BadgerStore> loadOrCreateBadgerStore = BadgerStore.LoadOrCreateBadgerStore(Peers, Config.NodeConfig.getCacheSize(), dbDir);
		Store = loadOrCreateBadgerStore.result;
		err = loadOrCreateBadgerStore.err;

		if (err != null) {
			return err;
		}

		if (Store.NeedBoostrap()) {
			Config.logger.debug("loaded badger store from existing database at " + dbDir);
		} else {
			Config.logger.debug("created new badger store from fresh database");
		}
	}

	return null;
}

public error initKey() {
	if (Config.Key == null) {
		PemKey pemKey = new PemKey(Config.DataDir);
		RetResult<PrivateKey> readKey = pemKey.ReadKey();
		PrivateKey privKey = readKey.result;
		error err = readKey.err;

		if (err != null) {
			Config.logger.warn("Cannot read private key from file" + err);

			RetResult<PrivateKey> keygen = Keygen(Config.DataDir);
			privKey = keygen.result;
			err = keygen.err;

			if (err != null) {
				Config.logger.error("Cannot generate a new private key" + err);

				return err;
			}

			RetResult<PemDump> toPemKey = crypto.PemKey.ToPemKey(privKey);
			PemDump pem = toPemKey.result;

			Config.logger.info("Created a new key:" + pem.PublicKey);
		}

		Config.Key = privKey;
	}

	return null;
}

public error initNode() {
	PrivateKey key = Config.Key;

	String nodePub = String.format("0x%X", crypto.Utils.FromECDSAPub(crypto.Utils.getPublicFromPrivate(key)));
	Peer n = Peers.getByPubKey().get(nodePub);
	boolean ok = n != null;
	if (!ok) {
		return error.Errorf("cannot find self pubkey in peers.json");
	}

	long nodeID = n.GetID();

//	Config.logger.WithFields(logrus.Fields{
//		"participants": Peers,
//		"id":           nodeID,
//	}).Debug("PARTICIPANTS");

	Node = new node.Node(
		Config.NodeConfig,
		nodeID,
		key,
		Peers,
		Store,
		Transport,
		Config.Proxy
	);

	error err = Node.Init();
	if ( err != null) {
		return error.Errorf(String.format("failed to initialize node: %s", err));
	}

	return null;
}

	public error initService() {
		if (!Config.ServiceAddr.isEmpty()) {
			Service = new service.Service(Config.ServiceAddr, Node, Config.logger);
		}
		return null;
	}

	public error Init() {
		error err;
		if (Config.logger == null) {
			Config.logger = Logger.getLogger(Lachesis.class);
//			lachesis_log.NewLocal(Config.Logger, Config.LogLevel);
		}

		if ((err = initPeers()) != null) {
			return err;
		}

		if ((err = initStore()) != null) {
			return err;
		}

		if ((err = initTransport()) != null) {
			return err;
		}

		if ((err = initKey()) != null) {
			return err;
		}

		if ((err = initNode()) != null) {
			return err;
		}

		if ((err = initService()) != null) {
			return err;
		}

		return null;
	}

	public void Run() {
		if (Service != null) {
			ExecService.go(() -> Service.Serve());
		}
		Node.Run(true);
	}

	public RetResult<PrivateKey> Keygen(String datadir) {
		PemKey pemKey = new PemKey(datadir);

		error err = pemKey.ReadKey().err;
		if (err == null) {
			return new RetResult<PrivateKey>(null, error.Errorf( String.format("another key already lives under %s", datadir)));
		}

		RetResult<PrivateKey> generateECDSAKey = crypto.Utils.GenerateECDSAKey();
		PrivateKey privKey = generateECDSAKey.result;
		err = generateECDSAKey.err;

		if (err != null) {
			return new RetResult<PrivateKey>(null, err);
		}

		err = pemKey.WriteKey(privKey);
		if (err != null) {
			return new RetResult<PrivateKey>(null, err);
		}

		return new RetResult<PrivateKey>(privKey, null);
	}
}