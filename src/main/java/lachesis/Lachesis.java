package lachesis;

import java.security.KeyPair;

import autils.Logger;
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
	private node.Node Node;
	net.Transport Transport;
	private poset.Store Store;
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
		RetResult<peers.Peers> peersCall = peerStore.peers();
		peers.Peers participants = peersCall.result;
		error err = peersCall.err;

		if (err != null) {
			return err;
		}

		if (participants.length() < 2) {
			return error.Errorf("peers.json should define at least two peers");
		}

		Peers = participants;

		return null;
	}

public  error initStore() {
	String dbDir = String.format("%s/badger", Config.DataDir);

	if (!Config.Store) {
		setStore(new InmemStore(Peers, Config.NodeConfig.getCacheSize()));

		Config.logger.debug("created new in-mem store");
	} else {
		error err;

		Config.logger.field("path", Config.BadgerDir()).debug("Attempting to load or create database");
		RetResult<BadgerStore> loadOrCreateBadgerStore = BadgerStore.LoadOrCreateBadgerStore(Peers, Config.NodeConfig.getCacheSize(), dbDir);
		setStore(loadOrCreateBadgerStore.result);
		err = loadOrCreateBadgerStore.err;

		if (err != null) {
			return err;
		}

		if (getStore().NeedBoostrap()) {
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
		RetResult<KeyPair> readKey = pemKey.ReadKeyPair();
		KeyPair privKey = readKey.result;
		error err = readKey.err;

		if (err != null) {
			Config.logger.warn("Cannot read private key from file" + err);

			RetResult<KeyPair> keygen = Keygen(Config.DataDir);
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
	KeyPair key = Config.Key;

//	String nodePub = String.format("0x%X", crypto.Utils.FromECDSAPub(key.getPublic()));
	String nodePub = crypto.Utils.keyToHexString(key.getPublic());

	Peer n = Peers.getByPubKey().get(nodePub);
	boolean ok = n != null;
	if (!ok) {
		return error.Errorf("cannot find self pubkey in peers.json");
	}

	long nodeID = n.GetID();

	Config.logger
		.field("participants", Peers)
		.field("id", nodeID).debug("PARTICIPANTS");

	setNode(new node.Node(
		Config.NodeConfig,
		nodeID,
		key,
		Peers,
		getStore(),
		Transport,
		Config.getProxy()
	));

	error err = getNode().Init();
	if ( err != null) {
		return error.Errorf(String.format("failed to initialize node: %s", err));
	}

	return null;
}

	public error initService() {
		if (!Config.ServiceAddr.isEmpty()) {
			Service = new service.Service(Config.ServiceAddr, getNode(), Config.logger);
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
		getNode().run(true);
	}

	public RetResult<KeyPair> Keygen(String datadir) {
		PemKey pemKey = new PemKey(datadir);

		error err = pemKey.ReadKeyPair().err;
		if (err == null) {
			return new RetResult<>(null, error.Errorf( String.format("another key already lives under %s", datadir)));
		}

		RetResult<KeyPair> generateECDSAKey = crypto.Utils.GenerateECDSAKeyPair();
		KeyPair keyPair = generateECDSAKey.result;
		err = generateECDSAKey.err;

		if (err != null) {
			return new RetResult<>(null, err);
		}

		err = pemKey.WriteKey(keyPair);
		if (err != null) {
			return new RetResult<>(null, err);
		}

		return new RetResult<>(keyPair, null);
	}

	public poset.Store getStore() {
		return Store;
	}

	public void setStore(poset.Store store) {
		Store = store;
	}

	public node.Node getNode() {
		return Node;
	}

	public void setNode(node.Node node) {
		Node = node;
	}
}