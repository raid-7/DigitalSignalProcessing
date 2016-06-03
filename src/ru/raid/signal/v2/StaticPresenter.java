package ru.raid.signal.v2;

import java.util.Arrays;

/**
 * Array data presenter
 * 
 * @author Raid
 *
 */
public class StaticPresenter implements DataPresenter {
	private double sampleRate;
	private double[] data;
	private int pos = 0, size;
	private double time, period;
	
	public StaticPresenter(double[] data, double sampleRate, double startTime) {
		this.data = Arrays.copyOf(data, data.length);
		this.sampleRate = sampleRate;
		size = this.data.length;
		period = 1 / sampleRate;
		time = startTime - period;
	}

	@Override
	public boolean hasNext() {
		return pos < size;
	}

	@Override
	public double next() {
		if (pos < size) {
			time += period;
			return data[pos++];
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
