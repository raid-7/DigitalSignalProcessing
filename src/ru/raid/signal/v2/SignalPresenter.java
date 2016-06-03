package ru.raid.signal.v2;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Copies updates of some signal
 * 
 * @author Raid
 *
 */
public class SignalPresenter implements DataPresenter, SignalListener {
	private Signal source;
	private double time;
	private Queue<Double> data = new LinkedList<>();
	
	public SignalPresenter(Signal signal) {
		source = signal;
		time = signal.getTimeOffset(1);
		signal.addWeakListener(this);
	}

	@Override
	public void onSampleFiltered(Signal signal, double smp) {
		if (signal == source) {
			data.add(smp);
		}
	}

	@Override
	public boolean hasNext() {
		source.available();
		return !data.isEmpty();
	}

	@Override
	public double next() {
		if (hasNext()) {
			time += source.getSamplePeriod();
			return data.poll();
		}
		throw new IllegalStateException("No next!");
	}
	
	@Override
	public double getTimeOffset() {
		return time;
	}

	@Override
	public double getSampleRate() {
		return source.getSampleRate();
	}

	@Override
	public void onNextSample(Signal signal, double data) {}
}
