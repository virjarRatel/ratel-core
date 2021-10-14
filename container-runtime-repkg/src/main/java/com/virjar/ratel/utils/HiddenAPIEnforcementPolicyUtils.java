package com.virjar.ratel.utils;

import android.os.Build;
import android.util.Log;


import com.virjar.ratel.allcommon.Constants;

import java.lang.reflect.Method;

//
// Created by Swift Gan on 2019/3/15.
//


//bypass hidden api on Android 9 - 10
public class HiddenAPIEnforcementPolicyUtils {

    private static Method addWhiteListMethod;

    private static Object vmRuntime;

    private static boolean hasInit = false;

    private static void init() {
        try {
            Method getMethodMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
            Method forNameMethod = Class.class.getDeclaredMethod("forName", String.class);
            Class vmRuntimeClass = (Class) forNameMethod.invoke(null, "dalvik.system.VMRuntime");
            addWhiteListMethod = (Method) getMethodMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});
            Method getVMRuntimeMethod = (Method) getMethodMethod.invoke(vmRuntimeClass, "getRuntime", null);
            vmRuntime = getVMRuntimeMethod.invoke(null);
            hasInit = true;
        } catch (Exception e) {
            Log.e("ReflectionUtils", "error get methods", e);
        }
    }


    public static void passApiCheck() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return;
        }
        try {
            if (BuildCompat.isR()) {
                HiddenApiBypass.setHiddenApiExemptions("Landroid/",
                        "Lcom/android/",
                        "Ljava/lang/",
                        // android 11 java.net.Inet6Address.Inet6AddressHolder.init(byte[], java.net.NetworkInterface)
                        "Ljava/net/",
                        "Ldalvik/system/",
                        "Llibcore/io/",
                        "Lsun/misc/",
                        "Lhuawei/");
                return;
            }
            if (!hasInit) {
                init();
            }
            if (BuildCompat.isQ() && BuildCompat.isEMUI()) {
                addReflectionWhiteList("Landroid/",
                        "Lcom/android/",
                        "Ljava/lang/",
                        "Ldalvik/system/",
                        "Llibcore/io/",
                        "Lsun/misc/",
                        "Lhuawei/"
                );
            } else {
                addReflectionWhiteList("Landroid/",
                        "Lcom/android/",
                        "Ljava/lang/",
                        "Ldalvik/system/",
                        "Llibcore/io/",
                        "Lsun/misc/"
                );
            }
        } catch (Throwable throwable) {
            Log.w(Constants.TAG, "pass Hidden API enforcement policy failed", throwable);
        }
    }

    public static void reverseApiCheck() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return;
        }
        if (!hasInit) {
            init();
        }
        try {
            //只保留java.lang.* 避免apk通过hidden api policy进行检测
            addReflectionWhiteList("Ljava/lang/");
        } catch (Throwable throwable) {
            Log.w(Constants.TAG, "reverseApiCheck failed", throwable);
        }
    }

    //methidSigs like Lcom/swift/sandhook/utils/ReflectionUtils;->vmRuntime:java/lang/Object; (from hidden policy list)
    private static void addReflectionWhiteList(String... memberSigs) throws Throwable {
        addWhiteListMethod.invoke(vmRuntime, new Object[]{memberSigs});
    }
}
