package ru.raid.signal.v2;

/**
 * Source of signal.
 * 
 * @author Raid
 *
 */
public interface SignalSource {
	/**
	 * Changes source state.
	 * @param enabled
	 * If true the source provides updates for its signals, if false the source stop updating all its signals
	 */
	public void setEnabled(boolean enabled);
	
	/**
	 * Gets source state.
	 * @return Source state. 
	 * If true the source provides updates for its signals, if false the source stop updating all its signals
	 */
	public boolean isEnabled();
	
	/**
	 * Creates new instance of signal producing by the source.
	 * @return Created signal
	 */
	public Signal getSignal();
}
