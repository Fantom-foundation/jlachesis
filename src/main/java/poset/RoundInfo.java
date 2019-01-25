package poset;

import java.util.Map;

import common.RetResult;
import common.error;
import poset.proto.Trilean;

public class RoundInfo {
	RoundInfoMessage Message;
	boolean queued;

	public RoundInfo() {
		this.Message = new RoundInfoMessage();
	}

	public void AddEvent(String x, boolean witness) {
		RoundEvent re = Message.Events.get(x);
		if (re == null) {
			Message.Events.put(x, new RoundEvent(witness));
		}
	}

	public void SetConsensusEvent(String x) {
		RoundEvent re = Message.Events.get(x);
		if (re == null) {
			re = new RoundEvent();
		}
		re.Consensus = true;
		Message.Events.put(x,  re);
	}

	public void SetFame(String x, boolean f) {
		RoundEvent re = Message.Events.get(x);
		if (re == null) {
			re = new RoundEvent(true);
		}
		if (f) {
			re.Famous = Trilean.TRUE;
		} else {
			re.Famous = Trilean.FALSE;
		}
		Message.Events.put(x, re);
	}

	//return true if no witnesses' fame is left undefined
	public boolean WitnessesDecided() {
		for (RoundEvent e: Message.Events.values()) {
			if (e.Witness && e.Famous == Trilean.UNDEFINED) {
				return false;
			}
		}
		return true;
	}

	//return witnesses
	public String[] Witnesses() {
		String[] res = Message.Events.keySet().stream().filter( x ->
			Message.Events.get(x).Witness
		).toArray(String[]::new);
		return res;

//		ArrayList<String> res = new ArrayList<String>();
//		for (String x : Message.Events.keySet()) {
//			RoundEvent e = Message.Events.get(x);
//			if (e.Witness) {
//				res.add(x);
//			}
//		}
//		return res.toArray(new String[res.size()]);
	}

	public String[] RoundEvents() {
		String[] res = Message.Events.keySet().stream().filter( x ->
			!(Message.Events.get(x).Consensus)
		).toArray(String[]::new);
		return res;
	}

	//return consensus events
	public String[] ConsensusEvents() {
		String[] res = Message.Events.keySet().stream().filter( x ->
			Message.Events.get(x).Consensus
		).toArray(String[]::new);
		return res;
	}

	//return famous witnesses
	public String[] FamousWitnesses() {
		String[] res = Message.Events.keySet().stream().filter( x -> {
			RoundEvent e = Message.Events.get(x);
			return (e.Witness && e.Famous == Trilean.TRUE);
		}).toArray(String[]::new);
		return res;
	}

	public boolean IsDecided(String witness) {
		RoundEvent w = Message.Events.get(witness);
		return w != null && w.Witness && w.Famous != Trilean.UNDEFINED;
	}

	public RetResult<byte[]> ProtoMarshal() {
//		proto.Buffer bf;
//		bf.SetDeterministic(true)
//		err := bf.Marshal(&Message);
//		if  (err != null) {
//			return new RetResult<byte[]>(null, err);
//		}
//		return RetResult<byte[]>(bf.Bytes(), null);

		// TBD

		return null;
	}

	public error ProtoUnmarshal(byte[] data) {
//		return proto.Unmarshal(data, Message);
		// TBD

		return null;
	}

	public boolean IsQueued() {
		return queued;
	}



	public boolean equals(RoundInfo that) {
		return this.queued == that.queued &&
			EqualsMapStringRoundEvent(this.Message.Events, that.Message.Events);
	}

	public static boolean EqualsMapStringRoundEvent(
			Map<String,RoundEvent> thisMap,
			Map<String,RoundEvent> thatMap) {
		return thisMap.equals(thatMap);
	}
}
