package ru.raid.signal.v2.chart.extended;

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Artist {
	private ArtistContext ctxDefault;
	private ArtistContext ctx;
	private List<Drawable> drawables = new ArrayList<>();
	private List<TacticDrawable> tactics = new ArrayList<>();
	private Range curRangeX, curRangeY;
	private Rectangle drawArea;
	private double xResolution, yResolution;
	private double xOffset, yOffset;
	private DrawContext drawCtx = new DContext();
	
	public Artist(ArtistContext defContext) {
		ctxDefault = defContext;
	}
	public Artist() {
		this(null);
	}

	public void addDrawable(Drawable db) {
		drawables.add(db);
	}
	public void removeDrawable(Drawable db) {
		drawables.remove(db);
	}
	public void clearDrawables() {
		drawables.clear();
	}
	public void addTactic(TacticDrawable db) {
		tactics.add(db);
	}
	public void removeTactic(TacticDrawable db) {
		tactics.remove(db);
	}
	public void clearTactics() {
		tactics.clear();;
	}
	

	
	public void repaint(ArtistContext ctx) {
		this.ctx = ctx;
		computeRanges();
		computeDrawArea();
		computeResolution();
		paintTactics();
		paint();
	}
	public void repaint() {
		repaint(ctxDefault);
	}
	
	private void paintTactics() {
		InnerContext ctxIn = new InnerContext(this);
		for (TacticDrawable db : tactics) {
			db.draw(drawCtx, ctx, ctxIn);
		}
	}
	private void paint() {
		for (Drawable db : drawables) {
			db.draw(drawCtx);
		}
	}
	
	private void computeRanges() {
		curRangeX = curRangeY = null;
		for (Drawable db : drawables) {
			if (curRangeX == null) {
				curRangeX = db.getXRange();
			} else {
				curRangeX = Range.disjunction(curRangeX, db.getXRange());
			}
			if (curRangeY == null) {
				curRangeY = db.getYRange();
			} else {
				curRangeY = Range.disjunction(curRangeY, db.getYRange());
			}
		}
		if (curRangeX == null) {
			curRangeX = new Range();
		}
		if (curRangeY == null) {
			curRangeY = new Range();
		}
	}
	private void computeDrawArea() {
		drawArea = new Rectangle(0, 0, ctx.getWidth(), ctx.getHeight());
		Insets ins = ctx.getInsets();
		if (ins != null) {
			drawArea.x = ins.left;
			drawArea.y = ins.top;
			drawArea.width -= ins.left + ins.right;
			drawArea.height -= ins.top + ins.bottom;
		}
	}
	private void computeResolution() {
		if (curRangeX != null)
			xResolution = drawArea.getWidth() / curRangeX.getRange();
		if (curRangeY != null)
			yResolution = drawArea.getHeight() / curRangeY.getRange();
	}
	
	public static class InnerContext {
		private Range xRange, yRange;
		private double xResolution, yResolution;
		private Rectangle drawArea;
		
		public InnerContext(Artist art) {
			xRange = art.curRangeX;
			yRange = art.curRangeY;
			xResolution = art.xResolution;
			yResolution = art.yResolution;
			drawArea = art.drawArea;
		}

		public Range getXRange() {
			return xRange;
		}
		public Range getYRange() {
			return yRange;
		}
		public double getXResolution() {
			return xResolution;
		}
		public double getYResolution() {
			return yResolution;
		}
		public Rectangle getDrawArea() {
			return new Rectangle(drawArea);
		}
	}
	
	private class DContext implements DrawContext {
		@Override
		public Graphics2D getGraphics() {
			return ctx.getGraphics();
		}

		@Override
		public Point recompute(double x, double y) {
			x += xOffset;
			y += yOffset;
			return new Point(
						(int) Math.round(xResolution * (x - curRangeX.getMin())) + drawArea.x,
						(int) Math.round(yResolution * (curRangeY.getMax() - y)) + drawArea.y
					);
		}
	}
}
