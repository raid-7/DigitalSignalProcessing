package ru.raid.signal.v2.io;

import java.io.Closeable;
import java.io.Flushable;

public interface SignalWriter extends Closeable, Flushable {
	public void setEnabled(boolean b);
	public boolean isEnabled();
}
