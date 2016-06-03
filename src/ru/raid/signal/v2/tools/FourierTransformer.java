package ru.raid.signal.v2.tools;

import java.util.Arrays;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import ru.raid.signal.v2.DataPresenter;
import ru.raid.signal.v2.Signal;
import ru.raid.signal.v2.SignalImpl;
import ru.raid.signal.v2.SignalListener;
import ru.raid.signal.v2.SignalSource;

public class FourierTransformer implements SignalSource {
	public static final int TYPE_AMPLITUDE = 0;
	public static final int TYPE_PHASE = 1;
	
	private FastFourierTransformer fftEngine = new FastFourierTransformer(DftNormalization.STANDARD);
	private SignalListener listener = new Listener();
	private Signal signal;
	private int type;
	private double restep;
	private boolean enabled = false;
	private double[][] currentTransform;
	private double frequencyStep;
	private int transformVersion = -1;
	private int transformSize = 0;
	private int news = 0;

	public FourierTransformer(Signal sgn, int tp, double gStep) {
		signal = sgn;
		type = tp;
		restep = gStep;
	}
	public FourierTransformer(Signal sgn, double gStep) {
		this(sgn, TYPE_AMPLITUDE, gStep);
	}

	
	
	@Override
	public void setEnabled(boolean enabled) {
		if (this.enabled == enabled) {
			return;
		}
		this.enabled = enabled;
		if (enabled) {
			signal.addListener(listener);
			recompute();
		} else {
			signal.removeListener(listener);
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public Signal getSignal() {
		return new InnerSignal(new Presenter());
	}
	
	
	private void recompute() {
		news = 0;
		dftForward();
	}

	private void dftForward() {
		Complex[] data = fftForward();
		double normal = 2.0 / data.length;
		double[] ampArr = Arrays.stream(data).mapToDouble((c) -> c.abs() * normal).toArray();
		double[] phsArr = Arrays.stream(data).mapToDouble((c) -> c.getArgument()).toArray();
		double freqRate = signal.getSampleRate() / data.length;
		transformVersion++;
		transformSize = data.length >> 1;
		frequencyStep = freqRate;
		currentTransform = new double[][]{ampArr, phsArr};
	}
	
	private Complex[] fftForward() {
		double[] dt = allocate();
		if (dt == null || dt.length == 0) {
			return new Complex[0];
		}
		return fftEngine.transform(dt, TransformType.FORWARD);
	}
	
	private double[] allocate() {
		int t = signal.available();
		int len = Integer.highestOneBit(t);
		if (len != t) {
			len <<= 1;
		}
		return signal.getData(len);
	}
	
	private class InnerSignal extends SignalImpl {
		public InnerSignal(DataPresenter presenter) {
			super(FourierTransformer.this, presenter, 1);
		}

		@Override
		public int available() {
			maxSize = transformSize;
			return Math.min(transformSize, super.available());
		}
	}
	private class Listener implements SignalListener {
		@Override
		public void onNextSample(Signal signal, double data) {}

		@Override
		public void onSampleFiltered(Signal signal, double data) {
			news++;
			if (news*signal.getSamplePeriod() >= restep) {
				recompute();
			}
		}
	}
	private class Presenter implements DataPresenter {
		private int version = -1;
		private int position = 0;
		
		@Override
		public boolean hasNext() {
			relax();
			return position < transformSize;
		}

		@Override
		public double next() {
			relax();
			return currentTransform[type][position++];
		}

		@Override
		public double getSampleRate() {
			return 1 / frequencyStep;
		}
		
		private void relax() {
			if (version < transformVersion) {
				version = transformVersion;
				position = 0;
			}
		}

		@Override
		public double getTimeOffset() {
			return (position-1) * frequencyStep;
		}
	}
}
