package poset;

import java.util.Map;

import common.RetResult;
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
		this.rim = new RollingIndexMap("ParticipantBlockSignatures", size, participants.ToIDSlice());
	}

	public RetResult<Long> participantID(String participant) {
		Peer peer = participants.getByPubKey().get(participant);

		if (peer == null) {
			return new RetResult<>(-1L, StoreErr.newStoreErr("ParticipantBlockSignatures", StoreErrType.UnknownParticipant, participant));
		}

		return new RetResult<>(peer.GetID(), null);
	}

	//return participant BlockSignatures where index > skip
	public RetResult<BlockSignature[]> Get(String participant, long skipIndex) {
		RetResult<Long> participantID = participantID(participant);
		long id = participantID.result;
		error err = participantID.err;
		if (err != null) {
			return new RetResult<>( new BlockSignature[]{}, err);
		}

		RetResult<Object[]> getId = rim.Get(id, skipIndex);
		Object[] ps = getId.result;
		err = getId.err;
		if (err != null) {
			return new RetResult<>( new BlockSignature[]{}, err);
		}

		BlockSignature[] res = new BlockSignature[ps.length];
		for (int k = 0; k < ps.length; ++k) {
			res[k] = (BlockSignature) ps[k];
		}
		return new RetResult<>(res, null);
	}

	public RetResult<BlockSignature> GetItem(String participant, long index) {
		RetResult<Long> participantID = participantID(participant);
		Long id = participantID.result;
		error err = participantID.err;
		if (err != null) {
			return new RetResult<>(new BlockSignature(), err);
		}

		RetResult<Object> getItem = rim.GetItem(id, index);
		Object item = getItem.result;
		err = getItem.err;
		if (err != null) {
			return new RetResult<>(new BlockSignature(), err);
		}
		return new RetResult<>( (BlockSignature)item, null);
	}

	public RetResult<BlockSignature> GetLast(String participant) {

		RetResult<Object> getLast = rim.GetLast(participants.getByPubKey().get(participant).GetID());
		Object last = getLast.result;
		error err = getLast.err;

		if (err != null) {
			return new RetResult<>(new BlockSignature(), err);
		}

		return new RetResult<>( (BlockSignature) last, null);
	}

	public error Set(String participant , BlockSignature sig ) {
		RetResult<Long> participantID = participantID(participant);
		long id = participantID.result;
		error err = participantID.err;
		if (err != null) {
			return err;
		}

		return rim.Set(id, sig, sig.Index);
	}

	//returns [participant id] => last BlockSignature Index
	public Map<Long,Long> Known() {
		return rim.Known();
	}

	public error Reset() {
		return rim.Reset();
	}
}