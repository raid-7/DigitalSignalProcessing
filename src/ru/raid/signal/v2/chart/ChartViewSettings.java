package ru.raid.signal.v2.chart;

import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;

public interface ChartViewSettings {
	public ChannelColorTable getChannelColorTable();
	public Color getMainAxisColor();
	public Color getGridColor();
	public Color getNumbersColor();
	public Color getAxisNameColor();
	public Stroke getMainAxisStroke();
	public Stroke getGridStroke();
	public Stroke getChartStroke();
	public Font getNumbersFont();
	public Font getAxisNameFont();
	public boolean drawGrid();
	public boolean drawMainAxis();
	public boolean drawNumbers();
	public boolean drawAxisNames();
	public int getXGridStep();
	public int getYGridStep();
	public Color getBackgroundColor();
}
