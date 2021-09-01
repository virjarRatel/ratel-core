package com.virjar.ratel.envmock.binder.servicehook;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.envmock.binder.BindMethodHook;
import com.virjar.ratel.envmock.binder.BinderHookManager;
import com.virjar.ratel.envmock.binder.BinderHookParcelHandler;
import com.virjar.ratel.runtime.RatelRuntime;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceIPCHookManager {

    private static final Map<ServiceConnection, ServiceConnection> proxyMap = new ConcurrentHashMap<>();

    private static final Map<String, PendingServiceHookHolder> serviceHookHolderRegistry = new ConcurrentHashMap<>();

    private static class PendingServiceHookHolder {
        private String serviceKey;
        private final Map<String, BindMethodHook> methodHookRegistry = new ConcurrentHashMap<>();
        private final Map<Integer, BindMethodHook> methodCodeHookRegistry = new ConcurrentHashMap<>();
        private final Map<String, BinderHookParcelHandler> parcelHandleRegistry = new ConcurrentHashMap<>();
    }

    private static void setup() {

        Class<?> contextImplClass = RposedHelpers.findClass("android.app.ContextImpl", ClassLoader.getSystemClassLoader());
        if (contextImplClass == null) {
            throw new RuntimeException("can not load contextImplClass");
        }
        //android.app.ContextImpl#bindServiceCommon
        RposedBridge.hookAllMethods(contextImplClass, "bindServiceCommon", new RC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                doConnectionReplace(param.args);
            }
        });
    }

    private static void doConnectionReplace(Object[] bindServiceCommonArgs) {
        ServiceConnection serviceConnection = null;
        int serviceConnectionIndex = -1;
        Intent intent = null;
        for (int i = 0; i < bindServiceCommonArgs.length; i++) {
            Object arg = bindServiceCommonArgs[i];
            if (arg instanceof ServiceConnection) {
                serviceConnection = (ServiceConnection) arg;
                serviceConnectionIndex = i;
            } else if (arg instanceof Intent) {
                intent = (Intent) arg;
            }
            if (serviceConnection != null && intent != null) {
                break;
            }
        }
        if (serviceConnection == null || intent == null) {
            Log.w(Constants.TAG, "can not parse arg list for bindServiceCommon");
            return;
        }
        bindServiceCommonArgs[serviceConnectionIndex] = replace(intent, serviceConnection);
    }

    private static ServiceConnection replace(Intent intent, ServiceConnection serviceConnection) {
        String targetPackage = intent.getPackage();
        String action = intent.getAction();

        if (TextUtils.isEmpty(targetPackage) || TextUtils.isEmpty(action)) {
            return serviceConnection;
        }
        String serviceKey = genInterfaceKey(targetPackage, action);
        if (RatelRuntime.isRatelDebugBuild) {
            Log.i(Constants.TAG, "on service connection" + serviceKey);
        }
        PendingServiceHookHolder pendingServiceHookHolder = serviceHookHolderRegistry.get(serviceKey);
        if (pendingServiceHookHolder == null) {
            //没有注册hook
            return serviceConnection;
        }

        Log.i(Constants.TAG, "do service hook:" + serviceKey);

        ServiceConnection proxy = proxyMap.get(serviceConnection);
        if (proxy != null) {
            return proxy;
        }
        synchronized (ServiceIPCHookManager.class) {
            proxy = (ServiceConnection) Proxy.newProxyInstance(serviceConnection.getClass().getClassLoader(),
                    new Class[]{ServiceConnection.class}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                            if (method.getDeclaringClass().equals(Object.class)) {
                                return method.invoke(this, objects);
                            }
                            if (method.getName().equals("onServiceConnected")) {
                                handleOnServiceConnected(serviceKey, (ComponentName) objects[0], (IBinder) objects[1]);
                            }
                            return method.invoke(serviceConnection, objects);
                        }
                    });
            proxyMap.put(serviceConnection, proxy);
        }
        return proxy;
    }

    private static void handleOnServiceConnected(String serviceKey, ComponentName componentName, IBinder iBinder) {
        // now setup  real binder hook
        PendingServiceHookHolder pendingServiceHookHolder = createOrGet(serviceKey);
        for (BinderHookParcelHandler binderHookParcelHandler : pendingServiceHookHolder.parcelHandleRegistry.values()) {
            BinderHookManager.addParcelHandler(binderHookParcelHandler, iBinder);
        }
        for (String method : pendingServiceHookHolder.methodHookRegistry.keySet()) {
            BindMethodHook bindMethodHook = pendingServiceHookHolder.methodHookRegistry.get(method);
            BinderHookManager.addBinderHook(serviceKey, method, -1, bindMethodHook);
        }

        for (Integer method : pendingServiceHookHolder.methodCodeHookRegistry.keySet()) {
            BindMethodHook bindMethodHook = pendingServiceHookHolder.methodCodeHookRegistry.get(method);
            BinderHookManager.addBinderHook(serviceKey, null, method, bindMethodHook);
        }
    }

    private static PendingServiceHookHolder createOrGet(String serviceKey) {
        PendingServiceHookHolder pendingServiceHookHolder = serviceHookHolderRegistry.get(serviceKey);
        if (pendingServiceHookHolder != null) {
            return pendingServiceHookHolder;
        }

        synchronized (ServiceIPCHookManager.class) {
            pendingServiceHookHolder = serviceHookHolderRegistry.get(serviceKey);
            if (pendingServiceHookHolder != null) {
                return pendingServiceHookHolder;
            }
            pendingServiceHookHolder = new PendingServiceHookHolder();
            pendingServiceHookHolder.serviceKey = serviceKey;
            serviceHookHolderRegistry.put(serviceKey, pendingServiceHookHolder);
        }

        return pendingServiceHookHolder;
    }

    public static String genInterfaceKey(String targetPackage, String action) {
        return targetPackage + "###" + action;
    }

    public static void addBinderHook(String targetPackage, String action, String method, BindMethodHook bindMethodHook) {
        createOrGet(genInterfaceKey(targetPackage, action))
                .methodHookRegistry.put(method, bindMethodHook);
    }

    public static void addBinderHook(String targetPackage, String action, int method, BindMethodHook bindMethodHook) {
        createOrGet(genInterfaceKey(targetPackage, action))
                .methodCodeHookRegistry.put(method, bindMethodHook);
    }

    public static void addParcelHandler(BinderHookParcelHandler binderHookParcelHandler) {
        createOrGet(binderHookParcelHandler.name())
                .parcelHandleRegistry.put(binderHookParcelHandler.method(), binderHookParcelHandler);
    }

    private static void addDefault() {
        HUAWEI_OPENIDS_SERVICE_Handler.addHandlers();
    }

    static {
        setup();
        addDefault();
    }
}
