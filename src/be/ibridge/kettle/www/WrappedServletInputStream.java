package be.ibridge.kettle.www;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

public class WrappedServletInputStream extends ServletInputStream {

    private final InputStream inputStream;

    public WrappedServletInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public int available() throws IOException {
        return inputStream.available();
    }

    public void close() throws IOException {
        inputStream.close();
    }

    public synchronized void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    public boolean markSupported() {
        return inputStream.markSupported();
    }

    public int read(byte[] b) throws IOException {
        return inputStream.read(b);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    public synchronized void reset() throws IOException {
        inputStream.reset();
    }

    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    public int read() throws IOException {
        return inputStream.read();
    }
}
