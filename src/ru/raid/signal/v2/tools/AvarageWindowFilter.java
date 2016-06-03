package ru.raid.signal.v2.tools;

import ru.raid.signal.v2.Filter;
import ru.raid.signal.v2.Signal;

public class AvarageWindowFilter implements Filter {
	private int l, r;
	
	public AvarageWindowFilter(int left, int right) {
		l = left;
		r = right;
	}

	@Override
	public int getBeforeOffset() {
		return l;
	}

	@Override
	public int getAfterOffset() {
		return r;
	}

	@Override
	public double filter(Signal signal, double[] data, int p) {
		double sum = 0.0;
		int n = 0;
		for (int i=p-l; i<=p+r; i++) {
			if (Double.isFinite(data[i]) && !Double.isNaN(data[i])) {
				sum += data[i];
				n++;
			}
		}
		return sum / n;
	}
}
