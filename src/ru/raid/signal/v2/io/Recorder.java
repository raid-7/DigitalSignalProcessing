package ru.raid.signal.v2.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ru.raid.signal.v2.Signal;

public interface Recorder {
	public SignalReader read(InputStream stream) throws IOException;
	public SignalWriter write(Signal signal, OutputStream stream) throws IOException;
}
