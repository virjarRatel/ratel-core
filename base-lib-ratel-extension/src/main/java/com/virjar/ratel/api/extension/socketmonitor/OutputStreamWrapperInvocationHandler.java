package com.virjar.ratel.api.extension.socketmonitor;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OutputStreamWrapperInvocationHandler implements InvocationHandler {
    private OutputStream delegate;
    private MonitorMode monitorMode;
    private static Map<Method, Method> methodMapping = new ConcurrentHashMap<>();
    private static ThreadLocal<Object> reentryFlag = new ThreadLocal<>();


    OutputStreamWrapperInvocationHandler(OutputStream delegate, MonitorMode monitorMode) {
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
        monitorMode.makeSureWriteTrace();
        return thisMethod.invoke(this, objects);
    }

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

    public void flush() throws IOException {
        delegate.flush();
        monitorMode.getOutputSteamData().flush();
    }

    public void close() throws IOException {
        delegate.close();
        monitorMode.getOutputSteamData().close();
        monitorMode.close();
    }


}
