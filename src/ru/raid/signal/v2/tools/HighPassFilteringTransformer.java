package ru.raid.signal.v2.tools;

import java.util.ArrayDeque;
import java.util.Queue;

import ru.raid.signal.v2.DataPresenter;
import ru.raid.signal.v2.QueuePresenter;
import ru.raid.signal.v2.Signal;
import ru.raid.signal.v2.SignalImpl;
import ru.raid.signal.v2.SignalListener;
import ru.raid.signal.v2.SignalPresenter;
import ru.raid.signal.v2.SignalSource;

public class HighPassFilteringTransformer implements SignalSource {
	private double rc;
	private Signal signal;
	private int size;
	
	private double k;
	private boolean enabled;
	private SignalListener listener = new Listener();
	
	private Signal innerSignal;
	private Queue<Double> data = new ArrayDeque<>();
	private double lastFiltered, lastNonfiltered;
	

	public HighPassFilteringTransformer(Signal sgn, double RC, int maxSize) {
		signal = sgn;
		rc = RC;
		size = maxSize;
		k = rc / (rc + signal.getSamplePeriod());
		innerSignal = new SignalImpl(this, new QueuePresenter(data, signal.getSampleRate(), signal.getTimeOffset(0)), size);
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (this.enabled == enabled) {
			return;
		}
		this.enabled = enabled;
		if (enabled) {
			signal.addListener(listener);
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
		return new InnerSignal(this, new SignalPresenter(innerSignal), size);
	}

	private class Listener implements SignalListener {
		@Override
		public void onNextSample(Signal signal, double data) {}

		@Override
		public void onSampleFiltered(Signal signal, double dt) {
			double x = dt;
			double xl = lastNonfiltered;
			double yl = lastFiltered;
			double y = k * (yl + x - xl);
			
			lastFiltered = y;
			lastNonfiltered = x;
			
			synchronized (data) {
				data.add(y);
			}
		}
	}
	private class InnerSignal extends SignalImpl {
		public InnerSignal(SignalSource source, DataPresenter presenter, int maxSize) {
			super(source, presenter, maxSize);
		}

		@Override
		protected synchronized void update() {
			signal.available();
			super.update();
		}
	}
}
