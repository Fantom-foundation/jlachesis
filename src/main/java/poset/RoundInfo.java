package poset;

import com.google.protobuf.Parser;

import common.IProto;
import poset.proto.Trilean;

public class RoundInfo {
	RoundInfoMessage Message;
	boolean queued;

	public RoundInfo() {
		Message = new RoundInfoMessage();
		// TODO just add this init. ok?
		queued = false;
	}

	public RoundInfo(RoundInfoMessage message) {
		super();
		Message = message;
		queued = false;
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

	public IProto<RoundInfo, poset.proto.RoundInfo> marshaller() {
		return new IProto<RoundInfo, poset.proto.RoundInfo>() {
			@Override
			public poset.proto.RoundInfo toProto() {
				poset.proto.RoundInfo.Builder builder = poset.proto.RoundInfo.newBuilder();
				if (Message != null) {
					builder.setMessage(Message.marshaller().toProto());
				}
				builder.setQueued(queued);
				return builder.build();
			}

			@Override
			public void fromProto(poset.proto.RoundInfo proto) {
				poset.proto.RoundInfoMessage msg = proto.getMessage();
				Message = null;
				if (msg != null) {
					Message = new RoundInfoMessage();
					Message.marshaller().fromProto(msg);
				}
				queued = proto.getQueued();
			}

			@Override
			public Parser<poset.proto.RoundInfo> parser() {
				return poset.proto.RoundInfo.parser();
			}
		};
	}

	public boolean IsQueued() {
		return queued;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Message == null) ? 0 : Message.hashCode());
		result = prime * result + (queued ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RoundInfo other = (RoundInfo) obj;
		if (Message == null) {
			if (other.Message != null)
				return false;
		} else if (!Message.equals(other.Message))
			return false;
		if (queued != other.queued)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RoundInfo [Message=").append(Message).append(", queued=").append(queued).append("]");
		return builder.toString();
	}
}
