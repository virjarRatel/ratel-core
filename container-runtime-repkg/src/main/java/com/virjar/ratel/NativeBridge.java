package com.virjar.ratel;

import android.content.Context;
import android.support.annotation.Keep;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.core.runtime.BuildConfig;
import com.virjar.ratel.envmock.PropertiesMockItem;
import com.virjar.ratel.envmock.SystemPropertiesFake;
import com.virjar.ratel.runtime.RatelConfig;
import com.virjar.ratel.runtime.RatelEnvironment;
import com.virjar.ratel.runtime.RatelRuntime;
import com.virjar.ratel.runtime.SelfExplosion;
import com.virjar.ratel.sandhook.ArtMethodSizeTest;
import com.virjar.ratel.sandhook.ClassNeverCall;
import com.virjar.ratel.sandhook.PendingHookHandler;
import com.virjar.ratel.sandhook.SandHook;
import com.virjar.ratel.sandhook.SandHookConfig;
import com.virjar.ratel.sandhook.SandHookMethodResolver;
import com.virjar.ratel.utils.IntegerUtil;
import com.virjar.ratel.utils.Types;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import external.org.apache.commons.lang3.BooleanUtils;

/**
 * 给native调用的桥接器，单独抽离做混淆白名单。进而减少业务代码混淆
 */
@Keep
public class NativeBridge {

    static {
        System.loadLibrary(BuildConfig.RATEL_NATIVE_LIB_NAME);
    }

    public static final long s = RatelRuntime.runtimeStartupTimestamp;

    public static File nativeCacheDir(Context context) {
        //return FileManager.nativeCacheDir(context);
        return RatelEnvironment.nativeCacheDir();
    }

    public static File originApkDir(Context context) {
        return RatelEnvironment.originApkDir();
    }

    public static File APKOptimizedDirectory(Context context) {
        return RatelEnvironment.APKOptimizedDirectory();
    }

    public static String getConfig(String key) {
        return RatelConfig.getConfig(key);
    }


    private static native String bridgeInitNative(Map<String, String> bindData, Context context, String originPkgName);

    public static String onGetSystemProperties(String key, String value) {
        return SystemPropertiesFake.onGetSystemProperties(key, value);
    }


    public static void nativeBridgeInit() {
        Map<String, String> bindData = new HashMap<>();
        bindData.put("RATEL_NATIVE", Types.getNativeClassName(RatelNative.class));
        bindData.put("SAND_HOOK", Types.getNativeClassName(SandHook.class));
        bindData.put("SAND_CLASS_NEVER_CALL", Types.getNativeClassName(ClassNeverCall.class));
        bindData.put("SAND_METHOD_SIZE_TEST", Types.getNativeClassName(ArtMethodSizeTest.class));
        bindData.put("SAND_METHOD_RESOLVER", Types.getNativeClassName(SandHookMethodResolver.class));
        bindData.put("SAND_HOOK_CONFIG", Types.getNativeClassName(SandHookConfig.class));
        bindData.put("RAND_HOOK_PENDING_HOOK", Types.getNativeClassName(PendingHookHandler.class));
        bindData.put("RATEL_PROPERTIES_MOCK_ITEM", Types.getNativeClassName(PropertiesMockItem.class));
        bindData.put("RATEL_A", Types.getNativeClassName(SelfExplosion.class));

        String errorMessage = bridgeInitNative(bindData, RatelRuntime.getOriginContext(), RatelRuntime.originPackageName);
        if (errorMessage == null) {
            return;
        }
        throw new IllegalStateException("ratel native engine init failed: " + errorMessage);
    }


    public static String ratelResourceDir() {
        try {
            return RatelEnvironment.ratelResourceDir().getCanonicalPath();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Random newRandom(String hexSeed) {
        int key = IntegerUtil.parseUnsignedInt(hexSeed, 16);
        return new Random(key + 32942);
    }

    public static void showStackTrace() {
        Log.i(Constants.TAG, "java stack: ", new Throwable());
    }
}
