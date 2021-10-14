package com.virjar.ratel;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.util.Pair;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.envmock.PropertiesMockItem;
import com.virjar.ratel.envmock.binder.BinderHookManager;
import com.virjar.ratel.runtime.RatelEnvironment;
import com.virjar.ratel.runtime.RatelRuntime;
import com.virjar.ratel.utils.BuildCompat;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class RatelNative {

    private static boolean sFlag = false;
    private static boolean sEnabled = false;
    private static boolean hasNewConfig = false;

    public static String getRedirectedPath(String origPath) {
        try {
            return nativeGetRedirectedPath(origPath);
        } catch (Throwable e) {
            Log.e(Constants.TAG, "getRedirectedPath error", e);
        }
        return origPath;
    }

    public static String resverseRedirectedPath(String origPath) {
        try {
            return nativeReverseRedirectedPath(origPath);
        } catch (Throwable e) {
            Log.e(Constants.TAG, "resverseRedirectedPath error", e);
        }
        return origPath;
    }

    private static final Map<String, String> REDIRECT_SETS = new HashMap<>();


    public static void redirectDirectory(String origPath, String newPath) {
        if (!origPath.endsWith("/")) {
            origPath = origPath + "/";
        }
        if (!newPath.endsWith("/")) {
            newPath = newPath + "/";
        }
        hasNewConfig = true;
        REDIRECT_SETS.put(origPath, newPath);
    }

    public static void redirectFile(String origPath, String newPath) {
        if (origPath.endsWith("/")) {
            origPath = origPath.substring(0, origPath.length() - 1);
        }
        if (newPath.endsWith("/")) {
            newPath = newPath.substring(0, newPath.length() - 1);
        }
        hasNewConfig = true;
        REDIRECT_SETS.put(origPath, newPath);
    }

    public static void readOnlyFile(String path) {
        try {
            nativeIOReadOnly(path);
        } catch (Throwable e) {
            Log.e(Constants.TAG, "readOnlyFile error", e);
        }
    }

    public static void readOnly(String path) {
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        try {
            nativeIOReadOnly(path);
        } catch (Throwable e) {
            Log.e(Constants.TAG, "readOnly error", e);
        }
    }

    public static void whitelistFile(String path) {
        try {
            nativeIOWhitelist(path);
        } catch (Throwable e) {
            Log.e(Constants.TAG, "whitelistFile error", e);
        }
    }

    public static void whitelist(String path) {
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        if (RatelRuntime.isRatelDebugBuild) {
            Log.i(Constants.TAG, "add whiteList : " + path);
        }
        try {
            nativeIOWhitelist(path);
        } catch (Throwable e) {
            Log.e(Constants.TAG, "whitelist error", e);
        }
    }

    public static void forbid(String path, boolean file) {
        if (!file && !path.endsWith("/")) {
            path = path + "/";
        }
        try {
            if (RatelRuntime.isRatelDebugBuild) {
                Log.i(Constants.TAG, "add forbid path : " + path);
            }
            nativeIOForbid(path);
        } catch (Throwable e) {
            Log.e(Constants.TAG, "forbid error", e);
        }
    }

    public static void enableIORedirect() {

        if (sEnabled && !hasNewConfig) {
            return;
        }

        LinkedList<Pair<String, String>> REDIRECT_LISTS = new LinkedList<>();
        for (Map.Entry<String, String> entry : REDIRECT_SETS.entrySet()) {
            REDIRECT_LISTS.add(Pair.create(entry.getKey(), entry.getValue()));
        }

        Collections.sort(REDIRECT_LISTS, new Comparator<Pair<String, String>>() {
            @Override
            public int compare(Pair<String, String> o1, Pair<String, String> o2) {
                String a = o1.first;
                String b = o2.first;
                return compare(b.length(), a.length());
            }

            private int compare(int x, int y) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    return Integer.compare(x, y);
                }
                return (x < y) ? -1 : ((x == y) ? 0 : 1);
            }
        });
        if (sEnabled) {
            //zelda模式下，存在两次enableIORedirect调用
            clearIORedirectConfig();
        }
        for (Pair<String, String> pair : REDIRECT_LISTS) {
            try {
                if (RatelRuntime.isRatelDebugBuild) {
                    Log.i(Constants.TAG, "IORedirect from: " + pair.first + " to: " + pair.second);
                }
                nativeIORedirect(pair.first, pair.second);
            } catch (Throwable e) {
                Log.e(Constants.TAG, "nativeIORedirect REDIRECT_LISTS error", e);
            }
        }
        hasNewConfig = false;
        enableIORedirectInternal();
    }

    private static void enableIORedirectInternal() {
        if (sEnabled) {
            return;
        }
        try {
            nativeEnableIORedirect(Build.VERSION.SDK_INT,
                    BuildCompat.getPreviewSDKInt(),
                    RatelEnvironment.ratelNativeLib32Path().getAbsolutePath(),
                    RatelEnvironment.ratelNativeLib64Path().getAbsolutePath()
            );
            //zelda 模式下，需要先enable在userIdentifierSeed
            setupUserSeed(RatelEnvironment.userIdentifierSeed());
        } catch (Throwable e) {
            Log.e(Constants.TAG, "nativeEnableIORedirect error", e);
        }
        sEnabled = true;
    }


    public static boolean onKillProcess(int pid, int signal) {
        Log.e(Constants.TAG, "killProcess: pid = " + pid + ", signal = " + signal + ". stackTrace: ", new Throwable());
        return true;
    }


    private static final String getCanonicalPath(String path) {
        File file = new File(path);
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    public static void printJavaStackTrace(String message) {
        Log.i(Constants.TAG, "msg from jni: " + message, new Throwable());
    }


    private static native void nativeMark();

    private static native String nativeReverseRedirectedPath(String redirectedPath);

    private static native String nativeGetRedirectedPath(String origPath);

    private static native void nativeIORedirect(String origPath, String newPath);

    private static native void clearIORedirectConfig();

    private static native void nativeIOWhitelist(String path);

    private static native void nativeIOForbid(String path);


    private static native void nativeIOReadOnly(String path);

    private static native void nativeEnableIORedirect(int apiLevel, int previewApiLevel, String soPath, String soPath64);

    public static native void nativeTraceProcess(int sdkVersion);


    public static native void nativeAddMockSystemProperty(String key, String value);

    public static native PropertiesMockItem nativeQueryMockSystemProperty(String key);

    public static native boolean nativeInit(Context context, String packageName);

    public static native void setMemoryDiff(long memoryDiff);

    public static native void setupUserSeed(int userSeed);

    public static native void enableZeldaNative(String originPackageName, String nowPackageName);

    public static native void enableMacAddressFake();

    public static native void traceFilePath(String path);

    public static native void enableUnpackComponent(String dumpDir, boolean dumpMethod);

    public static native ByteBuffer methodDex(Member method);

    public static native void hideMaps();

    public static native void mockPathStat(String path, long lastModified);

    public static native void fakeMediaDrmId(Method javaMethod, byte[] fakeDrmId);

    public static native void hookBinder(Class<BinderHookManager> binderHookManagerClass);

    public static native long methodNativePtr(Method method);

    public static native ByteBuffer dumpMemory(long start, long size);
}
