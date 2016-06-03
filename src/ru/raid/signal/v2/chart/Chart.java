package ru.raid.signal.v2.chart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.raid.signal.v2.Signal;
import ru.raid.signal.v2.SignalListener;

public class Chart {
	private List<Signal> signals = new ArrayList<>();
	private String xName, yName;
	private boolean autoComputeX, autoComputeY, reload = true;
	private double[] xRange, yRange;
	private double[][] data;
	private XRangeCharacteristic[] xranges;
	private SignalListener listener = new Listener();
	
	public Chart(String nmx, String nmy) {
		setXName(nmx);
		setYName(nmy);
		autoComputeX = autoComputeY = true;
	}
	
	public void addSignal(Signal signal) {
		signals.add(signal);
		signal.addListener(listener);
		reload = true;
	}
	public void removeSignal(Signal signal) {
		signal.removeListener(listener);
		signals.remove(signal);
		reload = true;
	}
	public void clearSignals() {
		Signal[] copy = new Signal[signals.size()];
		signals.toArray(copy);
		for (Signal sgn : copy) {
			removeSignal(sgn);
		}
	}
	
	public void update() {
		for (Signal sgn : signals) {
			sgn.available();
		}
		if (reload) {
			reload = false;
			reload();
		}
	}
	
	public void setXRange(double min, double max) {
		autoComputeX = false;
		xRange = new double[] {min, max};
	}
	public void setYRange(double min, double max) {
		autoComputeY = false;
		yRange = new double[] {min, max};
	}
	public String getXName() {
		return xName;
	}
	public void setXName(String xName) {
		this.xName = xName;
	}
	public String getYName() {
		return yName;
	}
	public void setYName(String yName) {
		this.yName = yName;
	}
	
	public int channelsCount() {
		if (data == null) {
			return 0;
		}
		return data.length;
	}
	public double[] getData(int channel) {
		return Arrays.copyOf(data[channel], data[channel].length);
	}
	public XRangeCharacteristic getXRangeCharacteristic(int channel) {
		return xranges[channel];
	}
	
	public double[] getXRange() {
		return Arrays.copyOf(xRange, 2);
	}
	public double[] getYRange() {
		return Arrays.copyOf(yRange, 2);
	}
	
	private void reload() {
		allocate();
		if (autoComputeX) {
			xRange = computeXRange();
		}
		if (autoComputeY) {
			yRange = computeYRange();
		}
	}
	private void allocate() {
		data = new double[signals.size()][];
		xranges = new XRangeCharacteristic[signals.size()];
		int i=0;
		for (Signal sgn : signals) {
			int k = sgn.available();
			data[i] = sgn.getData(k);
			xranges[i] = new XRangeCharacteristic(sgn.getTimeOffset(k), sgn.getTimeOffset(1), sgn.getSamplePeriod());
			i++;
		}
	}
	private double[] computeXRange() {
		double ansMin = Double.POSITIVE_INFINITY, ansMax = Double.NEGATIVE_INFINITY;
		if (xranges != null) {
			for (XRangeCharacteristic xrn : xranges) {
				ansMin = Math.min(ansMin, xrn.getMin());
				ansMax = Math.max(ansMax, xrn.getMax());
			}
		}
		if (Double.isInfinite(ansMin) || Double.isInfinite(ansMax)) {
			ansMin = ansMax = 0.0;
		}
		return new double[] {ansMin, ansMax};
	}
	private double[] computeYRange() {
		double ansMin = Double.POSITIVE_INFINITY, ansMax = Double.NEGATIVE_INFINITY;
		if (data != null) {
			for (double[] dt : data) {
				for (double v : dt) {
					if (Double.isNaN(v)) {
						continue;
					}
					ansMin = Math.min(v, ansMin);
					ansMax = Math.max(v, ansMax); 
				}
			}
		}
		if (Double.isInfinite(ansMin) || Double.isInfinite(ansMax)) {
			ansMin = ansMax = 0.0;
		}
		return new double[] {ansMin, ansMax};
	}
	

	private class Listener implements SignalListener {
		@Override
		public void onSampleFiltered(Signal signal, double data) {
			reload = true;
		}
		
		@Override
		public void onNextSample(Signal signal, double data) {}
	}
	
	public static class XRangeCharacteristic {
		private final double min, max, period;
		
		public XRangeCharacteristic(double mn, double mx, double p) {
			min = mn;
			max = mx;
			period = p;
		}

		public double getMin() {
			return min;
		}
		public double getMax() {
			return max;
		}
		public double getPeriod() {
			return period;
		}
	}
}
