package poset;

import java.util.Map;

import common.RResult;
import common.RollingIndexMap;
import common.StoreErr;
import common.StoreErrType;
import common.error;
import peers.Peer;

public class ParticipantBlockSignaturesCache {
	peers.Peers participants;
	RollingIndexMap rim;

	public ParticipantBlockSignaturesCache(int size, peers.Peers participants) {
		this.participants =  participants;
		this.rim = new RollingIndexMap("ParticipantBlockSignatures", size, participants.toIDSlice());
	}

	public RResult<Long> participantID(String participant) {
		Peer peer = participants.getByPubKey().get(participant);

		if (peer == null) {
			return new RResult<>(-1L, StoreErr.newStoreErr("ParticipantBlockSignatures", StoreErrType.UnknownParticipant, participant));
		}

		return new RResult<>(peer.getID(), null);
	}

	//return participant BlockSignatures where index > skip
	public RResult<BlockSignature[]> Get(String participant, long skipIndex) {
		RResult<Long> participantID = participantID(participant);
		long id = participantID.result;
		error err = participantID.err;
		if (err != null) {
			return new RResult<>( new BlockSignature[]{}, err);
		}

		RResult<Object[]> getId = rim.get(id, skipIndex);
		Object[] ps = getId.result;
		err = getId.err;
		if (err != null) {
			return new RResult<>( new BlockSignature[]{}, err);
		}

		BlockSignature[] res = new BlockSignature[ps.length];
		for (int k = 0; k < ps.length; ++k) {
			res[k] = (BlockSignature) ps[k];
		}
		return new RResult<>(res, null);
	}

	public RResult<BlockSignature> GetItem(String participant, long index) {
		RResult<Long> participantID = participantID(participant);
		Long id = participantID.result;
		error err = participantID.err;
		if (err != null) {
			return new RResult<>(new BlockSignature(), err);
		}

		RResult<Object> getItem = rim.getItem(id, index);
		Object item = getItem.result;
		err = getItem.err;
		if (err != null) {
			return new RResult<>(new BlockSignature(), err);
		}
		return new RResult<>( (BlockSignature)item, null);
	}

	public RResult<BlockSignature> GetLast(String participant) {

		RResult<Object> getLast = rim.getLast(participants.getByPubKey().get(participant).getID());
		Object last = getLast.result;
		error err = getLast.err;

		if (err != null) {
			return new RResult<>(new BlockSignature(), err);
		}

		return new RResult<>( (BlockSignature) last, null);
	}

	public error Set(String participant , BlockSignature sig ) {
		RResult<Long> participantID = participantID(participant);
		long id = participantID.result;
		error err = participantID.err;
		if (err != null) {
			return err;
		}

		return rim.set(id, sig, sig.index);
	}

	//returns [participant id] => last BlockSignature Index
	public Map<Long,Long> Known() {
		return rim.known();
	}

	public error Reset() {
		return rim.reset();
	}
}