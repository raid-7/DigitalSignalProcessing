package ru.raid.signal.v2;

/**
 * Sample presenter.
 * 
 * @author Raid
 */
public interface DataPresenter {
	/**
	 * Checks are the new samples available now.
	 * @return true if there are new samples, false otherwise
	 */
	public boolean hasNext();
	
	/**
	 * Gets next sample.
	 * @return New sample
	 * @throws IllegalStateException if there are no new samples
	 */
	public double next();
	
	/**
	 * Gets the sample rate.
	 * @return Sample rate
	 */
	public double getSampleRate();
	
	/**
	 * Compute current sample time.
	 * @return Time of receiving last sample, got by next()
	 */
	public double getTimeOffset();
}
