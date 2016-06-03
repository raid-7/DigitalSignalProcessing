package ru.raid.signal.v2.chart.extended;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.border.Border;

public class ArtistPanel extends JPanel {
	private static final long serialVersionUID = -1117225000989029405L;
	
	private Artist artist;
	
	public ArtistPanel(Artist art) {
		artist = art;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		artist.repaint(new ArtistContext() {
			@Override
			public Graphics2D getGraphics() {
				return (Graphics2D) g;
			}

			@Override
			public int getWidth() {
				return ArtistPanel.this.getWidth();
			}

			@Override
			public int getHeight() {
				return ArtistPanel.this.getHeight();
			}

			@Override
			public Insets getInsets() {
				Border brd = getBorder();
				if (brd == null)
					return null;
				return brd.getBorderInsets(ArtistPanel.this);
			}
		});
	}

}
