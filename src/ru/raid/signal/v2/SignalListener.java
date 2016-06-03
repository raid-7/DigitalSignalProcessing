package ru.raid.signal.v2;

public interface SignalListener {
	/**
	 * Called when signal accepts new sample.
	 * @param signal Calling signal
	 * @param data New sample
	 */
	public void onNextSample(Signal signal, double data);
	
	/**
	 * Called when some sample has been filtered. 
	 * @param signal Calling signal
	 * @param data Filtered sample
	 */
	public void onSampleFiltered(Signal signal, double data);
}
