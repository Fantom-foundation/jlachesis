package node;

import java.util.concurrent.atomic.AtomicReference;

import channel.ExecService;

public class nodeState {
	AtomicReference<NodeState> state;
	WaitGroup wg;
//	sync.WaitGroup wg;

	public NodeState getState() {
		return state.get();
	}

	public void setState(NodeState s) {
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

	class WaitGroup {
		private int jobs = 0;

		public synchronized void add(int i) {
			jobs += i;
		}

		public synchronized void done() {
			if (--jobs == 0) {
				notifyAll();
			}
		}

		public synchronized void await() throws InterruptedException {
			while (jobs > 0) {
				wait();
			}
		}
	}
}