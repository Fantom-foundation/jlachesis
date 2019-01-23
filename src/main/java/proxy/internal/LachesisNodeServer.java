package proxy.internal;

import common.error;

/**
 * LachesisNodeServer is the server API for LachesisNode service.
 */
public interface LachesisNodeServer  {
	error Connect(LachesisNode_ConnectServer cs);
}