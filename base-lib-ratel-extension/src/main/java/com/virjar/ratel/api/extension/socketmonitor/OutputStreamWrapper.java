package com.virjar.ratel.api.extension.socketmonitor;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamWrapper extends OutputStream {
    private OutputStream delegate;

    private MonitorMode monitorMode;


    private static ThreadLocal<Object> reentryFlag = new ThreadLocal<>();

    OutputStreamWrapper(OutputStream delegate, MonitorMode monitorMode) {
        this.delegate = delegate;
        this.monitorMode = monitorMode;
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        delegate.write(bytes);

        boolean reEntry = reentryFlag.get() != null;
        if (!reEntry) {
            reentryFlag.set(new Object());
        }
        try {
            if (reEntry) {
                return;
            }
            monitorMode.outputWrite(bytes);
        } finally {
            if (!reEntry) {
                reentryFlag.remove();
            }
        }
    }

    @Override
    public void write(byte[] bytes, int i, int i1) throws IOException {
        delegate.write(bytes, i, i1);
        boolean reEntry = reentryFlag.get() != null;
        if (!reEntry) {
            reentryFlag.set(new Object());
        }
        try {
            if (reEntry) {
                return;
            }
            monitorMode.outputWrite(bytes, i, i1);
        } finally {
            if (!reEntry) {
                reentryFlag.remove();
            }
        }
    }

    @Override
    public void write(int i) throws IOException {
        delegate.write(i);
        boolean reEntry = reentryFlag.get() != null;
        if (!reEntry) {
            reentryFlag.set(new Object());
        }
        try {
            if (reEntry) {
                return;
            }
            monitorMode.outputWrite(i);
        } finally {
            if (!reEntry) {
                reentryFlag.remove();
            }
        }
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
        monitorMode.close();
    }


}
