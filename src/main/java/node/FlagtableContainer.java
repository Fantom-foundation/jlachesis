package node;

import java.util.Map;

import common.RResult;

public interface FlagtableContainer {
	RResult<Map<String,Long>> getFlagTable();
}