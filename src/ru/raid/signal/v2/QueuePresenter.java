package ru.raid.signal.v2;

import java.util.Queue;

public class QueuePresenter implements DataPresenter {
	private double sampleRate;
	private double time, period;
	private Queue<Double> que;
	
	public QueuePresenter(Queue<Double> q, double sampleRate, double startTime) {
		this.sampleRate = sampleRate;
		que = q;
		period = 1 / sampleRate;
		time = startTime - period;
	}

	@Override
	public boolean hasNext() {
		synchronized (que) {
			return !que.isEmpty();
		}
	}

	@Override
	public double next() {
		synchronized (que) {
			if (!que.isEmpty()) {
				time += period;
				return que.poll();
			}
		}
		throw new IllegalStateException("No next!");
	}

	@Override
	public double getSampleRate() {
		return sampleRate;
	}

	@Override
	public double getTimeOffset() {
		return time;
	}
}
