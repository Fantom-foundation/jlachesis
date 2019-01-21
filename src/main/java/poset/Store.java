package poset;

import java.util.Map;

import common.RetResult;
import common.RetResult3;
import common.error;

// Store provides an interface for persistent and non-persistent stores
// to store key lachesis consensus information on a node.
public interface Store {
	int CacheSize();
	RetResult<peers.Peers> Participants();
	RetResult<Map<String,Root>> RootsBySelfParent();
	RetResult<Event> GetEvent(String s);
	error SetEvent(Event e);
	RetResult<String[]> ParticipantEvents(String s, long l);
	RetResult<String> ParticipantEvent(String s, long l);
	RetResult3<String,Boolean> LastEventFrom(String s);
	RetResult3<String,Boolean> LastConsensusEventFrom(String s);
	Map<Long,Long> KnownEvents();
	String[] ConsensusEvents();
	long ConsensusEventsCount();
	error AddConsensusEvent(Event e);
	RetResult<RoundInfo> GetRound(long l);
	error SetRound(long l, RoundInfo r);
	long LastRound();
	String[] RoundWitnesses(long l);
	int RoundEvents(long l);
	RetResult<Root> GetRoot(String s);
	RetResult<Block> GetBlock(long l);
	error SetBlock(Block b);
	long LastBlockIndex();
	RetResult<Frame>  GetFrame(long l);
	error SetFrame(Frame f);
	error Reset(Map<String,Root> map);
	error Close();
	boolean NeedBoostrap(); // Was the store loaded from existing db
	String StorePath();
//	RetResult<Event[]> TopologicalEvents();
}