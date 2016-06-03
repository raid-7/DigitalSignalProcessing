package ru.raid.signal.v2.tools;

import java.util.function.DoubleUnaryOperator;

import ru.raid.signal.v2.Filter;
import ru.raid.signal.v2.Signal;

public class FunctionalFilter implements Filter {
	private DoubleUnaryOperator filter;
	
	public FunctionalFilter(DoubleUnaryOperator func) {
		filter = func;
	}

	@Override
	public int getBeforeOffset() {
		return 0;
	}

	@Override
	public int getAfterOffset() {
		return 0;
	}

	@Override
	public double filter(Signal signal, double[] data, int filteringPosition) {
		return filter.applyAsDouble(data[filteringPosition]);
	}
}
