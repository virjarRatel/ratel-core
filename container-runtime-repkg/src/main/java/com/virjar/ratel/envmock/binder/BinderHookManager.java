package com.virjar.ratel.envmock.binder;

import android.app.AppOpsManager;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.StrictMode;
import android.support.annotation.Keep;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.RatelNative;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.envmock.binder.handlers.IPhoneSubInfoManagerHandler;
import com.virjar.ratel.envmock.binder.handlers.ISUBManagerHandler;
import com.virjar.ratel.envmock.binder.handlers.PackageManagerHandler;
import com.virjar.ratel.envmock.binder.handlers.PhoneManagerHandler;
import com.virjar.ratel.envmock.binder.handlers.WifiManagerHandler;
import com.virjar.ratel.runtime.RatelRuntime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BinderHookManager {
    public static int EX_HAS_NOTED_APPOPS_REPLY_HEADER = -127; // special; see below
    private static int EX_HAS_STRICTMODE_REPLY_HEADER = -128;  // special; see below

    static {
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                EX_HAS_NOTED_APPOPS_REPLY_HEADER = mirror.android.os.Parcel.EX_HAS_NOTED_APPOPS_REPLY_HEADER.get(null);
                EX_HAS_STRICTMODE_REPLY_HEADER = mirror.android.os.Parcel.EX_HAS_STRICTMODE_REPLY_HEADER.get(null);
            }else{
                EX_HAS_STRICTMODE_REPLY_HEADER = mirror.android.os.Parcel.EX_HAS_REPLY_HEADER.get(null);
            }
        } catch (Throwable throwable) {
            //ignore
        }
    }


    private static Map<String, ServiceHookRegistry> hookRegistry = new HashMap<>();
    private static Class<?> serviceManagerClass;

    private static Map<IBinder, String> serviceBinderMaps = new HashMap<>();
    private static ConcurrentHashMap<IBinder, String> binderDescriptorCache = new ConcurrentHashMap<>();
    private static Map<String, String> descriptorServiceMap = new HashMap<>();

    private static void init() {
        registerBinderHook();
        serviceManagerClass = RposedHelpers.findClass("android.os.ServiceManager", ClassLoader.getSystemClassLoader());
    }

    private static String getBinderDescriptor(IBinder iBinder) {
        if (binderDescriptorCache.containsKey(iBinder)) {
            return binderDescriptorCache.get(iBinder);
        }
        String des = getBinderDescriptorWithoutCache(iBinder);
        binderDescriptorCache.put(iBinder, des);
        return des;
    }

    private static String getBinderDescriptorWithoutCache(IBinder iBinder) {

        try {
            return iBinder.getInterfaceDescriptor();
        } catch (RemoteException error) {
            //
            Log.w("weijia", "query descriptor error", error);
        }
        Parcel getDescriptorData = Parcel.obtain();
        Parcel getDescriptorReply = Parcel.obtain();
        try {
            iBinder.transact(IBinder.INTERFACE_TRANSACTION, getDescriptorData, getDescriptorReply, 0);
            return getDescriptorReply.readString();
        } catch (Throwable error) {
            Log.w("weijia", "query descriptor error", error);
            //ignore
        } finally {
            getDescriptorData.recycle();
            getDescriptorReply.recycle();
        }
        return null;
    }


    @Keep
    @SuppressWarnings("unused")
    public static boolean transactNativeHookMethod(IBinder iBinder, int code, Parcel data, Parcel reply,
                                                   int flags) {
        if (code == IBinder.INTERFACE_TRANSACTION) {
            return transactNativeOrigin(iBinder, code, data, reply, flags);
        }
        //Log.i("weijia", "transactNativeHookMethod: " + iBinder);

        String service = serviceBinderMaps.get(iBinder);
        if (service == null) {
            String binderDescriptor = getBinderDescriptor(iBinder);
            if (binderDescriptor != null) {
                service = descriptorServiceMap.get(binderDescriptor);
            }
        }

        if (service == null) {
            // not hook this services
            return transactNativeOrigin(iBinder, code, data, reply, flags);
        }

        ServiceHookRegistry serviceHookRegistry = hookRegistry.get(service);
        if (serviceHookRegistry == null) {
            Log.e(RatelToolKit.TAG, "no ServiceHookRegistry found for service: " + service + " but there is a hook binder register");
            return transactNativeOrigin(iBinder, code, data, reply, flags);
        }


        return serviceHookRegistry.handleHook(code, data, reply, flags);


    }


    @Keep
    @SuppressWarnings({"unused"})
    public static native boolean transactNativeOrigin(IBinder thiz, int code, Parcel data, Parcel reply,
                                                      int flags);

    private static void registerBinderHook() {
        RatelNative.hookBinder(BinderHookManager.class);
    }

    private static void registerDefaultParcelHandler() {
        IPhoneSubInfoManagerHandler.addHandlers();
        ISUBManagerHandler.addHandlers();
        PackageManagerHandler.addHandlers();
        PhoneManagerHandler.addHandlers();
        WifiManagerHandler.addHandlers();
    }


    private static class BinderMethodHookRegistry {
        private String method;
        private String service;
        private BinderHookParcelHandler binderHookParcelHandler;
        private List<BindMethodHook> hookRegistry = new ArrayList<>();
        private boolean status = false;

        public BinderMethodHookRegistry(String service, String method) {
            this.method = method;
            this.service = service;
        }

        public void addHook(BindMethodHook bindMethodHook) {
            if (TextUtils.isEmpty(method)) {
                Log.w(Constants.TAG, "no binderHookParcelHandler defined for " + service + "." + method);
            }
            hookRegistry.add(bindMethodHook);
        }
    }

    private static class ServiceHookRegistry {
        private String service;
        private Map<String, BinderMethodHookRegistry> methodRegistry = new HashMap<>();
        private Map<Integer, String> ipcMethodCodeRegistry = new HashMap<>();
        private String descriptor;
        private Class<?> ipcClass;
        private IBinder mRmote;

        private boolean status = false;

        private void init(IBinder b) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                ClassLoader classLoader = ClassLoader.getSystemClassLoader();
                if (b == null) {
                    //系统service 没有binder
                    b = (IBinder) RposedHelpers.callStaticMethod(serviceManagerClass, "getService", service);
                } else {
                    classLoader = b.getClass().getClassLoader();
                }
                /**
                 * public static final int INTERFACE_TRANSACTION
                 * IBinder事务协议码:向事务接收端询问他的规范接口描述符。
                 * 常量值: 1598968902 (0x5f4e5446)
                 */
                b.transact(IBinder.INTERFACE_TRANSACTION, obtain, obtain2, 0);
                descriptor = obtain2.readString();
                if (descriptor == null) {
                    Log.e(Constants.TAG, "can not query descriptor", new Throwable("can not query descriptor"));
                    return;
                }
                IInterface iInterface = b.queryLocalInterface(descriptor);
                if (iInterface == null) {
                    mRmote = b;
                } else {
                    mRmote = iInterface.asBinder();
                }

                serviceBinderMaps.put(mRmote, service);
                descriptorServiceMap.put(descriptor, service);
                //com.uodis.opendevice.aidl.OpenDeviceIdentifierService 对应接口和class不一致
                ipcClass = RposedHelpers.findClassIfExists(descriptor + "$Stub", classLoader);
                status = true;
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            } finally {
                obtain.recycle();
                obtain2.recycle();
            }
        }

        public ServiceHookRegistry(String service, IBinder iBinder) {
            this.service = service;
            init(iBinder);
        }

        public boolean ok() {
            return status;
        }

        public boolean handleHook(int code, Parcel data, Parcel reply, int flags) {
            String theHookIPCMethod = ipcMethodCodeRegistry.get(code);
            if (theHookIPCMethod == null) {
                return BinderHookManager.transactNativeOrigin(mRmote, code, data, reply, flags);
            }
            BinderMethodHookRegistry binderMethodHookRegistry = methodRegistry.get(theHookIPCMethod);
            if (binderMethodHookRegistry == null) {
                Log.e(RatelToolKit.TAG, "no BinderMethodHook handler register,but the ipcCode registed!!");
                return BinderHookManager.transactNativeOrigin(mRmote, code, data, reply, flags);
            }

            if (!binderMethodHookRegistry.status) {
                //方法不存在
                return BinderHookManager.transactNativeOrigin(mRmote, code, data, reply, flags);
            }

            if (binderMethodHookRegistry.hookRegistry.isEmpty()) {
                //注册了ipc解析规则，但是没有对应hook的handler，此时直接路由到原始逻辑
                return BinderHookManager.transactNativeOrigin(mRmote, code, data, reply, flags);
            }

            BinderHookParcelHandler binderHookParcelHandler = binderMethodHookRegistry.binderHookParcelHandler;


            data.setDataPosition(0);
            Object[] objects = binderHookParcelHandler.parseInvokeParam(descriptor, data);

            for (BindMethodHook bindMethodHook : binderMethodHookRegistry.hookRegistry) {
                try {
                    bindMethodHook.beforeIpcCall(objects);
                } catch (Throwable t) {
                    Log.e(Constants.TAG, "binder hook handler error", t);
                }
            }
            Parcel newData = Parcel.obtain();
            Parcel newReply = Parcel.obtain();
            boolean result;
            try {
                binderHookParcelHandler.writeParamsToParcel(descriptor, newData, objects);
                result = transactNativeOrigin(mRmote, code, newData, newReply, flags);
                // handle exception before parse invoke result
                BinderInvokeException invokeException = readExceptionFromParcel(newReply);
                if (invokeException != null) {
                    Log.i(Constants.TAG, "read exception from newReply:" + invokeException + ",result:" + result);
                    writeExceptionToParcel(reply, invokeException);
                } else {
                    Log.i(Constants.TAG, "hook binder success:" + descriptor);
                    Object invokeResult = binderHookParcelHandler.parseInvokeResult(newReply);
                    Log.i(Constants.TAG, "parse binder InvokeResult:" + invokeResult);
                    for (BindMethodHook bindMethodHook : binderMethodHookRegistry.hookRegistry) {
                        try {
                            invokeResult = bindMethodHook.afterIpcCall(objects, invokeResult);
                        } catch (Throwable t) {
                            Log.e(Constants.TAG, "binder hook handler error", t);
                        }
                    }
                    binderHookParcelHandler.writeNewResultToParcel(reply, invokeResult);
                }
                reply.setDataPosition(0);
            } finally {
                newData.recycle();
                newReply.recycle();
            }
            return result;
        }

        private synchronized BinderMethodHookRegistry createOrGetMethodRegistry(String method, int transactCode) {
            BinderMethodHookRegistry binderMethodHookRegistry = methodRegistry.get(method);
            if (binderMethodHookRegistry == null) {
                try {
                    binderMethodHookRegistry = new BinderMethodHookRegistry(service, method);
                    methodRegistry.put(method, binderMethodHookRegistry);
                    if (transactCode > 0) {
                        ipcMethodCodeRegistry.put(transactCode, method);
                    } else {
                        ipcMethodCodeRegistry.put(RposedHelpers.getStaticIntField(ipcClass, method), method);
                    }
                    binderMethodHookRegistry.status = true;
                } catch (Throwable throwable) {
                    Log.w(Constants.TAG, "can not find ipcMethod: " + service + "." + method, throwable);
                }
            }
            return binderMethodHookRegistry;
        }

        public void addHookHandler(String method, int transactCode, BindMethodHook bindMethodHook) {
            createOrGetMethodRegistry(method, transactCode)
                    .addHook(bindMethodHook);
        }

        public void addParcelHandler(String method, int transactCode, BinderHookParcelHandler binderHookParcelHandler) {
            createOrGetMethodRegistry(method, transactCode).binderHookParcelHandler = binderHookParcelHandler;
        }
    }


    private static synchronized ServiceHookRegistry createOrGetServiceHookRegistry(String service, IBinder iBinder) {
        try {
            ServiceHookRegistry serviceHookRegistry = hookRegistry.get(service);
            if (serviceHookRegistry != null) {
                return serviceHookRegistry;
            }
            serviceHookRegistry = new ServiceHookRegistry(service, iBinder);
            if (!serviceHookRegistry.ok()) {
                Log.w(Constants.TAG, "error to add  ServiceHookRegistry:" + service);
                return null;
            }
            hookRegistry.put(service, serviceHookRegistry);

            return serviceHookRegistry;
        } catch (Throwable throwable) {
            if (RatelRuntime.isRatelDebugBuild) {
                Log.w(Constants.TAG, "create ServiceHookRegistry failed", throwable);
            }
            return null;
        }
    }

    public static void addBinderHook(String service, String method, BindMethodHook bindMethodHook) {
        addBinderHook(service, method, -1, bindMethodHook);
    }

    public static void addBinderHook(String service, String method, int transactCode, BindMethodHook bindMethodHook) {
        ServiceHookRegistry serviceHookRegistry = createOrGetServiceHookRegistry(service, null);
        if (serviceHookRegistry == null) {
            return;
        }
        if (!TextUtils.isEmpty(method) && !method.startsWith("TRANSACTION_")) {
            method = "TRANSACTION_" + method;
        }
        serviceHookRegistry.addHookHandler(method, transactCode, bindMethodHook);
    }

    public static void addParcelHandler(BinderHookParcelHandler binderHookParcelHandler) {
        addParcelHandler(binderHookParcelHandler, null);
    }

    public static void addParcelHandler(BinderHookParcelHandler binderHookParcelHandler, IBinder iBinder) {
        ServiceHookRegistry serviceHookRegistry = createOrGetServiceHookRegistry(binderHookParcelHandler.name(), iBinder);
        if (serviceHookRegistry == null) {
            return;
        }

        serviceHookRegistry
                .addParcelHandler(
                        binderHookParcelHandler.method(),
                        binderHookParcelHandler.transactCode(),
                        binderHookParcelHandler
                );
    }

    @SuppressWarnings("unchecked")
    public static <T> Parcelable.Creator<T> getCreator(String aidlClass) {
        Class<?> aClass = RposedHelpers.findClassIfExists(aidlClass, ClassLoader.getSystemClassLoader());
        if (aClass == null) {
            Log.e(Constants.TAG, "can not find creator class: " + aidlClass);
            throw new RuntimeException("can not find creator class: " + aidlClass);
        }
        return (Parcelable.Creator<T>) RposedHelpers.getStaticObjectField(aClass, "CREATOR");
    }

    //静态代码块放最后,否则其他常量没有创建
    static {
        init();
//        if (RatelRuntime.processName.equals(RatelRuntime.nowPackageName)) {
//            Debug.waitForDebugger();
//        }
        registerDefaultParcelHandler();
    }

    public static void consumeEnforceInterface(Parcel parcel) {
        //https://www.androidos.net.cn/android/9.0.0_r8/xref//frameworks/native/libs/binder/Parcel.cpp
        parcel.readInt();//strictPolicy
        if (Build.VERSION.SDK_INT == 29) {
            //https://www.androidos.net.cn/android/10.0.0_r6/xref/frameworks/native/libs/binder/Parcel.cpp
            parcel.readInt();//workSource
        } else if (Build.VERSION.SDK_INT >= 30) {
            parcel.readInt();//workSource
            parcel.readInt();//vendorHeader
            // todo 理论上vendorHeader如果不等于kHeader后面可能会直接返回
        }
        parcel.readString();//interface
    }

    /**
     * 反序列化parcel中的异常
     *
     * @param parcel IPC参数
     * @see Parcel#readException()
     */
    public static BinderInvokeException readExceptionFromParcel(Parcel parcel) {
        // read exception code
        int code = parcel.readInt();
        if (code == 0) {
            // no exception
            return null;
        }
        BinderInvokeException invokeException = new BinderInvokeException();
        if (Build.VERSION.SDK_INT >= 30) {
            if (code == EX_HAS_NOTED_APPOPS_REPLY_HEADER) {
                RposedHelpers.callStaticMethod(AppOpsManager.class, "readAndLogNotedAppops", parcel);
                code = parcel.readInt();
            }
        }
        invokeException.exceptionCode = code;
        if (code == EX_HAS_STRICTMODE_REPLY_HEADER) {
            // StrictMode stacks
            int headerSize = parcel.readInt();
            if (headerSize != 0) {
                // just parse out the StrictMode stuff.
                RposedHelpers.callStaticMethod(StrictMode.class, "readAndHandleBinderCallViolations", parcel);
            }
        }
        // read exception message
        invokeException.exceptionMessage = parcel.readString();
        // read remote stack trace
        invokeException.remoteStackTraceSize = parcel.readInt();
        if (invokeException.remoteStackTraceSize <= 0) {
            // no remote stack
            return invokeException;
        }
        invokeException.remoteStackTraces = parcel.readString();
        if (code == RposedHelpers.getStaticIntField(Parcel.class, "EX_SERVICE_SPECIFIC")) {
            invokeException.specificServiceErrorCode = parcel.readInt();
        }
        if (code == RposedHelpers.getStaticIntField(Parcel.class, "EX_PARCELABLE")) {
            invokeException.exParcelableSize = parcel.readInt();
            invokeException.exParcelable = parcel.readParcelable(Parcel.class.getClassLoader());
        }
        return invokeException;
    }


    /**
     * @param parcel    待写入Parcel
     * @param exception 反序列化后转化的自定义异常 {@link this#readExceptionFromParcel(Parcel)}
     * @see Parcel#writeException(Exception)
     */
    public static void writeExceptionToParcel(Parcel parcel, BinderInvokeException exception) {
        if (exception == null || exception.exceptionCode == 0) {
            return;
        }
        parcel.writeInt(exception.exceptionCode);
        parcel.writeString(exception.exceptionMessage);
        if (exception.remoteStackTraceSize > 0) {
            parcel.writeInt(exception.remoteStackTraceSize);
            parcel.writeString(exception.remoteStackTraces);
        } else {
            parcel.writeInt(0);
        }
        if (exception.exceptionCode == RposedHelpers.getStaticIntField(Parcel.class, "EX_SERVICE_SPECIFIC")) {
            parcel.writeInt(exception.specificServiceErrorCode);
        } else if (exception.exceptionCode == RposedHelpers.getStaticIntField(Parcel.class, "EX_PARCELABLE")) {
            parcel.writeInt(exception.exParcelableSize);
            parcel.writeParcelable(exception.exParcelable, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        }
    }
}
