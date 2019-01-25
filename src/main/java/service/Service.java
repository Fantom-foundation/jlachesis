package service;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import autils.JsonUtils;
import common.RetResult;
import common.RetResult3;
import common.error;
import node.Graph;
import node.Graph.Infos;
import peers.Peers;
import poset.Block;
import poset.Event;
import poset.Root;
import poset.RoundInfo;

@CrossOrigin(maxAge = 3600)
@RestController
@EnableAutoConfiguration
public class Service {
	String bindAddress;
	node.Node node;
	node.Graph graph;
	Logger logger;

	@RequestMapping("/stats")
    String stats() {
		Map<String, String> stats = node.GetStats();
		return JsonUtils.ObjectToString(stats);
    }

	Service(){

	}

	@RequestMapping("/participants")
    String participants() {
		RetResult<Peers> getParticipants = node.GetParticipants();
		Peers participants = getParticipants.result;
		error err = getParticipants.err;
		if (err != null) {
			logger.error("Parsing participants parameter" + err);
//			http.Error(w, err.Error(), http.StatusInternalServerError);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err.Error()).toString();
		}
		return JsonUtils.ObjectToString(participants);
    }

	@RequestMapping("/event")
    String event(@RequestBody String param) {
		RetResult<Event> getEvent = node.GetEvent(param);
		Event event = getEvent.result;
		error err = getEvent.err;
		if (err != null) {
			logger.error(error.Errorf(String.format("Retrieving event %s, err= %s", event, err.Error())));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err.Error()).toString();
		}

