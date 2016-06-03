package ru.raid.signal.v2.chart.extended;

public class Range {
	private double min, max;
	
	public Range() {
		this(0.0, 0.0);
	}
	public Range(double a, double b) {
		min = Math.min(a, b);
		max = Math.max(a, b);
	}
	public Range(Range r) {
		this(r.getMin(), r.getMax());
	}
	
	public double getMin() {
		return min;
	}
	public double getMax() {
		return max;
	}
	public double getRange() {
		return max - min;
	}
	public double[] toArray() {
		return new double[] {min, max};
	}
	
	public static Range disjunction(Range a, Range b) {
		return new Range(Math.min(a.getMin(), b.getMin()), Math.max(a.getMax(), b.getMax()));
	}
}
