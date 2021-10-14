package com.virjar.ratel.shellengine;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.hook.RatelHookFlagMethodCallback;
import com.virjar.ratel.runtime.engines.EngineShell;
import com.virjar.ratel.runtime.RatelConfig;
import com.virjar.ratel.runtime.RatelRuntime;

import external.org.apache.commons.lang3.StringUtils;

@SuppressLint("Registered")
public class ShellEngineEntryApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        try {
            RatelRuntime.applicationAttachWithShellMode(base);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //install synchronized anyway if this is not first startup
        if (StringUtils.isNotBlank(RatelConfig.getConfig(Constants.hasShellEngineInstalledKey))) {
            try {
                EngineShell.installRatelShellEngineSync();
                return;
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }

        }

        if (RatelRuntime.isMainProcess) {
            // in main process, the delegate apk need dex2oat before it`s first load,this process will take a long time
            // an ANR will happen if we do dex2oat process in UI thread,and user maybe confused with UI blocking
            // but if a backend service started in the main process,we must patch classloader synchronized ,this case will happened from "multi-process daemon"
            // so make sure ratel installed before any service created
            // TODO how about other android component,such as content provider|broadcast receiver
            RposedBridge.hookAllMethods(
                    RposedHelpers.findClass("android.app.ActivityThread", ClassLoader.getSystemClassLoader()),
                    "handleCreateService",
                    new RatelHookFlagMethodCallback() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            EngineShell.installRatelShellEngineSync();
                        }
                    });
            return;
        }
        // if this process is a child process, the dex2oat must has bean finished, the install process running very fast
        // or if this if a ui task, child process must called from other interactive ui process, do not worry about ANR
        try {
            EngineShell.installRatelShellEngineSync();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
