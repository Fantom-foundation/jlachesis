package net;

import java.util.Arrays;

import autils.JsonUtils;
import common.error;

public class FastForwardResponse implements ParsableMessage {
	long FromID;
	poset.Block Block;
	poset.Frame Frame;
	byte[] Snapshot;

	public FastForwardResponse()
	{
		FromID = -1;
		Block = null;
		Frame = null;;
		Snapshot = null;
	}

	public FastForwardResponse(long fromID, poset.Block block, poset.Frame frame, byte[] snapshot) {
		FromID = fromID;
		Block = block;
		Frame = frame;
		Snapshot = snapshot;
	}

	public FastForwardResponse(long id) {
		FromID = id;
		Block = null;
		Frame = null;;
		Snapshot = null;
	}

	public void copy(FastForwardResponse r) {
		FromID = r.FromID;
		Block = r.Block;
		Frame = r.Frame;
		Snapshot = r.Snapshot;
	}

	public long getFromID() {
		return FromID;
	}

	public void setFromID(long fromID) {
		FromID = fromID;
	}

	public poset.Block getBlock() {
		return Block;
	}

	public void setBlock(poset.Block block) {
		Block = block;
	}

	public poset.Frame getFrame() {
		return Frame;
	}

	public void setFrame(poset.Frame frame) {
		Frame = frame;
	}

	public byte[] getSnapshot() {
		return Snapshot;
	}

	public void setSnapshot(byte[] snapshot) {
		Snapshot = snapshot;
	}

	@Override
	public error parseFrom(String s) {
		error err = null;
		try {
			FastForwardResponse o = JsonUtils.StringToObject(s, FastForwardResponse.class);
			copy(o);
		} catch (Exception e) {
			err = error.Errorf(e.getMessage());
		}
		return err;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Block == null) ? 0 : Block.hashCode());
		result = prime * result + ((Frame == null) ? 0 : Frame.hashCode());
		result = prime * result + (int) (FromID ^ (FromID >>> 32));
		result = prime * result + Arrays.hashCode(Snapshot);
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
		FastForwardResponse other = (FastForwardResponse) obj;
		if (Block == null) {
			if (other.Block != null)
				return false;
		} else if (!Block.equals(other.Block))
			return false;
		if (Frame == null) {
			if (other.Frame != null)
				return false;
		} else if (!Frame.equals(other.Frame))
			return false;
		if (FromID != other.FromID)
			return false;
		if (!Arrays.equals(Snapshot, other.Snapshot))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FastForwardResponse [FromID=").append(FromID).append(", Block=").append(Block)
				.append(", Frame=").append(Frame).append(", Snapshot=").append(Arrays.toString(Snapshot)).append("]");
		return builder.toString();
	}
}
