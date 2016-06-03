package ru.raid.signal.v2.chart;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JPanel;
import javax.swing.border.Border;


public class ChartDrawer extends JPanel {
	private static final long serialVersionUID = 4648285705595887504L;
	private static final double RANGE_ADD_MULTIPLIER = 1.8e-2;
	
	private Chart chart;
	private ChartViewSettings settings;
	
	private double xGridStep, yGridStep;
	private double xResolution, yResolution;
	private Rectangle drawArea;
	private double[] xRange = {0, 0}, yRange = null;
	
	public ChartDrawer(ChartViewSettings set) {
		setViewSettings(set);
		setOpaque(true);
	}
	public ChartDrawer(ChartViewSettings set, Chart chart) {
		this(set);
		this.setChart(chart);
	}
	
	public Chart getChart() {
		return chart;
	}
	public void setChart(Chart chart) {
		this.chart = chart;
		yRange = null;
	}
	public void setViewSettings(ChartViewSettings settings) {
		this.settings = settings;
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(settings.getBackgroundColor());
		g.fillRect(0, 0, getWidth(), getHeight());
		
		if (chart != null) {
			Graphics2D g2d = (Graphics2D) g;
			paintPrepare();
			innerPaint(g2d);
		}
	}
	
	private void computeDrawArea() {
		drawArea = new Rectangle(0, 0, getWidth(), getHeight());
		Border border = getBorder();
		Insets ins;
		if (border != null && (ins = border.getBorderInsets(this)) != null) {
			drawArea.x = ins.left;
			drawArea.y = ins.top;
			drawArea.width -= ins.left + ins.right;
			drawArea.height -= ins.top + ins.bottom;
		}
	}
	private void paintPrepare() {
		chart.update();
		computeDrawArea();
		
		double[] nxr = chart.getXRange();
		double nx = nxr[1] - nxr[0];
		double[] nyr = chart.getYRange();
		double ny = nyr[1] - nyr[0];
		xRange[0] = nxr[0] - nx * RANGE_ADD_MULTIPLIER;
		xRange[1] = nxr[1] + nx * RANGE_ADD_MULTIPLIER;
		if (yRange == null) {
			yRange = new double[] {
				nyr[0] - ny * RANGE_ADD_MULTIPLIER,
				nyr[1] + ny * RANGE_ADD_MULTIPLIER
			};
		} else {
			yRange[0] = Math.min(yRange[0], nyr[0] - ny * RANGE_ADD_MULTIPLIER);
			yRange[1] = Math.max(yRange[1], nyr[1] + ny * RANGE_ADD_MULTIPLIER);
		}
		
		xResolution = drawArea.getWidth() / (xRange[1] - xRange[0]);
		yResolution = drawArea.getHeight() / (yRange[1] - yRange[0]);
		
		xGridStep = settings.getXGridStep()*1.0 / xResolution;
		yGridStep = settings.getYGridStep()*1.0 / yResolution;
	}
	private void innerPaint(Graphics2D g) {
		paintMainAxis(g);
		paintGrid(g);
		
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		for (int i=0; i<chart.channelsCount(); i++) {
			paintChartChannel(i, g);
		}
	}
	private void paintChartChannel(int channel, Graphics2D g) {
		g.setStroke(settings.getChartStroke());
		g.setColor(settings.getChannelColorTable().getColor(channel));
		
		double[] data = chart.getData(channel);
		Chart.XRangeCharacteristic xrn = chart.getXRangeCharacteristic(channel);
		for (int i=0; i<data.length-1; i++) {
			if (Double.isNaN(data[i]) || Double.isNaN(data[i+1])) {
				continue;
			}
			double cx1 = xrn.getMin() + i*xrn.getPeriod();
			double cx2 = xrn.getMin() + (i+1)*xrn.getPeriod();
			Point p1 = recompute(cx1, data[i]);
			Point p2 = recompute(cx2, data[i+1]);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
	}
	private void paintMainAxis(Graphics2D g) {
		double xLineY = calcXLinePos();
		double yLineX = calcYLinePos();
		Point xLineStart = recompute(xRange[0], xLineY);
		Point xLineEnd = recompute(xRange[1], xLineY);
		Point yLineStart = recompute(yLineX, yRange[0]);
		Point yLineEnd = recompute(yLineX, yRange[1]);
		
		if (settings.drawMainAxis()) {
			g.setStroke(settings.getMainAxisStroke());
			g.setColor(settings.getMainAxisColor());
			
			g.drawLine(xLineStart.x, xLineStart.y, xLineEnd.x, xLineEnd.y);
			g.drawLine(yLineStart.x, yLineStart.y, yLineEnd.x, yLineEnd.y);
		}
		
		if (settings.drawAxisNames()) {
			g.setColor(settings.getAxisNameColor());
			g.setFont(settings.getAxisNameFont());
			FontMetrics fm = g.getFontMetrics();
			
			String xStr = chart.getXName();
			Point xNamePoint = new Point(xLineEnd.x-fm.stringWidth(xStr)-1, xLineEnd.y+fm.getAscent()+1);
			g.drawString(xStr, xNamePoint.x, xNamePoint.y);
			
			String yStr = chart.getYName();
			Point yNamePoint = new Point(yLineEnd.x-fm.stringWidth(yStr)-1, yLineEnd.y+fm.getAscent()+1);
			g.drawString(yStr, yNamePoint.x, yNamePoint.y);
		}
	}
	private double calcXLinePos() {
		double xLineY = 0.0;
		if (yRange[0] >= -0.0 || yRange[1] <= +0.0) {
			xLineY = Math.ceil(yRange[0] / yGridStep) * yGridStep;
		}
		return xLineY;
	}
	private double calcYLinePos() {
		double yLineX = 0.0;
		if (xRange[0] >= -0.0 || xRange[1] <= +0.0) {
			yLineX = Math.ceil(xRange[0] / xGridStep) * xGridStep;
		}
		return yLineX;
	}
	private void paintGrid(Graphics2D g) {
		g.setFont(settings.getNumbersFont());
		
		double xMainY = calcXLinePos();
		double yMainX = calcYLinePos();
		
		int xGridStart = (int)Math.floor(xRange[0] / xGridStep);
		int xGridEnd = (int)Math.floor(xRange[1] / xGridStep);
		double d = 0.0;
		if (Math.abs(yMainX) > 1e-12) {
			d = xRange[0] - xGridStart * xGridStep;
		}
		for (int i=xGridStart; i<=xGridEnd; i++) {
			if (settings.drawGrid()) {
				Point top = recompute(i*xGridStep+d, yRange[0]);
				Point bottom = recompute(i*xGridStep+d, yRange[1]);
				g.setColor(settings.getGridColor());
				g.setStroke(settings.getGridStroke());
				g.drawLine(top.x, top.y, bottom.x, bottom.y);
			}

			if (settings.drawNumbers()) {
				String text = String.format("%.3g", i*xGridStep);
				Point textPoint = recompute(i*xGridStep+d, xMainY);
				textPoint.x += 1;
				textPoint.y += g.getFontMetrics().getAscent();
				g.setColor(settings.getNumbersColor());
				g.drawString(text, textPoint.x, textPoint.y);
			}
		}
		
		int yGridStart = (int)Math.floor(yRange[0] / yGridStep);
		int yGridEnd = (int)Math.floor(yRange[1] / yGridStep);
		for (int i=yGridStart; i<=yGridEnd; i++) {
			if (settings.drawGrid()) {
				Point left = recompute(xRange[0], i*yGridStep);
				Point right = recompute(xRange[1], i*yGridStep);
				g.setColor(settings.getGridColor());
				g.setStroke(settings.getGridStroke());
				g.drawLine(left.x, left.y, right.x, right.y);
			}

			if (settings.drawNumbers()) {
				String text = String.format("%.3g", i*yGridStep);
				Point textPoint = recompute(yMainX, i*yGridStep);
				textPoint.x += 1;
				textPoint.y -= g.getFontMetrics().getDescent();
				g.setColor(settings.getNumbersColor()); 
				g.drawString(text, textPoint.x, textPoint.y);
			}
		}
	}
	private Point recompute(double x, double y) {
		return new Point(
					(int) Math.round(xResolution * (x - xRange[0])) + drawArea.x,
					(int) Math.round(yResolution * (yRange[1] - y)) + drawArea.y
				);
	}
}
