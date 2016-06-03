package ru.raid.signal.v2.tools;

import java.util.function.DoubleUnaryOperator;

import ru.raid.signal.v2.DataPresenter;
import ru.raid.signal.v2.Signal;
import ru.raid.signal.v2.SignalImpl;
import ru.raid.signal.v2.SignalSource;

public class LineGeneratorSource implements SignalSource {
	private DoubleUnaryOperator function;
	private double sampleRate;
	private long samplePeriod;
	private int size;
	private boolean enabled = false;
	private long initTime, deltaTime = 0, tempTime;
	
	public LineGeneratorSource(DoubleUnaryOperator func, double rate, int sz) {
		function = func;
		size = sz;
		sampleRate = rate;
		samplePeriod = Math.round(1000.0 / rate);
		tempTime = initTime = System.currentTimeMillis();
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (!this.enabled && enabled) {
			deltaTime += System.currentTimeMillis() - tempTime;
		}
		this.enabled = enabled;
		if (this.enabled && !enabled) {
			tempTime = System.currentTimeMillis();
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public Signal getSignal() {
		return new SignalImpl(this, new Presenter(), size);
	}

	private class Presenter implements DataPresenter {
		private long time = System.currentTimeMillis() - deltaTime;
		private long phase = (time - initTime) / samplePeriod - 1;
		
		@Override
		public boolean hasNext() {
			return enabled && System.currentTimeMillis() - time - deltaTime >= samplePeriod;
		}

		@Override
		public double next() {
			time += samplePeriod;
			phase++;
			return function.applyAsDouble(phase * 1.0 / sampleRate);
		}

		@Override
		public double getSampleRate() {
			return sampleRate;
		}

		@Override
		public double getTimeOffset() {
			return time *1.0 / 1000.0;
		}
	}
}
