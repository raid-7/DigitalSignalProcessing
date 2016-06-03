package ru.raid.signal.v2.chart.extended;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;

import ru.raid.signal.v2.chart.extended.Artist.InnerContext;

public class GridTactic implements TacticDrawable {
	private Color mainAxisColor, gridColor, numbersColor, axisNameColor, backgroundColor;
	private Stroke mainAxisStroke, gridStroke, chartStroke;
	private Font numbersFont, axisNameFont;
	private boolean drawMainAxis, drawGrid, drawNumbers, drawAxisName;
	private int xGridStepPx, yGridStepPx;
	private String xAxisName = "", yAxisName = "";

	public GridTactic() {
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
	public void draw(DrawContext ctxDr, ArtistContext ctxAr, InnerContext ctxIn) {
		Rectangle r = ctxIn.getDrawArea();
		ctxDr.getGraphics().setColor(getBackgroundColor());
		ctxDr.getGraphics().fillRect(r.x, r.y, r.width, r.height);
		
		double[] gs = new double[]{
				getXGridStep()*1.0 / ctxIn.getXResolution(),
				getYGridStep()*1.0 / ctxIn.getYResolution()	
		};
		
		paintMainAxis(ctxDr, ctxIn, gs);
		paintGrid(ctxDr, ctxIn, gs);
	}

	private void paintMainAxis(DrawContext ctxDr, InnerContext ctxIn, double[] gs) {
		Graphics2D g = ctxDr.getGraphics();
		
		double xLineY = calcXLinePos(ctxIn, gs);
		double yLineX = calcYLinePos(ctxIn, gs);
		Point xLineStart = ctxDr.recompute(ctxIn.getXRange().getMin(), xLineY);
		Point xLineEnd = ctxDr.recompute(ctxIn.getXRange().getMax(), xLineY);
		Point yLineStart = ctxDr.recompute(yLineX, ctxIn.getYRange().getMin());
		Point yLineEnd = ctxDr.recompute(yLineX, ctxIn.getYRange().getMax());
		
		if (drawMainAxis()) {
			g.setStroke(getMainAxisStroke());
			g.setColor(getMainAxisColor());
			
			g.drawLine(xLineStart.x, xLineStart.y, xLineEnd.x, xLineEnd.y);
			g.drawLine(yLineStart.x, yLineStart.y, yLineEnd.x, yLineEnd.y);
		}
		
		if (drawAxisNames()) {
			g.setColor(getAxisNameColor());
			g.setFont(getAxisNameFont());
			FontMetrics fm = g.getFontMetrics();
			
			String xStr = getXAxisName();
			Point xNamePoint = new Point(xLineEnd.x-fm.stringWidth(xStr)-1, xLineEnd.y+fm.getAscent()+1);
			g.drawString(xStr, xNamePoint.x, xNamePoint.y);
			
			String yStr = getYAxisName();
			Point yNamePoint = new Point(yLineEnd.x-fm.stringWidth(yStr)-1, yLineEnd.y+fm.getAscent()+1);
			g.drawString(yStr, yNamePoint.x, yNamePoint.y);
		}
	}
	private double calcXLinePos(InnerContext ctxIn, double[] gs) {
		double xLineY = 0.0;
		if (ctxIn.getYRange().getMin() >= -0.0 || ctxIn.getYRange().getMax() <= +0.0) {
			xLineY = Math.ceil(ctxIn.getYRange().getMin() / gs[1]) * gs[1];
		}
		return xLineY;
	}
	private double calcYLinePos(InnerContext ctxIn, double[] gs) {
		double yLineX = 0.0;
		if (ctxIn.getXRange().getMin() >= -0.0 || ctxIn.getXRange().getMax() <= +0.0) {
			yLineX = Math.ceil(ctxIn.getXRange().getMin() / gs[0]) * gs[0];
		}
		return yLineX;
	}
	private void paintGrid(DrawContext ctxDr, InnerContext ctxIn, double[] gs) {
		Graphics2D g = ctxDr.getGraphics();
		
		g.setFont(getNumbersFont());
		
		double xMainY = calcXLinePos(ctxIn, gs);
		double yMainX = calcYLinePos(ctxIn, gs);
		
		int xGridStart = (int)Math.floor(ctxIn.getXRange().getMin() / gs[0]);
		int xGridEnd = (int)Math.floor(ctxIn.getXRange().getMax() / gs[0]);
		for (int i=xGridStart; i<=xGridEnd; i++) {
			if (drawGrid()) {
				Point top = ctxDr.recompute(i*gs[0], ctxIn.getYRange().getMax());
				Point bottom = ctxDr.recompute(i*gs[0], ctxIn.getYRange().getMin());
				g.setColor(getGridColor());
				g.setStroke(getGridStroke());
				g.drawLine(top.x, top.y, bottom.x, bottom.y);
			}

			if (drawNumbers()) {
				String text = String.format("%.3g", i*gs[0]);
				Point textPoint = ctxDr.recompute(i*gs[0], xMainY);
				textPoint.x += 1;
				textPoint.y += g.getFontMetrics().getAscent();
				g.setColor(getNumbersColor());
				g.drawString(text, textPoint.x, textPoint.y);
			}
		}
		
		int yGridStart = (int)Math.floor(ctxIn.getYRange().getMin() / gs[1]);
		int yGridEnd = (int)Math.floor(ctxIn.getYRange().getMax() / gs[1]);
		for (int i=yGridStart; i<=yGridEnd; i++) {
			if (drawGrid()) {
				Point left = ctxDr.recompute(ctxIn.getXRange().getMin(), i*gs[1]);
				Point right = ctxDr.recompute(ctxIn.getXRange().getMax(), i*gs[1]);
				g.setColor(getGridColor());
				g.setStroke(getGridStroke());
				g.drawLine(left.x, left.y, right.x, right.y);
			}

			if (drawNumbers()) {
				String text = String.format("%.3g", i*gs[1]);
				Point textPoint = ctxDr.recompute(yMainX, i*gs[1]);
				textPoint.x += 1;
				textPoint.y -= g.getFontMetrics().getDescent();
				g.setColor(getNumbersColor()); 
				g.drawString(text, textPoint.x, textPoint.y);
			}
		}
	}
	
	
	

	public String getXAxisName() {
		return xAxisName;
	}
	public void setXAxisName(String s) {
		xAxisName = s;
	}
	public String getYAxisName() {
		return yAxisName;
	}
	public void setYAxisName(String s) {
		yAxisName = s;
	}
	
	public Color getMainAxisColor() {
		return mainAxisColor;
	}
	public void setMainAxisColor(Color color) {
		mainAxisColor = color;
	}

	public Color getGridColor() {
		return gridColor;
	}
	public void setGridColor(Color color) {
		gridColor = color;
	}

	public Color getNumbersColor() {
		return numbersColor;
	}
	public void setNumbersColor(Color color) {
		numbersColor = color;
	}

	public Color getAxisNameColor() {
		return axisNameColor;
	}
	public void setAxisNameColor(Color color) {
		axisNameColor = color;
	}

	public Stroke getMainAxisStroke() {
		return mainAxisStroke;
	}
	public void setMainAxisStroke(Stroke stroke) {
		mainAxisStroke = stroke;
	}

	public Stroke getGridStroke() {
		return gridStroke;
	}
	public void setGridStroke(Stroke stroke) {
		gridStroke = stroke;
	}

	public Stroke getChartStroke() {
		return chartStroke;
	}
	public void setChartStroke(Stroke stroke) {
		chartStroke = stroke;
	}

	public Font getNumbersFont() {
		return numbersFont;
	}
	public void setNumbersFont(Font font) {
		numbersFont = font;
	}

	public Font getAxisNameFont() {
		return axisNameFont;
	}
	public void setAxisNameFont(Font font) {
		axisNameFont = font;
	}

	public boolean drawGrid() {
		return drawGrid && gridColor != null && gridStroke != null && xGridStepPx != 0 && yGridStepPx != 0;
	}
	public void setDrawGrid(boolean v) {
		drawGrid = v;
	}

	public boolean drawMainAxis() {
		return drawMainAxis && mainAxisColor != null && mainAxisStroke != null;
	}
	public void setDrawMainAxis(boolean v) {
		drawMainAxis = v;
	}

	public boolean drawNumbers() {
		return drawNumbers && numbersFont != null && numbersColor != null;
	}
	public void setDrawNumbers(boolean v) {
		drawNumbers = v;
	}

	public boolean drawAxisNames() {
		return drawAxisName && axisNameFont != null && axisNameColor != null;
	}
	public void setDrawAxisNames(boolean v) {
		drawAxisName = v;
	}

	public int getXGridStep() {
		return xGridStepPx;
	}
	public void setXGridStep(int px) {
		xGridStepPx = px;
	}

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

	public Color getBackgroundColor() {
		return backgroundColor;
	}
	public void setBackgroundColor(Color color) {
		backgroundColor = color;
	}
}
