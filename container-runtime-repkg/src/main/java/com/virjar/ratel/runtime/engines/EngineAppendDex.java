package com.virjar.ratel.runtime.engines;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.RatelEngine;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.core.runtime.BuildConfig;
import com.virjar.ratel.runtime.RatelRuntime;
import com.virjar.ratel.sandhook.SandHookConfig;
import com.virjar.ratel.utils.HiddenAPIEnforcementPolicyUtils;

import java.lang.reflect.Constructor;

public class EngineAppendDex {
    public static void applicationAttachWithMultiDexMode(Context context) throws Throwable {
        RatelRuntime.ratelEngine = RatelEngine.APPEND_DEX;
        if (BuildConfig.DEBUG) {
            Log.i(Constants.TAG, "multi dex apppend entry");
        }
        if (SandHookConfig.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenAPIEnforcementPolicyUtils.passApiCheck();
        }
        RatelRuntime.init(null, context, context);

        Context contextImpl = context;
        Context nextContext;
        while ((contextImpl instanceof ContextWrapper) &&
                (nextContext = ((ContextWrapper) contextImpl).getBaseContext()) != null) {
            contextImpl = nextContext;
        }

        Object loadApk = RposedHelpers.getObjectField(contextImpl, "mPackageInfo");
        RatelRuntime.theLoadApk = loadApk;

        //修复application里面的className
        ApplicationInfo applicationInfo = (ApplicationInfo) RposedHelpers.getObjectField(loadApk, "mApplicationInfo");
        if (RatelRuntime.originApplicationClassName != null) {
            applicationInfo.className = RatelRuntime.originApplicationClassName;
            applicationInfo.name = RatelRuntime.originApplicationClassName;
        } else {
            applicationInfo.className = "android.app.Application";
            applicationInfo.name = null;
        }

        //可以开始加载xposed模块了
        RatelRuntime.ratelStartUp(context);

        //创建代理 application
        if (TextUtils.isEmpty(RatelRuntime.originApplicationClassName)) {
            RatelRuntime.realApplication = new Application();
        } else {
            if (RatelRuntime.isRatelDebugBuild) {
                Log.i(Constants.TAG, "load originApplicationClassName: " + RatelRuntime.originApplicationClassName);
            }
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Application> realClass =
                        (Class<? extends Application>) RposedHelpers.findClass(RatelRuntime.originApplicationClassName, context.getClassLoader());
                Constructor<? extends Application> constructor = realClass.getConstructor();
                RatelRuntime.realApplication = constructor.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        RatelRuntime.entryContext = RatelRuntime.realApplication;
        RposedHelpers.setObjectField(RatelRuntime.mainThread, "mInitialApplication", RatelRuntime.realApplication);
        RposedHelpers.callMethod(RatelRuntime.realApplication, "attachBaseContext", context);
    }

}
