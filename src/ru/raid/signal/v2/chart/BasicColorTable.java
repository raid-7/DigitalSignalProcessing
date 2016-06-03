package ru.raid.signal.v2.chart;

import java.awt.Color;
import java.util.Arrays;

public class BasicColorTable implements ChannelColorTable {
	private Color[] colors;
	private int size;
	
	public BasicColorTable(Color... colors) {
		size = colors.length;
		this.colors = Arrays.copyOf(colors, size);
	}

	@Override
	public Color getColor(int channel) {
		return colors[channel % size];
	}

}
