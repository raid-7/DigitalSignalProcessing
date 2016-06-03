package ru.raid.signal.v2.io;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Queue;

import ru.raid.signal.v2.ConcatPresenter;
import ru.raid.signal.v2.DataPresenter;
import ru.raid.signal.v2.QueuePresenter;
import ru.raid.signal.v2.Signal;
import ru.raid.signal.v2.SignalImpl;
import ru.raid.signal.v2.SignalListener;
import ru.raid.signal.v2.SignalPresenter;
import ru.raid.signal.v2.StaticPresenter;

public class StreamRecorder implements Recorder {
	private static final int maxSize = 100_000_000;
	private static final int bufMaxSize = 8192;
	private static Recorder streamRecorder;
	
	private StreamRecorder() {}

	@Override
	public SignalReader read(InputStream stream) throws IOException {
		final StreamReader reader = new StreamReader(stream);
		reader.init();
		return new SignalReader() {
			private boolean enabled = false;
			
			@Override
			public void setEnabled(boolean b) {
				if (enabled == b) {
					return;
				}
				enabled = b;
				
				
			}

			@Override
			public boolean isEnabled() {
				return enabled;
			}

			@Override
			public Signal getSignal() {
				return new SignalImpl(this, reader.getPresenter(), maxSize) {
					@Override
					protected void update() {
						if (enabled) {
							try {
								reader.update();
							} catch (IOException e) {}
						}
						super.update();
					}
				};
			}

			@Override
			public void close() throws IOException {
				setEnabled(false);
				reader.close();
			}
		};
	}

	@Override
	public SignalWriter write(Signal sgn, OutputStream stream) throws IOException {
		StreamWriter writer = new StreamWriter(stream);
		writer.begin(sgn);
		return new SignalWriter() {
			private boolean enabled = false;
			
			@Override
			public void setEnabled(boolean b) {
				if (b == enabled) {
					return;
				}
				enabled = b;
				
				if (b) {
					sgn.addListener(writer);
				} else {
					sgn.removeListener(writer);
				}
			}

			@Override
			public boolean isEnabled() {
				return enabled;
			}
			
			
			@Override
			public void close() throws IOException {
				setEnabled(false);
				writer.close();
			}

			@Override
			public void flush() throws IOException {
				writer.flush();
			}
		};
	}

	public static Recorder getRecorder() {
		return streamRecorder == null ? (streamRecorder = new StreamRecorder()) : streamRecorder;
	}
	

	
	private class StreamReader implements Closeable {
		private InputStream is;
		private byte[] buf;
		private int bufPos;
		private int bufSize;
		
		private int size;
		private double sampleRate;
		private double startTime;
		private double[] data;
		private Signal additionalInner;
		private Queue<Double> que;
		
		private boolean inited = false;
		
		public StreamReader(InputStream inst) {
			is = inst;
		}
		
		public void init() throws IOException {
			if (inited) {
				return;
			}
			inited = true;
			readMetadata();
			readData();
		}
		
		public void update() throws IOException {
			while (bufSize - bufPos >= 8 || is.available() >= 8) {
				que.add(readDouble());
			}
		}
		
		private void readMetadata() throws IOException {
			size = readInt();
			sampleRate = readDouble();
			startTime = readDouble();
			size = Math.min(size, maxSize);
		}
		
		private void readData() throws IOException {
			data = new double[size];
			for (int i=0; i<size; i++) {
				data[i] = readDouble();
			}
			
			double time = startTime + size / sampleRate;
			que = new ArrayDeque<>();
			additionalInner = new SignalImpl(null, new QueuePresenter(que, sampleRate, time), maxSize);
		}
		
		public DataPresenter getStaticPresenter() {
			return new StaticPresenter(data, sampleRate, startTime);
		}
		public DataPresenter getAdditionalPresenter() {
			return new SignalPresenter(additionalInner);
		}
		public DataPresenter getPresenter() {
			return new ConcatPresenter(getStaticPresenter(), getAdditionalPresenter());
		}
		
		public void close() throws IOException {
			is.close();
		}
		
		private int readInt() throws IOException {
			allocate(4);
			byte[] bs = buf;
			int p = bufPos;
			bufPos += 4;
			return (bs[p] << 24) | (bs[p+1] << 16) | (bs[p+2] << 8) | bs[p+3];
		}
		private long readLong() throws IOException {
			long a = readInt();
			long b = readInt();
			return (a << 32) | b;
		}
		private double readDouble() throws IOException {
			return Double.longBitsToDouble(readLong());
		}
		
		private void allocateNext() throws IOException {
			byte[] newBuf = new byte[bufMaxSize];
			int k = 0;
			if (buf != null) {
				k = bufSize - bufPos;
				System.arraycopy(buf, bufPos, newBuf, 0, k);
			}
			
			int t = is.read(newBuf, k, bufMaxSize - k);
			if (t < 0) {
				throw new IOException("Bad, bad, bad... Happy New Year, guy! 2016");
			}
			
			buf = newBuf;
			bufPos = 0;
			bufSize = k + t;
		}
		private void allocate(int l) throws IOException {
			if (bufSize - bufPos < l) {
				allocateNext();
			}
		}
	}
	private class StreamWriter implements Closeable, Flushable, SignalListener {
		private OutputStream os;
		private byte[] buf;
		private int bufPos;
		private int size;
		
		public StreamWriter(OutputStream oust) {
			os = oust;
		}

		public void begin(Signal sgn) throws IOException {
			writeMetadata(sgn);
			writeSamples(sgn.getData(size));
		}
		
		public void writeSample(double v) throws IOException {
			writeDouble(v);
		}
		public void writeSamples(double[] vs) throws IOException {
			for (double x : vs) {
				writeDouble(x);
			}
		}

		@Override
		public void onNextSample(Signal signal, double data) {}

		@Override
		public void onSampleFiltered(Signal signal, double data) {
			try {
				writeSample(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void flush() throws IOException {
			allocateNext();
			os.flush();
		}
		@Override
		public void close() throws IOException {
			flush();
			os.close();
		}

		private void writeMetadata(Signal sgn) throws IOException {
			size = sgn.available();
			size = Math.min(size, maxSize);
			double rate = sgn.getSampleRate();
			double time = sgn.getTimeOffset(size);
			writeInt(size);
			writeDouble(rate);
			writeDouble(time);
		}
		
		private void writeInt(int x) throws IOException {
			allocate(4);
			int p = bufPos;
			bufPos += 4;
			buf[p+0] = (byte) ((x >>> 24) & 0xFF);
			buf[p+1] = (byte) ((x >>> 16) & 0xFF);
			buf[p+2] = (byte) ((x >>>  8) & 0xFF);
			buf[p+3] = (byte) ((x >>>  0) & 0xFF);
		}
		
		private void writeLong(long x) throws IOException {
			writeInt((int) ((x >>> 32) & 0xFF));
			writeInt((int) ( x         & 0xFF));
		}
		
		private void writeDouble(double v) throws IOException {
			writeLong(Double.doubleToLongBits(v));
		}
		
		private void allocateNext() throws IOException {
			os.write(buf, 0, bufPos);
			buf = new byte[bufMaxSize];
			bufPos = 0;
		}
		private void allocate(int k) throws IOException {
			if (bufMaxSize - bufPos < k) {
				allocateNext();
			}
		}
	}
}
