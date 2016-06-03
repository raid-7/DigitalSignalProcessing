package ru.raid.signal.v2.chart.extended;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

public interface DrawContext {
	public Graphics2D getGraphics();
	public Point recompute(double x, double y);
	
	public default Point recompute(Point2D.Double p) {
		return recompute(p.getX(), p.getY());
	}
}
