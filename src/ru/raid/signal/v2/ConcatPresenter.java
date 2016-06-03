package ru.raid.signal.v2;

import java.util.Arrays;

/**
 * Concats data from a few presenters
 * 
 * @author Raid
 *
 */
public class ConcatPresenter implements DataPresenter {
	private DataPresenter[] presenters;
	private double time;
	
	public ConcatPresenter(DataPresenter ... pres) {
		presenters = Arrays.copyOf(pres, pres.length);
		time = presenters[0].getTimeOffset();
		for (DataPresenter p : presenters) {
			if (p.hasNext()) {
				time = p.getTimeOffset();
				break;
			}
		}
	}

	@Override
	public boolean hasNext() {
		for (DataPresenter p : presenters) {
			if (p.hasNext()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public double next() {
		for (DataPresenter p : presenters) {
			if (p.hasNext()) {
				double t = p.next();
				time = p.getTimeOffset();
				return t;
			}
		}
		throw new IllegalStateException("No next!");
	}

	@Override
	public double getSampleRate() {
		for (DataPresenter p : presenters) {
			if (p.hasNext()) {
				return p.getSampleRate();
			}
		}
		return presenters[presenters.length-1].getSampleRate();
	}

	@Override
	public double getTimeOffset() {
		return time;
	}
}
