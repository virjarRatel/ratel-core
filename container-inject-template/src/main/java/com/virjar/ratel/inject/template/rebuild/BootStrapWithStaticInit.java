package com.virjar.ratel.inject.template.rebuild;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.virjar.ratel.inject.template.hidebypass.HiddenApiBypass;

import java.lang.reflect.Method;

public class BootStrapWithStaticInit {
    public static void startup() {
        try {
//            if (isPie()) {
//                passApiCheck();
//            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                HiddenApiBypass.addHiddenApiExemptions("");
            }
            java.lang.Class activityThreadClass = java.lang.Class.forName("android.app.ActivityThread");
            java.lang.reflect.Method declaredMethod = activityThreadClass.getDeclaredMethod("currentActivityThread", new java.lang.Class[0]);
            declaredMethod.setAccessible(true);
            java.lang.Object activityThread = declaredMethod.invoke(null, new java.lang.Object[0]);
            java.lang.reflect.Field declaredField = activityThreadClass.getDeclaredField("mBoundApplication");
            declaredField.setAccessible(true);
            java.lang.Object loadApk = declaredField.get(activityThread);
            java.lang.reflect.Field declaredField2 = loadApk.getClass().getDeclaredField("info");
            declaredField2.setAccessible(true);
            loadApk = declaredField2.get(loadApk);

            java.lang.reflect.Method declaredMethod2 = java.lang.Class.forName("android.app.ContextImpl").getDeclaredMethod("createAppContext", new java.lang.Class[]{activityThreadClass, loadApk.getClass()});
            declaredMethod2.setAccessible(true);
            Context context = (android.content.Context) declaredMethod2.invoke(null, new java.lang.Object[]{activityThread, loadApk});
            BootStrap.startUp(context, context, false);

        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private static Method addWhiteListMethod;

    private static Object vmRuntime;

//    static {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            try {
//                //高版本才需要执行这个
//                Method getMethodMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
//                Method forNameMethod = Class.class.getDeclaredMethod("forName", String.class);
//                Class vmRuntimeClass = (Class) forNameMethod.invoke(null, "dalvik.system.VMRuntime");
//                addWhiteListMethod = (Method) getMethodMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});
//                Method getVMRuntimeMethod = (Method) getMethodMethod.invoke(vmRuntimeClass, "getRuntime", null);
//                vmRuntime = getVMRuntimeMethod.invoke(null);
//            } catch (Exception e) {
//                Log.e("ReflectionUtils", "error get methods", e);
//            }
//        }
//    }

    private static void passApiCheck() {
        try {
            if (isQ() && isEMUI()) {
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
            Log.w("RATEL", "pass Hidden API enforcement policy failed", throwable);
        }
    }

    public static boolean isEMUI() {
        if (Build.DISPLAY.toUpperCase().startsWith("EMUI")) {
            return true;
        }
        try {
            String emuiVersion = (String) Class.forName("android.os.SystemProperties").getMethod("get", String.class).invoke(null, "ro.build.version.emui");
            if (emuiVersion == null || !emuiVersion.contains("EmotionUI")) {
                return false;
            }
        } catch (Throwable throwable) {
            //ignore
        }
        return true;
    }


    public static boolean isQ() {
        return Build.VERSION.SDK_INT > 28 || (Build.VERSION.SDK_INT == 28 && getPreviewSDKInt() > 0);
    }

    private static boolean isPie() {
        return Build.VERSION.SDK_INT > 27 || (Build.VERSION.SDK_INT == 27 && getPreviewSDKInt() > 0);
    }

    private static int getPreviewSDKInt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                return Build.VERSION.PREVIEW_SDK_INT;
            } catch (Throwable e) {
                // ignore
            }
        }
        return 0;
    }


    //methidSigs like Lcom/swift/sandhook/utils/ReflectionUtils;->vmRuntime:java/lang/Object; (from hidden policy list)
    private static void addReflectionWhiteList(String... memberSigs) throws Throwable {
        addWhiteListMethod.invoke(vmRuntime, new Object[]{memberSigs});
    }
}