		return JsonUtils.ObjectToString(event);
    }

	@RequestMapping("/lasteventfrom")
    String lastevents(@RequestBody String param) {
		RetResult3<String, Boolean> getLastEventFrom = node.GetLastEventFrom(param);
		String event = getLastEventFrom.result1;
		error err = getLastEventFrom.err;
		if (err != null) {
			logger.error(error.Errorf(String.format("Retrieving event %s, err= %s", event, err.Error())));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err.Error()).toString();
		}

		return JsonUtils.ObjectToString(event);
    }

	@RequestMapping("/events")
    String events() {
		Map<Long, Long> knownEvents = node.GetKnownEvents();
		return JsonUtils.ObjectToString(knownEvents);
    }

	@RequestMapping("/consensusevents")
    String consensusEvents() {
		String[] consensusEvents = node.GetConsensusEvents();
		return JsonUtils.ObjectToString(consensusEvents);
    }

	@RequestMapping("/round")
    String round(@RequestBody String param) {
		error err = null;
		long roundIndex = -1;
		try{
			roundIndex = Long.parseLong(param);
		} catch (Exception e) {
			err = error.Errorf(e.getMessage());
		}
		if (err != null) {
			logger.error(error.Errorf(String.format("Parsing roundIndex parameter %s, err= %s", param, err.Error())));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err.Error()).toString();
		}

		RetResult<RoundInfo> getRound = node.GetRound(roundIndex);
		RoundInfo round = getRound.result;
		err = getRound.err;
		if (err != null) {
			logger.error(error.Errorf(String.format("Retrieving round %d, err= %s", roundIndex, err.Error())));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err.Error()).toString();
		}

		return JsonUtils.ObjectToString(round);
    }

	@RequestMapping("/lastround")
    String lastRound() {
		long lastRound = node.GetLastRound();
		return "" + lastRound;
    }

	@RequestMapping("/roundwitnesses")
    String roundWitnesses(@RequestBody String param) {
		error err = null;
		long roundWitnessesIndex = -1;
		try{
			roundWitnessesIndex = Long.parseLong(param);
		} catch (Exception e) {
			err = error.Errorf(e.getMessage());
		}

		if (err != null) {
			logger.error(error.Errorf(String.format("Parsing roundEventsIndex parameter %s, err= %s", param, err.Error())));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err.Error()).toString();
		}

		String[] roundWitnesses = node.GetRoundWitnesses(roundWitnessesIndex);
		return JsonUtils.ObjectToString(roundWitnesses);
    }

	@RequestMapping("/roundevents")
    String roundEvents(@RequestBody String param) {
		error err = null;
		long roundEventsIndex = -1;
		try{
			roundEventsIndex = Long.parseLong(param);
		} catch (Exception e) {
			err = error.Errorf(e.getMessage());
		}

		if (err != null) {
			logger.error(error.Errorf(String.format("Parsing roundEventsIndex parameter %s, err= %s", param, err.Error())));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err.Error()).toString();
		}

		int roundEvent = node.GetRoundEvents(roundEventsIndex);
		return JsonUtils.ObjectToString(roundEvent);
    }

	@RequestMapping("/root")
    String root(@RequestBody String param) {
		error err = null;
		long rootIndex = -1;
		try{
			rootIndex = Long.parseLong(param);
		} catch (Exception e) {
			err = error.Errorf(e.getMessage());
		}
		RetResult<Root> getRoot = node.GetRoot(rootIndex);
		Root root = getRoot.result;

		err = getRoot.err;
		if (err != null) {
			logger.error(error.Errorf(String.format("Retrieving root %s, err= %s", param, err.Error())));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err.Error()).toString();
		}

		return JsonUtils.ObjectToString(root);
    }

	@RequestMapping("/block")
    String block(@RequestBody String param) {
		error err = null;
		long blockIndex = -1;
		try{
			blockIndex = Long.parseLong(param);
		} catch (Exception e) {
			err = error.Errorf(e.getMessage());
		}

		if (err != null) {
			logger.error(error.Errorf(String.format("Parsing block_index parameter %s, err= %s", param, err.Error())));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err.Error()).toString();
		}

		RetResult<Block> getBlock = node.GetBlock(blockIndex);
		Block block = getBlock.result;
		err = getBlock.err;
		if (err != null) {
			logger.error(error.Errorf(String.format("Retrieving block %d, err= %s", blockIndex, err.Error())));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err.Error()).toString();
		}
		return JsonUtils.ObjectToString(block);
    }

	@RequestMapping("/graph")
    String graph() {
	 	Infos res = graph.GetInfos();
	 	return JsonUtils.ObjectToString(res);
    }

	@RequestMapping("/static")
    String staticDir() {
//		http.StripPrefix("/static/", http.FileServer(http.Dir("src/service/static/"))))
		return null;
    }


	public Service(String bindAddress , node.Node n, Logger logger){
		this.bindAddress = bindAddress;
		this.node = n;
		this.graph = new Graph(n);
		this.logger = logger;

	}

	public void Serve() {
		SpringApplication.run(Service.class, new String[] {});
	}

	public static void main (String[] args) {
		SpringApplication.run(Service.class, new String[] {});
	}


//	public http.HandlerFunc corsHandler(http.HandlerFunc h) {
//		return public(ResponseWriter w, Request r) {
//			w.Header().Set("Access-Control-Allow-Origin", "*")
//			w.Header().Set("Access-Control-Allow-Methods", "GET, OPTIONS")
//			w.Header().Set("Access-Control-Allow-Headers",
//				"Accept, Content-Type, Content-Length, Accept-Encoding, Authorization");
//			if (r.Method == "OPTIONS") {
//				/*w.Header().Set("Access-Control-Allow-Origin", "*")
//				    	w.Header().Set("Access-Control-Allow-Methods", "GET, OPTIONS")
//							w.Header().Set("Access-Control-Allow-Headers",
//				        "Accept, Content-Type, Content-Length, Accept-Encoding, Authorization")*/
//			} else {
//				/*w.Header().Set("Access-Control-Allow-Origin", "*")
//				    	w.Header().Set("Access-Control-Allow-Methods", "GET, OPTIONS")
//							w.Header().Set("Access-Control-Allow-Headers",
//				        "Accept, Content-Type, Content-Length, Accept-Encoding, Authorization")*/
//				h.ServeHTTP(w, r);
//			}
//		}
//	}
}