package ru.raid.signal.v2.chart.extended;

public interface Drawable {
	public void draw(DrawContext ctx);
	public Range getXRange();
	public Range getYRange();
}
