package com.virjar.ratel.runtime;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.Keep;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.NativeBridge;
import com.virjar.ratel.RatelNative;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.RatalStartUpCallback;
import com.virjar.ratel.api.RatelEngine;
import com.virjar.ratel.api.RatelEngineUpgradeEvent;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.SDK_VERSION_CODES;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.core.runtime.BuildConfig;
import com.virjar.ratel.envmock.EnvMockController;
import com.virjar.ratel.hook.sandcompat.XposedCompat;
import com.virjar.ratel.nativehide.NativeHide;
import com.virjar.ratel.runtime.engines.EngineAppendDex;
import com.virjar.ratel.runtime.engines.EngineKratos;
import com.virjar.ratel.runtime.engines.EngineRebuildDex;
import com.virjar.ratel.runtime.engines.EngineShell;
import com.virjar.ratel.runtime.engines.EngineZelda;
import com.virjar.ratel.runtime.fixer.AssertFixer;
import com.virjar.ratel.runtime.fixer.SignatureFixer;
import com.virjar.ratel.runtime.ipc.IPCControlHandler;
import com.virjar.ratel.sandhook.SandHook;
import com.virjar.ratel.utils.HiddenAPIEnforcementPolicyUtils;
import com.virjar.ratel.utils.SignatureKill;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import external.org.apache.commons.io.FileUtils;
import external.org.apache.commons.io.IOUtils;
import mirror.android.app.ActivityThread;
import mirror.dalvik.system.VMRuntime;

public class RatelRuntime {

    public static Context entryContext;
    @SuppressLint("StaticFieldLeak")
    public static Context originContext;

    public static Context ratelEnvContext;

    public static String processName;

    //zelda引擎专有，标记当前真正的pkg
    public static String nowPackageName;
    //zelda引擎专有
    public static String sufferKey;

    public static String originApplicationClassName = null;
    public static String originPackageName = null;


    public static String originLaunchActivity;
    /**
     * ActivityThread instance
     */
    public static android.app.ActivityThread mainThread;

    public static ApplicationInfo originApplicationInfo;

    //此乃手动混淆，我们在native层会访问h对象，
    // hostPkgInfo
    private static PackageInfo h;


    public static boolean isMainProcess;


    private static boolean startCompleted = false;

    public static long runtimeStartupTimestamp = System.currentTimeMillis();

    public static boolean isStartCompleted() {
        return startCompleted;
    }

    public static boolean isHostPkgDebug = false;

    public static boolean isRatelDebugBuild = BuildConfig.DEBUG;


    public static Application realApplication = null;

    public static List<ProviderInfo> providers = null;

    private static boolean ratelEngineUpgrade = false;

    public static RatelEngine ratelEngine = RatelEngine.REBUILD_DEX;

    public static boolean isZeldaEngine() {
        return ratelEngine == RatelEngine.ZELDA;
    }

    public static boolean isKratosEngine() {
        return ratelEngine == RatelEngine.KEATOS;
    }

    public static void init(Context applicationContext, Context context, Context ratelEnvContext) throws Exception {
        //如果直接访问build对象，可能被代码优化工具删除代码块，所以这里包裹一下，避免代码优化
        if (getSdkInt() < SDK_VERSION_CODES.LOLLIPOP) {
            //还不支持Android 4.4 以下的版本
            throw new RuntimeException("ratel framework now support before android 4.4 now");
        }

        Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            Log.e(Constants.TAG, "error for thread: " + t, e);
            if (defaultUncaughtExceptionHandler != null) {
                //可能为null??
                defaultUncaughtExceptionHandler.uncaughtException(t, e);
            }
        });

        //android.os.Debug.waitForDebugger();

        RatelRuntime.ratelEnvContext = ratelEnvContext;
        RatelRuntime.originContext = context;
        RatelRuntime.entryContext = applicationContext;

        PackageManager unhookPackageManager = originContext.getPackageManager();
        h = unhookPackageManager.getPackageInfo(originContext.getPackageName(),
                PackageManager.GET_PROVIDERS |
                        PackageManager.GET_SIGNATURES | PackageManager.GET_PERMISSIONS);
        if (isRatelDebugBuild) {
            Log.i(Constants.TAG, "RATEL 启动 宿主APK 签名：" + SignatureKill.signatureInfo(h.signatures));
        }
        originApplicationInfo = unhookPackageManager
                .getApplicationInfo(originContext.getPackageName(),
                        PackageManager.GET_META_DATA);

        nowPackageName = originApplicationInfo.packageName;
        originPackageName = nowPackageName;


        //append multi dex方案&套壳容器方案，会走这里
        Bundle bundle = originApplicationInfo.metaData;
        if (bundle != null) {
            //TODO 统一修改为 配置文件
            //可能为null的
            originApplicationClassName = bundle.getString(Constants.APPLICATION_CLASS_NAME);
            originLaunchActivity = bundle.getString(Constants.ratelLaunchActivityName);
        }

        if (originLaunchActivity != null && originLaunchActivity.startsWith(".")) {
            originLaunchActivity = nowPackageName + originLaunchActivity;
        }


        tryTestHostDebugFlag();

        //invalid ratel resources,if a new apk installed
        ratelEngineUpgrade = checkCache(ratelEnvContext);

        //FileManager.releaseApkFiles(context);
        RatelEnvironment.releaseApkFiles();


        RatelConfig.init();

