package ru.raid.signal.v2.chart.extended;

import java.awt.Graphics2D;
import java.awt.Insets;

public interface ArtistContext {
	public Graphics2D getGraphics();
	public int getWidth();
	public int getHeight();
	public Insets getInsets();
}
