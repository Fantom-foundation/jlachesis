package poset;

import java.util.Map;

import common.RResult;
import common.RResult3;
import common.error;

/**
 * Store provides an interface for persistent and non-persistent stores
 */
public interface Store {
	int cacheSize();
	RResult<peers.Peers> participants();
	RResult<Map<String,Root>> rootsBySelfParent();
	RResult<Event> getEvent(String s);
	error setEvent(Event e);
	RResult<String[]> participantEvents(String s, long l);
	RResult<String> participantEvent(String s, long l);
	RResult3<String,Boolean> lastEventFrom(String s);
	RResult3<String,Boolean> lastConsensusEventFrom(String s);
	Map<Long,Long> knownEvents();
	String[] consensusEvents();
	long consensusEventsCount();
	error addConsensusEvent(Event e);
	RResult<RoundInfo> getRound(long l);
	error setRound(long l, RoundInfo r);
	long lastRound();
	String[] roundWitnesses(long l);
	int roundEvents(long l);
	RResult<Root> getRoot(String s);
	RResult<Block> getBlock(long l);
	error setBlock(Block b);
	long lastBlockIndex();
	RResult<Frame>  getFrame(long l);
	error setFrame(Frame f);
	error reset(Map<String,Root> map);
	error close();
	boolean needBoostrap(); // Was the store loaded from existing db
	String storePath();
	RResult<Event[]> topologicalEvents();
}