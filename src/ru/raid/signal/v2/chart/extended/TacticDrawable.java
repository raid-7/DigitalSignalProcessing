package ru.raid.signal.v2.chart.extended;

import ru.raid.signal.v2.chart.extended.Artist.InnerContext;

public interface TacticDrawable {
	public void draw(DrawContext ctxDr, ArtistContext ctxAr, InnerContext ctxIn);
}
