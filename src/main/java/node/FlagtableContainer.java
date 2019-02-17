package node;

import java.util.Map;

import common.RetResult;

public interface FlagtableContainer {
	RetResult<Map<String,Long>> getFlagTable();
}