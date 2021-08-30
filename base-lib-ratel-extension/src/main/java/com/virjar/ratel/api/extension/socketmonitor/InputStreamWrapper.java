package com.virjar.ratel.api.extension.socketmonitor;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamWrapper extends InputStream {

    private InputStream delegate;
    private MonitorMode monitorMode;


    private static ThreadLocal<Object> reentryFlag = new ThreadLocal<>();


    @Override
    public int read() throws IOException {
        boolean reEntry = reentryFlag.get() != null;
        if (!reEntry) {
            reentryFlag.set(new Object());
        }
        try {
            int data = delegate.read();
            if (reEntry) {
                return data;
            }
            if (data > 0) {
                monitorMode.inputWrite(data);
            }
            return data;
        } finally {
            if (!reEntry) {
                reentryFlag.remove();
            }
        }
    }

    public InputStreamWrapper(InputStream delegate, MonitorMode monitorMode) {
        this.delegate = delegate;
        this.monitorMode = monitorMode;
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        boolean reEntry = reentryFlag.get() != null;

        if (!reEntry) {
            reentryFlag.set(new Object());
        }
        try {
            int readSize = delegate.read(bytes);
            if (reEntry) {
                return readSize;
            }
            if (readSize > 0) {
                monitorMode.inputWrite(bytes, 0, readSize);
            }
            return readSize;
        } finally {
            if (!reEntry) {
                reentryFlag.remove();
            }
        }
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        boolean reEntry = reentryFlag.get() != null;

        if (!reEntry) {
            reentryFlag.set(new Object());
        }
        try {
            int readSize = delegate.read(bytes, off, len);
            if (reEntry) {
                return readSize;
            }
            if (readSize > 0) {
                monitorMode.inputWrite(bytes, off, readSize);

            }
            return readSize;
        } finally {
            if (!reEntry) {
                reentryFlag.remove();
            }
        }
    }

    @Override
    public long skip(long l) throws IOException {
        return delegate.skip(l);
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
        monitorMode.close();
    }

    @Override
    public synchronized void mark(int i) {
        delegate.mark(i);
    }

    @Override
    public synchronized void reset() throws IOException {
        delegate.reset();
    }

    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }
}
