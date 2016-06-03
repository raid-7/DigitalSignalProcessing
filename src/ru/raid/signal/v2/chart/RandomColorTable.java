package ru.raid.signal.v2.chart;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomColorTable implements ChannelColorTable {
	private Random random;
	private List<Color> table;
	
	public RandomColorTable(long seed) {
		random = new Random();
		table = new ArrayList<>();
	}
	public RandomColorTable() {
		this(System.currentTimeMillis());
	}

	@Override
	public Color getColor(int channel) {
		generate(channel - table.size() + 1);
		return table.get(channel);
	}
	
	private Color next() {
		return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
	}
	private void generate(int x) {
		for (int i=0; i<x; i++) {
			table.add(next());
		}
	}
}
