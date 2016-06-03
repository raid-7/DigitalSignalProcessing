package ru.raid.signal.v2.tools;

import ru.raid.signal.v2.Filter;
import ru.raid.signal.v2.Signal;

public class LowPassFilter implements Filter {
	private double rc;
	
	/**
	 * Cutoff frequency of the filter f=1/(2*pi*rc)
	 * 
	 * @param RC Time constant
	 */
	public LowPassFilter(double RC) {
		rc = RC;
	}
	
	@Override
	public int getBeforeOffset() {
		return 1;
	}

	@Override
	public int getAfterOffset() {
		return 0;
	}

	@Override
	public double filter(Signal sgn, double[] data, int pos) {
		double t = sgn.getSamplePeriod();
		double k = t / (rc + t);
		return data[pos-1] * (1-k) + data[pos] * k;
	}
}
