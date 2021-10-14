package com.virjar.ratel.api.extension.socketmonitor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InputStreamWrapperInvocationHandler implements InvocationHandler {
    private InputStream delegate;
    private MonitorMode monitorMode;

    private static Map<Method, Method> methodMapping = new ConcurrentHashMap<>();

    private static ThreadLocal<Object> reentryFlag = new ThreadLocal<>();


    InputStreamWrapperInvocationHandler(InputStream delegate, MonitorMode monitorMode) {
        this.delegate = delegate;
        this.monitorMode = monitorMode;
    }

    private synchronized void genCache(Method fromMethod) {
        for (Method thisMethod : this.getClass().getDeclaredMethods()) {
            if (MethodUtils.isMethodSame(thisMethod, fromMethod)) {
                methodMapping.put(fromMethod, thisMethod);
                return;
            }
        }
        methodMapping.put(fromMethod, null);
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        if (!methodMapping.containsKey(method)) {
            genCache(method);
        }
        Method thisMethod = methodMapping.get(method);
        if (thisMethod == null) {
            return method.invoke(delegate, objects);
        }
        monitorMode.makeSureReadTrace();
        return thisMethod.invoke(this, objects);
    }

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

    public void close() throws IOException {
        delegate.close();
        monitorMode.getInputStreamData().close();
        monitorMode.close();
    }
//
//    public long skip(long l) throws IOException {
//        return delegate.skip(l);
//    }
//
//
//    public int available() throws IOException {
//        return delegate.available();
//    }
//
//
//
//    public synchronized void mark(int i) {
//        delegate.mark(i);
//    }
//
//    public synchronized void reset() throws IOException {
//        delegate.reset();
//    }
//
//    public boolean markSupported() {
//        return delegate.markSupported();
//    }
}
