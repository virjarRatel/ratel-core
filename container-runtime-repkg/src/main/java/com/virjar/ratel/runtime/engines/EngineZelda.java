package com.virjar.ratel.runtime.engines;

import android.app.Instrumentation;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.RatelNative;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.RatelEngine;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.core.runtime.BuildConfig;
import com.virjar.ratel.runtime.RatelEnvironment;
import com.virjar.ratel.runtime.RatelRuntime;
import com.virjar.ratel.runtime.fixer.AppBindDataFixer;
import com.virjar.ratel.runtime.fixer.IPCPackageNameFixer;
import com.virjar.ratel.runtime.fixer.ProviderUriFixer;
import com.virjar.ratel.runtime.fixer.StorageManagerFixer;
import com.virjar.ratel.sandhook.SandHookConfig;
import com.virjar.ratel.utils.HiddenAPIEnforcementPolicyUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class EngineZelda {
    public static void applicationAttachWithZeldaEngine(Context context) throws Throwable {
        RatelRuntime.ratelEngine = RatelEngine.ZELDA;
        if (BuildConfig.DEBUG) {
            Log.i(Constants.TAG, "Zelda entry");
        }
        if (SandHookConfig.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenAPIEnforcementPolicyUtils.passApiCheck();
        }
        RatelRuntime.init(null, context, context);

        // 提前访问一下这个路径，触发到 /data/user/0/com.sdu.didi.gsui/app_ratel_env_mock
        RatelEnvironment.envMockDir();

        restoreDeclaredComponent();

        Context contextImpl = context;
        Context nextContext;
        while ((contextImpl instanceof ContextWrapper) &&
                (nextContext = ((ContextWrapper) contextImpl).getBaseContext()) != null) {
            contextImpl = nextContext;
        }

        Object loadApk = RposedHelpers.getObjectField(contextImpl, "mPackageInfo");
        RatelRuntime.theLoadApk = loadApk;

        //修复application里面的className
        ApplicationInfo applicationInfo = RposedHelpers.getObjectField(loadApk, "mApplicationInfo");
        if (RatelRuntime.originApplicationClassName != null) {
            applicationInfo.className = RatelRuntime.originApplicationClassName;
        } else {
            applicationInfo.className = "android.app.Application";
        }


        IPCPackageNameFixer.fixIpcTransact();
        ProviderUriFixer.fixUri();

        AppBindDataFixer.fixAppBindData();
        StorageManagerFixer.relocateStorageManager();
        //BroadcastFixer.fix();

        RatelNative.enableIORedirect();
        RatelNative.enableZeldaNative(RatelRuntime.originPackageName, RatelRuntime.nowPackageName);

        //重制缓存，否则可能清空文件的时候出错
        RatelEnvironment.resetCache();

        //可以开始加载xposed模块了
        RatelRuntime.ratelStartUp(context);

//        RatelNative.enableZeldaNative(RatelRuntime.originPackageName, RatelRuntime.nowPackageName);
//        AppBindDataFixer.fixAppBindData();

        //String initApplicationClassName = "android.app.Application.Application";
        if (TextUtils.isEmpty(RatelRuntime.originApplicationClassName)) {
            RatelRuntime.originApplicationClassName = "android.app.Application.Application";
        }
        if (RatelRuntime.isRatelDebugBuild) {
            Log.i(Constants.TAG, "load originApplicationClassName: " + RatelRuntime.originApplicationClassName);
        }
//        if (TextUtils.isEmpty(RatelRuntime.originApplicationClassName)) {
//            RatelRuntime.realApplication = new Application();
//        } else {
//            if (RatelRuntime.isRatelDebugBuild) {
//                Log.i(Constants.TAG, "load originApplicationClassName: " + RatelRuntime.originApplicationClassName);
//            }
//            try {
//                @SuppressWarnings("unchecked")
//                Class<? extends Application> realClass =
//                        (Class<? extends Application>) RposedHelpers.findClass(RatelRuntime.originApplicationClassName, context.getClassLoader());
//                Constructor<? extends Application> constructor = realClass.getConstructor();
//                RatelRuntime.realApplication = constructor.newInstance();
//            } catch (Exception e) {
//                throw new IllegalStateException(e);
//            }
//        }

        RatelRuntime.entryContext = RatelRuntime.realApplication;

        Instrumentation instrumentation = RposedHelpers.getObjectField(
                RatelRuntime.mainThread, "mInstrumentation");
        RatelRuntime.realApplication = instrumentation.newApplication(context.getClassLoader(), RatelRuntime.originApplicationClassName, context);


//        RposedHelpers.setObjectField(RatelRuntime.mainThread, "mInitialApplication", RatelRuntime.realApplication);
//        RposedHelpers.callMethod(RatelRuntime.realApplication, "attachBaseContext", context);
    }

    private static void restoreDeclaredComponent() {
        AssetManager assets = RatelRuntime.originContext.getAssets();
        InputStream inputStream;
        try {
            try {
                inputStream = assets.open(Constants.declaredComponentListConfig);
            } catch (FileNotFoundException fie) {
                //ignore
                return;
            }
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith(Constants.KEY_KEEP_CLASS_NAMES)) {
                    RatelRuntime.declaredComponentClassNames.add(line.substring(Constants.KEY_KEEP_CLASS_NAMES.length()));
                } else if (line.startsWith(Constants.KEY_REPLACE_AUTHORITIES)) {
                    RatelRuntime.declaredAuthorities.add(line.substring(Constants.KEY_REPLACE_AUTHORITIES.length()));
                } else if (line.startsWith(Constants.KEY_CHILD_PROCESS)) {
                    String childProcess = line.substring(Constants.KEY_CHILD_PROCESS.length());
                    String nowProcess = RatelRuntime.nowPackageName + childProcess;
                    String originProcess = RatelRuntime.originPackageName + childProcess;
                    RatelRuntime.childProcesses.add(childProcess);
                    RatelRuntime.nowProcess2OriginProcess.put(nowProcess, originProcess);
                    RatelRuntime.originProcess2NowProcess.put(originProcess, nowProcess);
                }
            }
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e(Constants.TAG, "copy assets resource failed!!", e);
            throw new IllegalStateException(e);
        }
    }
}

/**
 * TODO
 * 1. 二次获取 android.app.ActivityThread.AppBindData 拦截
 */