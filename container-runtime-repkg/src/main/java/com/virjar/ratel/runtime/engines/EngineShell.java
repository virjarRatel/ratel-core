package com.virjar.ratel.runtime.engines;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.RatelEngine;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.runtime.RatelConfig;
import com.virjar.ratel.runtime.RatelEnvironment;
import com.virjar.ratel.runtime.RatelRuntime;
import com.virjar.ratel.sandhook.SandHookConfig;
import com.virjar.ratel.shellengine.InstallCallback;
import com.virjar.ratel.shellengine.ShellJumpActivity;
import com.virjar.ratel.shellengine.StatusCollectCallback;
import com.virjar.ratel.utils.HiddenAPIEnforcementPolicyUtils;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dalvik.system.DexClassLoader;

public class EngineShell {

    public static void applicationAttachWithShellMode(Context context) throws Exception {
        RatelRuntime.ratelEngine = RatelEngine.SHELL;
        if (SandHookConfig.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenAPIEnforcementPolicyUtils.passApiCheck();
        }
        RatelRuntime.init(null, context, context);


        // prevent installContentProviders because of classloader not ready
        RposedBridge.hookAllMethods(
                RposedHelpers.findClass("android.app.ActivityThread", ClassLoader.getSystemClassLoader()),
                "installContentProviders", new RC_MethodHook() {
                    @Override
                    @SuppressWarnings("unchecked")
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (RatelRuntime.providers == null) {
                            param.setResult(null);
                            RatelRuntime.providers = (List<ProviderInfo>) param.args[1];
                        }
                    }
                });

    }

    public static void installRatelShellEngine(final InstallCallback installCallback) {
        if (installFinished) {
            installCallback.onInstallSucced();
            return;
        }
        if (!hasInstalled.compareAndSet(false, true)) {
            installCallback.onInstallFailed(new IllegalStateException("install call already!!"));
            return;
        }
        new Thread("install-ratel-delegate") {
            @Override
            public void run() {
                try {
                    installInternal(installCallback);
                } catch (Throwable throwable) {
                    installCallback.onInstallFailed(throwable);
                }
            }
        }.start();
    }

    public static void installRatelShellEngineSync() throws Throwable {
        StatusCollectCallback statusCollectCallback = new StatusCollectCallback();
        installInternal(statusCollectCallback);
        if (!statusCollectCallback.success) {
            throw statusCollectCallback.throwable;
        }
    }

    private static AtomicBoolean hasInstalled = new AtomicBoolean(false);
    private static volatile boolean installFinished = false;


    private static void installInternal(final InstallCallback installCallback) {
        try {
            installInternalUncheck(installCallback);
        } catch (Throwable e) {
            installCallback.onInstallFailed(e);
        }
    }

    private static void installInternalUncheck(InstallCallback installCallback) throws Exception {
        if (installFinished) {
            installCallback.onInstallSucced();
            return;
        }

        DexClassLoader dexClassLoader = new DexClassLoader(RatelEnvironment.originApkDir().getCanonicalPath()
                , RatelEnvironment.APKOptimizedDirectory().getAbsolutePath(),
                RatelRuntime.originApplicationInfo.nativeLibraryDir,
                RatelRuntime.originContext.getClassLoader()
                //originContext.getClassLoader()
        );

        // classloader 替换到context
        Context contextImpl = RatelRuntime.originContext;
        Context nextContext;
        while ((contextImpl instanceof ContextWrapper) &&
                (nextContext = ((ContextWrapper) contextImpl).getBaseContext()) != null) {
            contextImpl = nextContext;
        }

        Object loadApk = RposedHelpers.getObjectField(contextImpl, "mPackageInfo");
        //TODO 框架有特征，classloader为dexClassloader，正常情况应该是pathClassLoader
        RposedHelpers.setObjectField(loadApk, "mClassLoader", dexClassLoader);

        ApplicationInfo applicationInfo = (ApplicationInfo) RposedHelpers.getObjectField(loadApk, "mApplicationInfo");
        if (RatelRuntime.originApplicationClassName != null) {
            applicationInfo.className = RatelRuntime.originApplicationClassName;
        }

        RatelRuntime.ratelStartUp(RatelRuntime.originContext);


        //创建代理 application
        if (RatelRuntime.originApplicationClassName != null) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Application> realClass =
                        (Class<? extends Application>) RposedHelpers.findClass(RatelRuntime.originApplicationClassName, dexClassLoader);
                Constructor<? extends Application> constructor = realClass.getConstructor();
                RatelRuntime.realApplication = constructor.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        } else {
            RatelRuntime.realApplication = new Application();
        }
        RatelRuntime.entryContext = RatelRuntime.realApplication;
        //这句话不能少
        RposedHelpers.setObjectField(loadApk, "mApplication", RatelRuntime.realApplication);


        //hook activity create ,替换掉classloader之后，再返回我们的跳转 activity 就会有classNotFound
        RposedHelpers.findAndHookMethod(android.app.Instrumentation.class, "newActivity", ClassLoader.class, String.class, Intent.class, new RC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String activityClassName = (String) param.args[1];
                if (ShellJumpActivity.class.getName().equals(activityClassName)) {
                    param.args[0] = ShellJumpActivity.class.getClassLoader();
                }
            }
        });

        RatelConfig.setConfig(Constants.hasShellEngineInstalledKey, "true");

        if (Looper.getMainLooper() == Looper.myLooper()) {
            try {
                callApplicationStartUp(installCallback);
            } catch (Throwable throwable) {
                installCallback.onInstallFailed(throwable);
            }
        } else {
            //创建逻辑，需要运行在线程
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        callApplicationStartUp(installCallback);
                    } catch (Throwable throwable) {
                        installCallback.onInstallFailed(throwable);
                    }
                }
            });
        }
    }

    private static void callApplicationStartUp(InstallCallback installCallback) {
        RposedHelpers.setObjectField(RatelRuntime.mainThread, "mInitialApplication", RatelRuntime.realApplication);
        RposedHelpers.callMethod(RatelRuntime.realApplication, "attachBaseContext", RatelRuntime.originContext);

        Instrumentation instrumentation = (Instrumentation) RposedHelpers.getObjectField(RatelRuntime.mainThread, "mInstrumentation");
        instrumentation.callApplicationOnCreate(RatelRuntime.realApplication);

        if (RatelRuntime.providers != null) {
            //now lazy  install providers
            RposedHelpers.callMethod(RatelRuntime.mainThread, "installContentProviders", RatelRuntime.realApplication, RatelRuntime.providers);
            RatelRuntime.providers = null;
        }

        installFinished = true;
        installCallback.onInstallSucced();
    }
}
