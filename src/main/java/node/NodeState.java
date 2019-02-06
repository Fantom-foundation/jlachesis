package node;

import java.util.concurrent.atomic.AtomicReference;

import channel.ExecService;

/**
 * NodeState
 */
public class NodeState {
	AtomicReference<NodeStates> state = new AtomicReference<NodeStates>();
	WaitGroup wg = new WaitGroup();

	public NodeState() {
		state.set(NodeStates.CatchingUp);
	}

	public NodeStates getState() {
		return state.get();
	}

	public void setState(NodeStates s) {
		state.set(s);
	}

	@Override
	public String toString() {
		return state.get().getStateName();
	}

	// TBD: conversion is ok?

	// Start a goroutine and add it to waitgroup
	public void goFunc(Runnable r) {
		wg.add(1);
		ExecService.go(() -> {
			r.run();
			wg.done();
		});
	}

	public void waitRoutines() {
		try {
			wg.wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}