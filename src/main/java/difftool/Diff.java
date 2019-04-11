package difftool;

import java.util.Arrays;
import java.util.Comparator;

import com.google.gson.Gson;

import common.RResult;
import common.error;
import node.Node;
import poset.Block;
import poset.Frame;
import poset.RoundInfo;

/**
 * Diff contains and prints differences details
 */
public class Diff {
	error Err;

	node.Node[] node= new node.Node[2];
	long[] IDs = new long[2];
	long BlocksGap;
	long FirstBlockIndex;
	long RoundGap;
	long FirstRoundIndex;
	String Descr;

	public Diff(Node[] node, long[] iDs) {
		super();
		Err = null;
		this.node = node;
		this.IDs = iDs;

		BlocksGap = 0;
		FirstBlockIndex = 0;
		RoundGap = 0;
		FirstRoundIndex = 0;
		Descr = null;
	}

	private static final Gson gson = new Gson();

	/*
	 * Diff's methods
	 */

	public boolean IsEmpty() {
		boolean has = FirstBlockIndex > 0 || FirstRoundIndex > 0;
		return !has;
	}

	public String ToString() {
		if (Err != null) {
			return String.format("ERR: %s", Err.Error());
		}
		if (IsEmpty()) {
			return "";
		}

		String raw = null;
		try
		{
			raw = gson.toJson(this);
		} catch (Exception e) {
			Err = new error(e.toString());
		}
		if (Err != null) {
			return String.format("JSON: %s", Err.Error());
		}
		return raw;
	}

	public void AddDescr(String s) {
		Descr = Descr + s + "\n";
	}

	// Compare compares each node with others
	public Result Compare(node.Node... nodes) {
		Result result = new Result();
		Node n0, n1;
		Diff diff;
		for (int i = nodes.length - 1; i > 0; --i) {
			n0 = nodes[i];
			for (int j = 0; j < i; ++j) {
				n1 = nodes[j];
				diff = compare(n0, n1);
				result.add(diff);
			}
		}
		return result;
	}

	// compare compares pair of nodes
	public static Diff compare(node.Node n0, node.Node n1) {
		Diff diff = new Diff (
				new node.Node[]{n0, n1},
				new long[]{n0.ID(), n1.ID()});

		if (!compareBlocks(diff)) {
			return diff;
		}
		if (!compareRounds(diff)) {
			return diff;
		}
		if (!compareFrames(diff)) {
			return diff;
		}

		return diff;
	}

	// compareBlocks returns true if we need to go deeper
	public static boolean compareBlocks(Diff diff) {
		Node n0 = diff.node[0];
		Node n1 = diff.node[1];

		long minH = n0.getLastBlockIndex();
		long tmp = n1.getLastBlockIndex();
		diff.BlocksGap = minH - tmp;
		if (minH > tmp) {
			minH = tmp;
			tmp = minH;
		}

		poset.Block b0, b1;
		long i;
		for (i = 0; i <= minH; i++) {
			RResult<Block> getBlock = n0.getBlock(i);
			b0 = getBlock.result;
			diff.Err = getBlock.err;
			if (diff.Err != null) {
				return false;
			}

			RResult<Block> getBlock2 = n1.getBlock(i);
			b1 = getBlock2.result;
			diff.Err = getBlock2.err;
			if (diff.Err != null) {
				return false;
			}

			// NOTE: the same blocks Hashes are different because their Signatures.
			// So, compare bodies only.
			if (!b0.getBody().equals(b1.getBody())) {
				diff.FirstBlockIndex = i;
				diff.AddDescr(String.format("block:\n%+v \n!= \n%+v\n", b0.getBody(), b1.getBody()));

				diff.FirstRoundIndex = b0.roundReceived();
				if (diff.FirstRoundIndex > b1.roundReceived()) {
					diff.FirstRoundIndex = b1.roundReceived();
				}

				return true;
			}
		}

		return false;
	}

	// compareRounds returns true if we need to go deeper
	public static boolean compareRounds(Diff diff)  {
		Node n0 = diff.node[0];
		Node n1 = diff.node[1];

		diff.RoundGap = n0.getLastRound() - n1.getLastRound();

		poset.RoundInfo r0, r1;
		long i;
		for (i = 0; i <= diff.FirstRoundIndex; i++) {
			RResult<RoundInfo> getRound = n0.getRound(i);
			r0 = getRound.result;
			diff.Err = getRound.err;
			if (diff.Err != null) {
				return false;
			}
			RResult<RoundInfo> getRound2 = n1.getRound(i);
			r1 = getRound2.result;
			diff.Err = getRound2.err;
			if (diff.Err != null) {
				return false;
			}

			if (!r0.equals(r1)) {
				diff.FirstRoundIndex = i;
				diff.AddDescr(String.format("round:\n%+v \n!= \n%+v\n", r0, r1));
				return true;
			}

			String[] w0 = n0.roundWitnesses(i);
			String[] w1 = n1.roundWitnesses(i);
			Arrays.sort(w0, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}});
			// TBD the the sorting ok
//			sort.Sort(ByValue(w0));
//			sort.Sort(ByValue(w1));
			if (!w0.equals(w1)) {
				diff.FirstRoundIndex = i;
				diff.AddDescr(String.format("witness:\n%+v \n!= \n%+v\n", w0, w1));
				return true;
			}
		}

		return false;
	}

	// compareFrames returns true if we need to go deeper
	public static boolean compareFrames(Diff diff) {
		Node n0 = diff.node[0];
		Node n1 = diff.node[1];

		poset.Frame f0, f1 ;
		RResult<Frame> getFrame = n0.getFrame(diff.FirstRoundIndex);
		f0 = getFrame.result;
		diff.Err = getFrame.err;
		if (diff.Err != null) {
			return false;
		}

		getFrame = n1.getFrame(diff.FirstRoundIndex);
		f1 = getFrame.result;
		diff.Err = getFrame.err;
		if (diff.Err != null) {
			return false;
		}

		if (!f0.equals(f1)) {
			diff.AddDescr(String.format("frame:\n%+v \n!= \n%+v\n", f0, f1));
			return true;
		}

		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Diff [Err=").append(Err).append(", node=").append(Arrays.toString(node)).append(", IDs=")
				.append(Arrays.toString(IDs)).append(", BlocksGap=").append(BlocksGap).append(", FirstBlockIndex=")
				.append(FirstBlockIndex).append(", RoundGap=").append(RoundGap).append(", FirstRoundIndex=")
				.append(FirstRoundIndex).append(", Descr=").append(Descr).append("]");
		return builder.toString();
	}
}