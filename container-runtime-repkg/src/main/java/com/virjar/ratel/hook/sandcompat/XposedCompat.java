package com.virjar.ratel.hook.sandcompat;

import android.annotation.SuppressLint;
import android.os.Process;
import android.os.Trace;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.core.runtime.BuildConfig;
import com.virjar.ratel.hook.sandcompat.classloaders.ComposeClassLoader;
import com.virjar.ratel.hook.sandcompat.hookstub.DexMakerOptConfig;
import com.virjar.ratel.hook.sandcompat.hookstub.HookMethodEntity;
import com.virjar.ratel.hook.sandcompat.hookstub.HookStubManager;
import com.virjar.ratel.hook.sandcompat.methodgen.HookMaker;
import com.virjar.ratel.hook.sandcompat.methodgen.HookerDexMaker;
import com.virjar.ratel.hook.sandcompat.methodgen.HookerDexMakerNew;
import com.virjar.ratel.hook.sandcompat.utils.ApplicationUtils;
import com.virjar.ratel.hook.sandcompat.utils.FileUtils;
import com.virjar.ratel.hook.sandcompat.utils.ProcessUtils;
import com.virjar.ratel.runtime.RatelRuntime;
import com.virjar.ratel.sandhook.SandHook;
import com.virjar.ratel.sandhook.blacklist.HookBlackList;
import com.virjar.ratel.sandhook.wrapper.HookWrapper;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class XposedCompat {

    public static volatile String appDataDir;

    // TODO initialize these variables
    public static volatile File cacheDir;
    public static volatile ClassLoader classLoader;

    //try to use internal stub hooker & backup method to speed up hook
    public static volatile boolean useNewCallBackup = true;
    public static volatile boolean retryWhenCallOriginError = false;

    private static ClassLoader sandHookXposedClassLoader;


    public static File getCacheDir() {
        if (cacheDir == null) {
            String fixedAppDataDir = getDataPathPrefix() + getPackageName(appDataDir) + "/";
            cacheDir = new File(fixedAppDataDir, "/cache/sandhook/"
                    + ProcessUtils.getProcessName().replace(":", "_") + "/");
        }
        return cacheDir;
    }

    public static ClassLoader getClassLoader() {
        if (classLoader == null) {
            classLoader = getSandHookXposedClassLoader(ApplicationUtils.currentApplication().getClassLoader(), XposedCompat.class.getClassLoader());
        }
        return classLoader;
    }


    public static ClassLoader getSandHookXposedClassLoader(ClassLoader appOriginClassLoader, ClassLoader sandBoxHostClassLoader) {
        if (sandHookXposedClassLoader != null) {
            return sandHookXposedClassLoader;
        } else {
            sandHookXposedClassLoader = new ComposeClassLoader(sandBoxHostClassLoader, appOriginClassLoader);
            return sandHookXposedClassLoader;
        }
    }

    public static boolean clearCache() {
        try {
            FileUtils.delete(getCacheDir());
            getCacheDir().mkdirs();
            return true;
        } catch (Throwable throwable) {
            return false;
        }
    }

    public static void clearOatCache() {
        clearOatFile();
    }

    public static String getPackageName(String dataDir) {
        if (TextUtils.isEmpty(dataDir)) {
            return "";
        }
        int lastIndex = dataDir.lastIndexOf("/");
        if (lastIndex < 0) {
            return dataDir;
        }
        return dataDir.substring(lastIndex + 1);
    }

    // FIXME: Although multi-users is considered here, but compat mode doesn't support other users' apps on Oreo and later yet.
    @SuppressLint("SdCardPath")
    public static String getDataPathPrefix() {
        int userId = Process.myUid() / ProcessUtils.PER_USER_RANGE;
        String format = FileUtils.IS_USING_PROTECTED_STORAGE ? "/data/user_de/%d/" : "/data/user/%d/";
        return String.format(format, userId);
    }


    private static final Map<Member, Method> hookedInfo = new ConcurrentHashMap<>();
    private static HookMaker defaultHookMaker = XposedCompat.useNewCallBackup ? new HookerDexMakerNew() : new HookerDexMaker();
    private static final AtomicBoolean dexPathInited = new AtomicBoolean(false);
    private static File dexDir;

    public static Map<Member, HookMethodEntity> entityMap = new ConcurrentHashMap<>();

    public static boolean hooked(Member member) {
        return hookedInfo.containsKey(member) || entityMap.containsKey(member);
    }

    public static synchronized void hookMethod(Member hookMethod, RposedAdditionalHookInfo additionalHookInfo) {

        if (hookMethod.getDeclaringClass() == Log.class) {
            Log.e(RposedBridge.TAG, "some one hook Log!");
            return;
        }

        if (!checkMember(hookMethod)) {
            return;
        }

        if (hooked(hookMethod)) {
            Log.w(Constants.TAG, "already hook method:" + hookMethod.toString());
            return;
        }

        try {
            if (dexPathInited.compareAndSet(false, true)) {
                try {
                    String fixedAppDataDir = XposedCompat.getCacheDir().getAbsolutePath();
                    dexDir = new File(fixedAppDataDir, "/hookers/");
                    if (!dexDir.exists()) {
                        if (!dexDir.mkdirs()) {
                            throw new IllegalStateException("failed to create sandhook cache directory: " + dexDir.getAbsolutePath());
                        }
                    }
                } catch (Throwable throwable) {
                    Log.e(Constants.TAG, "error when init dex path", throwable);
                }
            }
            Trace.beginSection("SandXposed");
            long timeStart = System.currentTimeMillis();

            String hookPlan = null;
            HookWrapper.HookEntity frameworkHookEntity = DexMakerOptConfig.frameworkStub(hookMethod, additionalHookInfo);
            if (frameworkHookEntity != null) {
                //系统生成的插桩，ratel框架hook点需要的method资源，主要作用是减少内部stub的使用量
                SandHook.hook(frameworkHookEntity);
                hookedInfo.put(hookMethod, frameworkHookEntity.backup);
                hookPlan = "dexmaker-opt";
            } else if (!HookBlackList.canNotHookByStub(hookMethod) && !HookBlackList.canNotHookByBridge(hookMethod) && !BuildConfig.forceUserDexMakder) {
                //内部stub方案，理论上不影响效率，但是资源有限
                HookMethodEntity stub = HookStubManager.getHookMethodEntity(hookMethod, additionalHookInfo);
                if (stub != null) {
                    SandHook.hook(new HookWrapper.HookEntity(hookMethod, stub.hook, stub.backup, false));
                    entityMap.put(hookMethod, stub);
                    hookPlan = "internal stub.";
                }
            }

            if (hookPlan == null) {
                //dexmaker 方案，这效率最差
                HookMaker hookMaker;
                if (HookBlackList.canNotHookByBridge(hookMethod)) {
                    hookMaker = new HookerDexMaker();
                } else {
                    hookMaker = defaultHookMaker;
                }

                hookMaker.start(hookMethod, additionalHookInfo,
                        getComposedClassLoader(hookMethod.getDeclaringClass().getClassLoader())
                        , dexDir == null ? null : dexDir.getAbsolutePath());
                hookedInfo.put(hookMethod, hookMaker.getCallBackupMethod());
                hookPlan = "dex maker";
            }
            if (RatelRuntime.isRatelDebugBuild) {
                Log.i(Constants.TAG, "hook method <" + hookMethod.toString() + "> cost " + (System.currentTimeMillis() - timeStart) + " ms, by " + hookPlan);
            }
            Trace.endSection();


        } catch (Exception e) {
            Log.e(Constants.TAG, "error occur when hook method <" + hookMethod.toString() + ">", e);
        }
    }

    private static Map<ClassLoader, ComposeClassLoader> dexMakerClassLoaders = new ConcurrentHashMap<>();

    public static ComposeClassLoader getComposedClassLoader(ClassLoader classLoader) {
        if (classLoader instanceof ComposeClassLoader) {
            return (ComposeClassLoader) classLoader;
        }
        ComposeClassLoader composeClassLoader = dexMakerClassLoaders.get(classLoader);
        if (composeClassLoader != null) {
            return composeClassLoader;
        }

        composeClassLoader = new ComposeClassLoader(XposedCompat.class.getClassLoader(), classLoader);
        dexMakerClassLoaders.put(classLoader, composeClassLoader);
        return composeClassLoader;
    }

    public static void clearOatFile() {
        String fixedAppDataDir = XposedCompat.getCacheDir().getAbsolutePath();
        File dexOatDir = new File(fixedAppDataDir, "/hookers/oat/");
        if (!dexOatDir.exists())
            return;
        try {
            FileUtils.delete(dexOatDir);
            dexOatDir.mkdirs();
        } catch (Throwable throwable) {
        }
    }

    private static boolean checkMember(Member member) {

        if (member instanceof Method) {
            return true;
        } else if (member instanceof Constructor<?>) {
            return true;
        } else if (member.getDeclaringClass().isInterface()) {
            Log.e(Constants.TAG, "Cannot hook interfaces: " + member.toString());
            return false;
        } else if (Modifier.isAbstract(member.getModifiers())) {
            Log.e(Constants.TAG, "Cannot hook abstract methods: " + member.toString());
            return false;
        } else {
            Log.e(Constants.TAG, "Only methods and constructors can be hooked: " + member.toString());
            return false;
        }
    }


//    public static void init() {
//        SandHookConfig.libLoader = () -> {
//            //do it in loadDexAndInit
//            if (SandHookConfig.SDK_INT >= Build.VERSION_CODES.O) {
//                SandHook.setHookMode(HookMode.REPLACE);
//            }
//        };
//        SandHookConfig.DEBUG = true;
//        //in zygote disable compile
//    }


    private static final Object[] EMPTY_ARRAY = new Object[0];

    // built-in handlers
    public static final Map<Member, RposedBridge.CopyOnWriteSortedSet<RC_MethodHook>> sHookedMethodCallbacks = new HashMap<>();


    public static RC_MethodHook.Unhook hookMethod(Member hookMethod, RC_MethodHook callback) {
        if (!(hookMethod instanceof Method) && !(hookMethod instanceof Constructor<?>)) {
            throw new IllegalArgumentException("Only methods and constructors can be hooked: " + hookMethod.toString());
        } else if (hookMethod.getDeclaringClass().isInterface()) {
            throw new IllegalArgumentException("Cannot hook interfaces: " + hookMethod.toString());
        } else if (Modifier.isAbstract(hookMethod.getModifiers())) {
            throw new IllegalArgumentException("Cannot hook abstract methods: " + hookMethod.toString());
        }

        if (callback == null) {
            throw new IllegalArgumentException("callback should not be null!");
        }

        boolean newMethod = false;
        RposedBridge.CopyOnWriteSortedSet<RC_MethodHook> callbacks;
        synchronized (sHookedMethodCallbacks) {
            callbacks = sHookedMethodCallbacks.get(hookMethod);
            if (callbacks == null) {
                callbacks = new RposedBridge.CopyOnWriteSortedSet<>();
                sHookedMethodCallbacks.put(hookMethod, callbacks);
                newMethod = true;
            }
        }
        callbacks.add(callback);

        if (callbacks.getSnapshot().length > 10) {
            //有人一直在一个函数上面重复hook，导致系统崩溃，理论上一个函数挂在的hook回调就一两个，太多了本身逻辑都讲不清楚了
            Log.w(Constants.TAG, "to many hook callback for method: " + hookMethod + ", check if add duplicate method hook entry point");
        }

        if (newMethod) {

            Class<?>[] parameterTypes;
            Class<?> returnType;
            // ed: only support art
            parameterTypes = null;
            returnType = null;

            RposedAdditionalHookInfo additionalInfo = new RposedAdditionalHookInfo(callbacks, parameterTypes, returnType);
            hookMethod(hookMethod, additionalInfo);
        }

        return callback.new Unhook(hookMethod);
    }


    public static Object invokeOriginalMethod(Member method, Object thisObject, Object[] args)
            throws Throwable {
        if (args == null) {
            args = EMPTY_ARRAY;
        }
        return SandHook.callOriginMethod(method, thisObject, args);
    }

}
