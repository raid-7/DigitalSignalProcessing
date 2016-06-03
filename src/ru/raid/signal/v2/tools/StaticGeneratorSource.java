package ru.raid.signal.v2.tools;

import java.util.function.DoubleUnaryOperator;
import java.util.stream.IntStream;

import ru.raid.signal.v2.Signal;
import ru.raid.signal.v2.SignalImpl;
import ru.raid.signal.v2.SignalSource;
import ru.raid.signal.v2.StaticPresenter;

public class StaticGeneratorSource implements SignalSource {
	private boolean enabled;
	private double[] data;
	private double sampleRate;
	private double start;
	
	public StaticGeneratorSource(DoubleUnaryOperator func, double smpRate, double start, int size) {
		this.start = start;
		data = IntStream.range(0, size).mapToDouble((t) -> t*1.0/smpRate + start).map(func).toArray();
		sampleRate = smpRate;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public Signal getSignal() {
		return new SignalImpl(this, new StaticPresenter(data, sampleRate, start), data.length);
	}
}
