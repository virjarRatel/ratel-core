package com.virjar.ratel.api.extension.socketmonitor;

import android.util.Log;

import com.virjar.ratel.api.extension.socketmonitor.observer.SocketDataObserver;
import com.virjar.ratel.api.inspect.ClassLoadMonitor;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLSocket;


public class SocketMonitor {

    public static final String TAG = "SocketMonitor";

    private static Set<Method> hookedMethod = Collections.newSetFromMap(new ConcurrentHashMap<Method, Boolean>());

    private static Set<SocketDataObserver> socketDataObservers = new HashSet<>();

    private static ThreadLocal<Boolean> disableMonitor = new ThreadLocal<>();

    static {
        startMonitorInternal();
    }

    public static void setCurrentThreadMonitorFlag(boolean needMonitor) {
        disableMonitor.set(!needMonitor);
    }

    private static boolean skipMonitor() {
        Boolean aBoolean = disableMonitor.get();
        if (aBoolean == null) {
            return false;
        }
        return aBoolean;
    }

    private static void startMonitorInternal() {
        //由于我们的arthook不支持hook虚方法，所以无法通过监控构造函数的方法拦截所有socket
        monitorSocketClass(SSLSocket.class);
        monitorSocketClass(Socket.class);

        //这是Android内置的socket对象，由于在BootClassLoader中提前初始化了，所以通过ClassLoadMonitor无法监控到
        Class<?> Java8FileDescriptorSocketClass = RposedHelpers.findClassIfExists("com.android.org.conscrypt.Java8FileDescriptorSocket", ClassLoader.getSystemClassLoader());
        if (Java8FileDescriptorSocketClass != null) {
            monitorSocketClass(Java8FileDescriptorSocketClass);
        }

        Class<?> ConscryptEngineSocketClass = RposedHelpers.findClassIfExists("com.android.org.conscrypt.ConscryptEngineSocket", ClassLoader.getSystemClassLoader());
        if (ConscryptEngineSocketClass != null) {
            monitorSocketClass(ConscryptEngineSocketClass);
        }

        Class<?> ConscryptFileDescriptorSocketClass = RposedHelpers.findClassIfExists("com.android.org.conscrypt.ConscryptFileDescriptorSocket", ClassLoader.getSystemClassLoader());
        if (ConscryptFileDescriptorSocketClass != null) {
            monitorSocketClass(ConscryptFileDescriptorSocketClass);
        }

        //如果没有在运行前初始化，那么可以通过ClassLoadMonitor来监控
        ClassLoadMonitor.addClassLoadMonitor(new ClassLoadMonitor.OnClassLoader() {
            @Override
            public void onClassLoad(Class<?> clazz) {
                if (Socket.class.isAssignableFrom(clazz)) {
                    monitorSocketClass(clazz);
                }
            }
        });

    }

    private static void monitorSocketClass(Class socketClass) {

        //monitor socket input,
        Method getInputStreamMethod = RposedHelpers.findMethodBestMatch(socketClass, "getInputStream");
        if (getInputStreamMethod != null && !hookedMethod.contains(getInputStreamMethod)) {
            Log.i(TAG, "add monitor socket class: " + socketClass.getName());
            RposedBridge.hookMethod(getInputStreamMethod, new RC_MethodHook() {
                @Override
                protected void afterHookedMethod(RC_MethodHook.MethodHookParam param) {
                    if (skipMonitor()) {
                        return;
                    }
                    InputStream inputStream = (InputStream) param.getResult();
                    if (inputStream == null) {
                        return;
                    }

                    if (inputStream instanceof InputStreamWrapper) {
                        return;
                    }
                    param.setResult(new InputStreamWrapper(inputStream, MonitorModeRegistry.createOrGet((Socket) param.thisObject)));
                }
            });
            hookedMethod.add(getInputStreamMethod);
        }

        //monitor socket output,
        Method getOutputStreamMethod = RposedHelpers.findMethodBestMatch(socketClass, "getOutputStream");
        if (getOutputStreamMethod != null && !hookedMethod.contains(getOutputStreamMethod)) {
            RposedBridge.hookMethod(getOutputStreamMethod, new RC_MethodHook() {
                @Override
                protected void afterHookedMethod(RC_MethodHook.MethodHookParam param) {
                    if (skipMonitor()) {
                        return;
                    }
                    OutputStream outputStream = (OutputStream) param.getResult();
                    if (outputStream == null) {
                        return;
                    }

                    if (outputStream instanceof OutputStreamWrapper) {
                        return;
                    }
                    param.setResult(new OutputStreamWrapper(outputStream, MonitorModeRegistry.createOrGet((Socket) param.thisObject)));
                    // 实践发现动态代理机制无法使用，很多class都是private的，无法正常被继承
                }
            });
            hookedMethod.add(getOutputStreamMethod);
        }


        Method closeMethod = RposedHelpers.findMethodBestMatch(socketClass, "close");
        if (closeMethod != null && !hookedMethod.contains(closeMethod)) {
            RposedBridge.hookMethod(closeMethod, new RC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    MonitorModeRegistry.createOrGet((Socket) param.thisObject).close();
                }
            });
        }
    }

    public static synchronized void addPacketEventObserver(SocketDataObserver socketDataObserver) {
        socketDataObservers.add(socketDataObserver);
    }

    public static synchronized void setPacketEventObserver(SocketDataObserver eventObserver) {
        socketDataObservers.clear();
        socketDataObservers.add(eventObserver);
    }

    static void callDataObserver(SocketDataObserver.DataModel dataModel) {
        for (SocketDataObserver socketDataObserver : socketDataObservers) {
            socketDataObserver.onDecodeSocketData(dataModel);
        }
    }
}
