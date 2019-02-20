package poset;

import java.util.Map;

import autils.Logger;
import common.StoreErrType;
import common.RResult;
import common.RollingIndexMap;
import common.StoreErr;
import common.error;
import peers.Peer;

public class ParticipantEventsCache {

	private static Logger logger = Logger.getLogger(ParticipantEventsCache.class);
	peers.Peers participants;
	RollingIndexMap rim;

	class Key {
		String x;
		String y;

		public String ToString() {
			return String.format("{%s, %s}", x, y);
		}
	}

	public ParticipantEventsCache(int size, peers.Peers participants) {
		this.participants = participants;
		this.rim = new RollingIndexMap("ParticipantEvents", size, participants.toIDSlice());
	}

	public RResult<Long> participantID(String participant) {
		Peer peer = participants.getByPubKey().get(participant);

		if (peer == null) {
			return new RResult<Long>(-1L,
			StoreErr.newStoreErr("ParticipantEvents", StoreErrType.SkippedIndex, participant));

		}

		return new RResult<Long>(peer.getID(), null);
	}

	//return participant events with index > skip
	public RResult<String[]> Get(String participant, long skipIndex) {
		RResult<Long> participantID = participantID(participant);
		long id = participantID.result;
		error err= participantID.err;
		if (err != null) {
			return new RResult<String[]>(null, err);
		}

		RResult<Object[]> get = rim.get(id, skipIndex);
		Object[] pe = get.result;
		err = get.err;
		if (err != null) {
			return new RResult<String[]>(null, err);
		}

		String[] res = new String[pe.length];
		for (int k = 0; k < pe.length; k++) {
			res[k] = pe[k].toString();
		}
		return new RResult<String[]>(res, null);
	}

	public RResult<String> GetItem(String participant, long index) {
		logger.field("participant", participant).field("index", index).debug("GetItem()");

		RResult<Long> participantID = participantID(participant);
		long id = participantID.result;
		error err = participantID.err;
		if (err != null) {
			return new  RResult<String>("", err);
		}

		RResult<Object> getItem = rim.getItem(id, index);
		Object item = getItem.result;
		err = getItem.err;
		logger.field("id", id)
			.field("item", item)
			.field("err", err).debug("GetItem()");

		if (err != null) {
			return new  RResult<String>("", err);
		}
		return new  RResult<String>(item.toString(), null);
	}

	public RResult<String> GetLast(String participant) {
		RResult<Long> participantID = participantID(participant);
		long id = participantID.result;
		error err = participantID.err;
		if (err != null) {
			return new RResult<String>("", err);
		}

		RResult<Object> getLast = rim.getLast(id);
		Object last = getLast.result;
		err = getLast.err;
		if (err != null) {
			return new RResult<String>("", err);
		}
		return new RResult<String>(last.toString(), null);
	}

	public RResult<String> GetLastConsensus(String participant) {
		RResult<Long> participantID = participantID(participant);
		long id = participantID.result;
		error err = participantID.err;
		if (err != null) {
			return new RResult<String>("", err);
		}

		RResult<Object> getLast = rim.getLast(id);
		Object last = getLast.result;
		err = getLast.err;
		if (err != null) {
			return new RResult<String>("", err);
		}
		return new RResult<String>(last.toString(), null);
	}

	public error Set(String participant, String hash, long index) {
		RResult<Long> participantID = participantID(participant);
		long id = participantID.result;
		error err = participantID.err;
		if (err != null) {
			return err;
		}
		return rim.set(id, hash, index);
	}

	//returns [participant id] => lastKnownIndex
	public Map<Long,Long> Known() {
		return rim.known();
	}

	public error Reset() {
		return rim.reset();
	}

	public void Import(ParticipantEventsCache other) {
		rim.copy(other.rim);
	}
}