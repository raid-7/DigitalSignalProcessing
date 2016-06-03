package ru.raid.signal.v2;

public interface Filter {
	/**
	 * Computes required data length before filtering value.
	 * @return Required number of samples
	 */
	public int getBeforeOffset();
	
	/**
	 * Computes required data length after filtering value.
	 * @return Required number of samples
	 */
	public int getAfterOffset();
	
	/**
	 * Filter value.
	 * @param signal Calling signal
	 * @param data Array of samples. Length &gt;= {@link Filter#getBeforeOffset()} + 1 + {@link Filter#getAfterOffset()}
	 * @param filteringPosition Position of filtering value in data
	 * 
	 * @return Filtered value
	 */
	public double filter(Signal signal, double[] data, int filteringPosition);
}
