package ru.raid.signal.v2;


/**
 * Real time or static sample sequence. 
 * 
 * @author Райд
 *
 */
public interface Signal {
	/**
	 * Returns last len samples of signal.
	 * @param len Number of samples will be returned
	 * @return Array of samples
	 */
	public double[] getData(int len);
	
	/**
	 * Number of samples available this moment.
	 * @return Number of samples
	 */
	public int available();
	
	/**
	 * Adds signal filter.
	 * @param filter Adding filter
	 */
	public void addFilter(Filter filter);
	
	/**
	 * Removes signal filter.
	 * @param filter Removing filter
	 */
	public void removeFilter(Filter filter);
	
	/**
	 * Adds signal listener.
	 * @param listener Adding listener
	 */
	public void addListener(SignalListener listener);
	
	/**
	 * Adds signal listener using WeakReference.
	 * @param listener Adding listener
	 */
	public void addWeakListener(SignalListener listener);
	
	/**
	 * Removes signal listener.
	 * @param listener Removing listener
	 */
	public void removeListener(SignalListener listener);
	
	/**
	 * Creates the signal which always contains last len samples of this signal. 
	 * @param len Cutting length
	 * @return Cut signal
	 */
	public Signal cut(int len);
	
	/**
	 * Creates static signal, containing last len samples of this sample in call moment
	 * @param len Packing length
	 * @return Static pack signal
	 */
	public Signal pack(int len);
	
	/**
	 * Filter the signal and return result.
	 * @param f Filter
	 * @return Filtered signal
	 */
	public Signal filter(Filter f);
	
	/**
	 * Creates new signal applying filter to calling signal.
	 * @param f Filter
	 * @return Transformed signal
	 */
	public Signal transform(Filter f);
	
	/**
	 * Gets sample rate.
	 * @return Sample rate
	 */
	public double getSampleRate();
	
	/**
	 * Gets period of sample.
	 * @return Sample period
	 */
	public double getSamplePeriod();
	
	/**
	 * Gets the source of the signal.
	 * @return Sample source
	 */
	public SignalSource getSource();
	
	/**
	 * Compute time offset for last len samples.
	 * 
	 * @param len Number of samples
	 * @return Time of receiving the first of len last samples
	 */
	public double getTimeOffset(int len);
}
