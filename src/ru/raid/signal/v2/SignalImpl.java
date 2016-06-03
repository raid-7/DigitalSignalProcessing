package ru.raid.signal.v2;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Queue;
import java.util.TreeMap;

public class SignalImpl implements Signal {
	protected DataPresenter presenter;
	protected SignalSource source;
	protected Signal parent = null;
	protected int maxSize;
	protected List<SignalListener> listeners = new ArrayList<>();
	protected List<Reference<SignalListener>> weakListeners = new ArrayList<>();
	protected NavigableMap<Integer, List<Filter>> filterForwards = new TreeMap<>();
	protected int size = 0;
	protected double[] data = new double[0];
	protected double timeOffset = 0.0;
	
	public SignalImpl(SignalSource source, DataPresenter presenter, int maxSize) {
		this.presenter = presenter;
		this.source = source;
		this.maxSize = maxSize;
		this.timeOffset = presenter.getTimeOffset();
	}
	public SignalImpl(SignalSource source, Signal parent, DataPresenter presenter, int maxSize) {
		this(source, presenter, maxSize);
		this.parent = parent;
	}

	@Override
	public double[] getData(int len) {
		double[] out = new double[len];
		int copyLen = Math.min(len, available());
		System.arraycopy(data, size-copyLen, out, len-copyLen, copyLen);
		return out;
	}

	@Override
	public int available() {
		update();
		return Math.min(size, maxSize);
	}

	@Override
	public void addFilter(Filter filter) {
		Integer key = filter.getAfterOffset();
		if (!filterForwards.containsKey(key)) {
			filterForwards.put(key, new ArrayList<>());
		}
		filterForwards.get(key).add(filter);
	}

	@Override
	public void removeFilter(Filter filter) {
		Integer key = filter.getAfterOffset();
		if (filterForwards.containsKey(key)) {
			filterForwards.get(key).remove(filter);
		}
	}

	@Override
	public void addListener(SignalListener listener) {
		listeners.add(listener);
	}

	@Override
	public void addWeakListener(SignalListener listener) {
		Reference<SignalListener> ref = new WeakReference<>(listener);
		weakListeners.add(ref);
	}
	
	@Override
	public void removeListener(SignalListener listener) {
		listeners.remove(listener);
	}

	@Override
	public Signal cut(int len) {
		int k = Math.min(available(), len);
		DataPresenter current = new StaticPresenter(getData(k), getSampleRate(), getTimeOffset(k));
		DataPresenter future = new SignalPresenter(this);
		DataPresenter result = new ConcatPresenter(current, future);
		return new SignalImpl(source, this, result, len);
	}
	
	@Override
	public Signal pack(int len) {
		DataPresenter current = new StaticPresenter(getData(len), getSampleRate(), getTimeOffset(len));
		return new SignalImpl(source, current, len);
	}

	@Override
	public Signal filter(Filter f) {
		Signal res = cut(maxSize);
		res.addFilter(f);
		return res;
	}
	
	@Override
	public Signal transform(Filter f) {
		Queue<Double> que = new ArrayDeque<>();
		Signal buf = filter(new Filter() {
			@Override
			public int getBeforeOffset() {
				return f.getBeforeOffset();
			}

			@Override
			public int getAfterOffset() {
				return f.getAfterOffset();
			}

			@Override
			public double filter(Signal signal, double[] data, int fp) {
				double v = f.filter(signal, data, fp);
				que.add(v);
				return data[fp];
			}
		});
		buf.addListener(new SignalListener() {
			private int k = 0;
			
			@Override
			public void onSampleFiltered(Signal signal, double data) {}
			
			@Override
			public void onNextSample(Signal signal, double data) {
				if (k < f.getBeforeOffset()) {
					que.add(data);
					k++;
				}
			}
		});
		Signal res = new SignalImpl(source, buf,
								new QueuePresenter(que, getSampleRate(), getTimeOffset(available())), maxSize);
		return res;
	}

	@Override
	public double getTimeOffset(int len) {
		return timeOffset - (len-1) * getSamplePeriod();
	}
	
	@Override
	public double getSampleRate() {
		return presenter.getSampleRate();
	}

	@Override
	public double getSamplePeriod() {
		return 1 / getSampleRate();
	}

	@Override
	public SignalSource getSource() {
		return source;
	}

	protected synchronized void update() {
		if (parent != null) {
			parent.available();
		}
		
		if (!presenter.hasNext())
			return;
		
		List<Double> accumulator = new LinkedList<>();
		while (presenter.hasNext()) {
			accumulator.add(presenter.next());
		}
		double lastOffset = presenter.getTimeOffset();
		addNext(accumulator, lastOffset);
		
		update();
	}

	private void addNext(List<Double> adds, double lastOffset) {
		if (size + adds.size() > data.length) {
			int newSize = size + adds.size();
			int t = Integer.highestOneBit(newSize);
			newSize = Math.min(maxSize*2, t==newSize ? t : (t << 1));
			double[] newData = new double[newSize];
			int copyLength = Math.min(size, Math.max(0, maxSize-adds.size()));
			System.arraycopy(data, size-copyLength, newData, 0, copyLength);
			size = copyLength;
			data = newData;
		}

		int startOffset = Math.max(0, adds.size()-maxSize);
		double offset = lastOffset - adds.size() * getSamplePeriod();
		for (Double d : adds) {
			offset += getSamplePeriod();
			if (--startOffset < 0)
				handle(d, offset);
		}
	}
	private void handle(double v, double time) {
		invokeListenersOnNext(v);
		
		boolean callListeners = true;
		
		data[size] = v;
		if (!filterForwards.isEmpty()) {
			int maxKey = filterForwards.lastKey();
			if (maxKey != 0) {
				callListeners = false;
			}
			for (Entry<Integer, List<Filter>> fwOffset : filterForwards.entrySet()) {
				int p = size - fwOffset.getKey();
				if (p < 0) {
					continue;
				}
				for (Filter f : fwOffset.getValue()) {
					if (f.getBeforeOffset() <= p) {
						data[p] = f.filter(this, data, p);
					}
				}
				if (fwOffset.getKey() == maxKey && !callListeners) {
					invokeListenersOnFiltered(data[p]);
				}
			}
		}
		v = data[size];
		timeOffset = time;
		size++;
		
		if (callListeners) {
			invokeListenersOnFiltered(v);
		}
	}
	
	private void invokeListenersOnNext(double v) {
		for (SignalListener lis : listeners) {
			lis.onNextSample(this, v);
		}
		
		Iterator<Reference<SignalListener>> it = weakListeners.iterator();
		while (it.hasNext()) {
			Reference<SignalListener> ref = it.next();
			if (ref.get() == null) {
				it.remove();
			} else {
				ref.get().onNextSample(this, v);
			}
		}
	}
	private void invokeListenersOnFiltered(double v) {
		for (SignalListener lis : listeners) {
			lis.onSampleFiltered(this, v);
		}
		
		Iterator<Reference<SignalListener>> it = weakListeners.iterator();
		while (it.hasNext()) {
			Reference<SignalListener> ref = it.next();
			if (ref.get() == null) {
				it.remove();
			} else {
				ref.get().onSampleFiltered(this, v);
			}
		}
	}
}
