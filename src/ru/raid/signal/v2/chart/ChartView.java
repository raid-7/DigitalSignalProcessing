package ru.raid.signal.v2.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;

public class ChartView implements ChartViewSettings {
	private ChannelColorTable channelColorTable;
	private Color mainAxisColor, gridColor, numbersColor, axisNameColor, backgroundColor;
	private Stroke mainAxisStroke, gridStroke, chartStroke;
	private Font numbersFont, axisNameFont;
	private boolean drawMainAxis, drawGrid, drawNumbers, drawAxisName;
	private int xGridStepPx, yGridStepPx;
	
	public ChartView() {
		setChannelColorTable(new RandomColorTable());
		setMainAxisColor(Color.BLACK);
		setGridColor(Color.GRAY);
		setNumbersColor(Color.BLACK);
		setAxisNameColor(Color.BLACK);
		setMainAxisStroke(new BasicStroke(2.4f));
		setGridStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
						10.0f, new float[]{16.0f, 8.0f}, 0.0f));
		setChartStroke(new BasicStroke(1.2f));
		setNumbersFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setAxisNameFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
		setDrawAxisNames(true);
		setDrawGrid(true);
		setDrawMainAxis(true);
		setDrawNumbers(true);
		setGridStep(60, 60);
		setBackgroundColor(Color.WHITE);
	}

	@Override
	public ChannelColorTable getChannelColorTable() {
		return channelColorTable;
	}
	public void setChannelColorTable(ChannelColorTable table) {
		channelColorTable = table;
	}

	@Override
	public Color getMainAxisColor() {
		return mainAxisColor;
	}
	public void setMainAxisColor(Color color) {
		mainAxisColor = color;
	}

	@Override
	public Color getGridColor() {
		return gridColor;
	}
	public void setGridColor(Color color) {
		gridColor = color;
	}

	@Override
	public Color getNumbersColor() {
		return numbersColor;
	}
	public void setNumbersColor(Color color) {
		numbersColor = color;
	}

	@Override
	public Color getAxisNameColor() {
		return axisNameColor;
	}
	public void setAxisNameColor(Color color) {
		axisNameColor = color;
	}

	@Override
	public Stroke getMainAxisStroke() {
		return mainAxisStroke;
	}
	public void setMainAxisStroke(Stroke stroke) {
		mainAxisStroke = stroke;
	}

	@Override
	public Stroke getGridStroke() {
		return gridStroke;
	}
	public void setGridStroke(Stroke stroke) {
		gridStroke = stroke;
	}

	@Override
	public Stroke getChartStroke() {
		return chartStroke;
	}
	public void setChartStroke(Stroke stroke) {
		chartStroke = stroke;
	}

	@Override
	public Font getNumbersFont() {
		return numbersFont;
	}
	public void setNumbersFont(Font font) {
		numbersFont = font;
	}

	@Override
	public Font getAxisNameFont() {
		return axisNameFont;
	}
	public void setAxisNameFont(Font font) {
		axisNameFont = font;
	}

	@Override
	public boolean drawGrid() {
		return drawGrid && gridColor != null && gridStroke != null && xGridStepPx != 0 && yGridStepPx != 0;
	}
	public void setDrawGrid(boolean v) {
		drawGrid = v;
	}

	@Override
	public boolean drawMainAxis() {
		return drawMainAxis && mainAxisColor != null && mainAxisStroke != null;
	}
	public void setDrawMainAxis(boolean v) {
		drawMainAxis = v;
	}

	@Override
	public boolean drawNumbers() {
		return drawNumbers && numbersFont != null && numbersColor != null;
	}
	public void setDrawNumbers(boolean v) {
		drawNumbers = v;
	}

	@Override
	public boolean drawAxisNames() {
		return drawAxisName && axisNameFont != null && axisNameColor != null;
	}
	public void setDrawAxisNames(boolean v) {
		drawAxisName = v;
	}

	@Override
	public int getXGridStep() {
		return xGridStepPx;
	}
	public void setXGridStep(int px) {
		xGridStepPx = px;
	}

	@Override
	public int getYGridStep() {
		return yGridStepPx;
	}
	public void setYGridStep(int px) {
		yGridStepPx = px;
	}

	public void setGridStep(int xPx, int yPx) {
		setXGridStep(xPx);
		setYGridStep(yPx);
	}

	@Override
	public Color getBackgroundColor() {
		return backgroundColor;
	}
	public void setBackgroundColor(Color color) {
		backgroundColor = color;
	}
}
