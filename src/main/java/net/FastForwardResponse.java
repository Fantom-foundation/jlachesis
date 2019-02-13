package net;

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
}