//        String ratelCertificateId = RatelConfig.getConfig("ratel_certificate_id");
//        if (TextUtils.isEmpty(ratelCertificateId)) {
//            //TODO 这里是一个bug，configFile被删除了，暂时不确定为啥
//            File file = RatelEnvironment.ratelConfigFile();
//            if (file.exists()) {
//                FileUtils.forceDelete(file);
//            }
//            RatelEnvironment.releaseApkFiles();
//            RatelConfig.init();
//        }

        //and then some flag need reset for ratel config
        resetRatelStatusIfEngineUpgrade();

        XposedModuleLoader.init();

        AnrDetector.setup();

        //first time to load so
        NativeBridge.nativeBridgeInit();
        //just hide maps for libratelnative.so
        NativeHide.doHide();


        RatelNative.nativeInit(context, nowPackageName);
        initSandHook();
        // 这个时候，才可以hook，xposed的相关API在这里之后才调用，才算安全吧
        // 另外在这之后再访问隐藏API，
        //开启批量hook
        SandHook.setBatchHook(true);


        mainThread = android.app.ActivityThread.currentActivityThread();
        Object mBoundApplication = RposedHelpers.getObjectField(mainThread, "mBoundApplication");
        processName = RposedHelpers.getObjectField(mBoundApplication, "processName");
        isMainProcess = nowPackageName.equals(processName);


        APIInitializer.initAPIConstants();

        EnvMockController.initEnvModel();

        IPCControlHandler.initRatelManagerIPCClient();
    }


    private static void initSandHook() {
        SandHook.disableVMInline();
        // SandHook.tryDisableProfile(RatelRuntime.getAppPackageName());
        SandHook.disableDex2oatInline(false);

        XposedCompat.cacheDir = RatelEnvironment.sandHookCacheDir();
        //TODO
        XposedCompat.classLoader = originContext.getClassLoader();
    }


    private static void resetRatelStatusIfEngineUpgrade() {
        if (!ratelEngineUpgrade) {
            return;
        }
        RatelConfig.setConfig(Constants.hasShellEngineInstalledKey, null);
        //RatelConfig.setConfig(Constants.useNewEnvMockComponent, "true");
    }

    public static void ratelStartUp(Context context) throws Exception {

        AssertFixer.beforeIORedirect();

        SandHook.setBatchHook(false);
        //加载所有的模块class
        XposedModuleLoader.loadXposedModuleClasses();
        //现在执行的是平头哥相关模块，他可以访问RatelToolKit，但是此时还没有处理模块的apk的重定向问题。
        //模块本身如果有检查apk合法性，那么将会出现问题。
        XposedModuleLoader.loadRatelModules();

        SchedulerTaskLoader.callSchedulerTask();
        SandHook.setBatchHook(true);

        EnvMockController.switchEnvIfNeed(context);

        XposedModuleLoader.relocateForRatelInfectedXposedModules();

        //enableIORedirect需要在ratel启动最后调用
        RatelNative.enableIORedirect();

        AssertFixer.afterIORedirect();
        SignatureFixer.fixSignature();

        SandHook.setBatchHook(false);


        //微X模块在访问assetManager，所以需要在 AssertFixer.afterIORedirect(); 执行之后才能加载模块
        //此时记载Xposed兼容的模块，模块本身apk重定向问题处理完成，如微X模块
        XposedModuleLoader.loadXposedModules();

        RatelNative.enableIORedirect();

        RatelNative.hideMaps();

        Set<RatalStartUpCallback> ratalStartUpCallbackSet = RatelToolKit.ratalStartUpCallbackSet;
        if (ratalStartUpCallbackSet != null) {
            for (RatalStartUpCallback ratalStartUpCallback : ratalStartUpCallbackSet) {
                ratalStartUpCallback.onRatelStartCompletedEvent();
            }
        }

        if (ratelEngineUpgrade) {
            Set<RatelEngineUpgradeEvent> ratelEngineUpgradeEventSet = RatelToolKit.ratelEngineUpgradeEventSet;
            if (ratelEngineUpgradeEventSet != null) {
                for (RatelEngineUpgradeEvent ratelEngineUpgradeEvent : ratelEngineUpgradeEventSet) {
                    ratelEngineUpgradeEvent.onEngineUpgrade();
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenAPIEnforcementPolicyUtils.reverseApiCheck();
        }

        APIInitializer.postInitAPIConstants();

        if (RatelToolKit.killAppIfDetectANR) {
            DeadLockKiller.killAppIfDeadLock();
        }

        //异步管理热发模块
        HotModuleManager.startMonitorNewConfig();

        startCompleted = true;
    }


    public static Object theLoadApk = null;


    /**
     * Android O 及以上的 debug 模式会强制走解释器模式
     * 当 ART 发现你的方法已经被编译的时候，就不会走 CodeEntry
     * ArtInterpreterToInterpreterBridge 直接解释 CodeItem
     * 如果是debug模式，需要将method设置为native，关闭解释模式
     * ArtMethod::disableInterpreterForO
     */
    private static void tryTestHostDebugFlag() {
        try {
            isHostPkgDebug = (originContext.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Class<?> hostBuildConfigClass = Class.forName(nowPackageName + ".BuildConfig", true, originContext.getClassLoader());
            isHostPkgDebug = RposedHelpers.getStaticBooleanField(hostBuildConfigClass, "DEBUG");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private static boolean checkCache(Context context) throws IOException {

        //release model,anti debug
        // 不管是debug模式，还是prod模式，都需要这个判断，这个是为了防止攻击方修改 BuildConfig.buildTimestamp
        long nowTime = System.currentTimeMillis();
        if (nowTime < BuildConfig.buildTimestamp + 60 * 1000) {
            ActivityThread.mH.set(ActivityThread.currentActivityThread, null);
            ActivityThread.mBoundApplication.set(ActivityThread.currentActivityThread, null);
        }

        //get now serialNo
        InputStream stream = context.getAssets().open(Constants.serialNoFile);
        String serialNo = IOUtils.toString(stream, StandardCharsets.UTF_8);
        IOUtils.closeQuietly(stream);
        if (isRatelDebugBuild) {
            Log.i(Constants.TAG, "the apk serial number: " + serialNo);
        }

        //read file system No
        File serialNoFile = new File(RatelEnvironment.ratelResourceDir(), Constants.serialNoFile);
        //we need refresh cache if the flag file not exist
        boolean needUpdate = !serialNoFile.exists();
        if (!needUpdate) {
            //the file exist but not equal with now version
            String historySerialNo = FileUtils.readFileToString(serialNoFile, StandardCharsets.UTF_8);
            if (isRatelDebugBuild) {
                Log.i(Constants.TAG, "old apk serial number: " + historySerialNo);
            }
            needUpdate = !serialNo.equals(historySerialNo);
        }
        if (!needUpdate) {
            if (isRatelDebugBuild) {
                Log.i(Constants.TAG, "apk serial number equal ratel resource valid");
            }
            return false;
        }
        if (isRatelDebugBuild) {
            Log.i(Constants.TAG, "a new apk installed ,clean ratel files");
        }

        //删除资源，但是取消全局配置文件目录
        File ratelConfigFile = RatelEnvironment.ratelConfigFile();
        File[] files = RatelEnvironment.ratelResourceDir().listFiles();
        if (files != null) {
            for (File subFile : files) {
                if (subFile.equals(ratelConfigFile)) {
                    continue;
                }
                if (isRatelDebugBuild) {
                    Log.i(Constants.TAG, "remove dir: " + subFile);
                }
                //delete ratel work directory
                FileUtils.forceDelete(subFile);
            }
        }

        FileUtils.writeStringToFile(serialNoFile, serialNo, StandardCharsets.UTF_8);

        //文件被删除后，需要重新创建目录列表
        RatelEnvironment.resetCache();
        return true;

    }

    public static Context getRatelEnvContext() {
        return ratelEnvContext;
    }

    public static Context getOriginContext() {
        return originContext;
    }

    public static ApplicationInfo getOriginApplicationInfo() {
        return originApplicationInfo;
    }

    //此乃手动混淆，我们在native层会访问h对象
    public static PackageInfo getH() {
        return h;
    }


    public static int getSdkInt() {
        try {
            Field sdkIntFiled = Build.VERSION.class.getField("SDK_INT");
            return (int) sdkIntFiled.get(null);
        } catch (Throwable throwable) {
            //ignore
            throwable.printStackTrace();
            return Build.VERSION.SDK_INT;
        }

    }

    public static boolean is64bit() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Process.is64Bit();
        }
        return VMRuntime.is64Bit.call(VMRuntime.getRuntime.call());

    }

    //TODO 这两个字段迁移到 EngineZelda.java
    public static Set<String> declaredComponentClassNames = new HashSet<>();
    public static Set<String> declaredAuthorities = new HashSet<>();
    public static Set<String> childProcesses = new HashSet<>();
    public static Map<String, String> nowProcess2OriginProcess = new HashMap<>();
    public static Map<String, String> originProcess2NowProcess = new HashMap<>();

    //dex entry rebuild entry
    @SuppressWarnings("unused")
    public static void startUp(Context applicationContext, Context context, boolean bypassHiddenAPIEnforcementPolicy) throws Throwable {
        EngineRebuildDex.startUp(applicationContext, context, bypassHiddenAPIEnforcementPolicy);
    }

    //append multi dex plan entry
    @SuppressWarnings("unused")
    public static void applicationAttachWithMultiDexMode(Context context) throws Throwable {
        EngineAppendDex.applicationAttachWithMultiDexMode(context);
    }

    @SuppressWarnings("unused")
    public static void applicationAttachWithZeldaEngine(Context context) throws Throwable {
        EngineZelda.applicationAttachWithZeldaEngine(context);
    }

    @SuppressWarnings("unused")
    public static void applicationAttachWithKratosEngine(Context context, Context ratelEnvContext) throws Throwable {
        EngineKratos.applicationAttachWithKratosEngine(context, ratelEnvContext);
    }

    @SuppressWarnings("unused")
    /**
     * 请注意，zelda在multiDex模式上面实现的，所以这个入口和zelda和multiDex共用
     */
    public static void applicationOnCreateWithMultiMode() {
        //mApplication的覆盖动作，必须放到onCreate调用的时候，因为 application的attach之后，才会对loadApk里面的mApplication复制。
        RposedHelpers.setObjectField(theLoadApk, "mApplication", realApplication);

        if (RatelRuntime.isZeldaEngine()) {
            //TODO com.erdos.tihuobao 惠运通app，appendDex模式，如果替换了这个对象
            // app 无限onCreate com.tencent.StubShell.TxAppEntry.onCreate
            // 对应apk： http://oss.virjar.com/ratel/com.erdos.tihuobao_1.3.3_27.apk
            RposedHelpers.setObjectField(RatelRuntime.mainThread, "mInitialApplication", RatelRuntime.realApplication);
        }
        Instrumentation instrumentation = RposedHelpers.getObjectField(
                mainThread, "mInstrumentation");
        instrumentation.callApplicationOnCreate(realApplication);
    }

    //shell engine entry
    public static void applicationAttachWithShellMode(Context context) throws Exception {
        EngineShell.applicationAttachWithShellMode(context);
    }

    @SuppressWarnings("unused")
    @Keep
    public static void callOnTerminate() {
        if (realApplication != null) {
            realApplication.onTerminate();
        }
    }

    @SuppressWarnings("unused")
    @Keep
    public static void callOnConfigurationChanged(Configuration configuration) {
        if (realApplication != null) {
            realApplication.onConfigurationChanged(configuration);
        }
    }

    @SuppressWarnings("unused")
    @Keep
    public static void callOnLowMemory() {
        if (realApplication != null) {
            realApplication.onLowMemory();
        }
    }

    @SuppressWarnings("unused")
    @Keep
    public static void callOnTrimMemory(int level) {
        if (realApplication != null) {
            realApplication.onTrimMemory(level);
        }
    }

    @SuppressWarnings("unused")
    @Keep
    public static void callRegisterComponentCallbacks(ComponentCallbacks componentCallbacks) {
        if (realApplication != null) {
            realApplication.registerComponentCallbacks(componentCallbacks);
        }
    }

    @SuppressWarnings("unused")
    @Keep
    public static void callUnregisterComponentCallbacks(ComponentCallbacks componentCallbacks) {
        if (realApplication != null) {
            realApplication.unregisterComponentCallbacks(componentCallbacks);
        }
    }

    @SuppressWarnings("unused")
    @Keep
    public static void callRegisterActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks activityLifecycleCallbacks) {
        if (realApplication != null) {
            realApplication.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
        }
    }

    @SuppressWarnings("unused")
    @Keep
    public static void callUnregisterActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks activityLifecycleCallbacks) {
        if (realApplication != null) {
            realApplication.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
        }
    }

    @SuppressWarnings("unused")
    @Keep
    public static void callRegisterOnProvideAssistDataListener(Application.OnProvideAssistDataListener onProvideAssistDataListener) {
        if (realApplication != null) {
            realApplication.registerOnProvideAssistDataListener(onProvideAssistDataListener);
        }
    }

    @SuppressWarnings("unused")
    @Keep
    public static void callUnregisterOnProvideAssistDataListener(Application.OnProvideAssistDataListener onProvideAssistDataListener) {
        if (realApplication != null) {
            realApplication.unregisterOnProvideAssistDataListener(onProvideAssistDataListener);
        }
    }
}
