package com.virjar.ratel.manager.component;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import com.virjar.ratel.manager.BuildConfig;
import com.virjar.ratel.manager.RatelManagerApp;
import com.virjar.ratel.manager.ShellStartActivityService;
import com.virjar.ratel.manager.util.HiddenAPIEnforcementPolicyUtils;
import com.virjar.ratel.manager.util.ReflectUtil;

import java.io.OutputStream;

public class ShellStartActivityServiceImpl extends ShellStartActivityService.Stub {

    static {
        initIfNeed();
    }

    private static void initIfNeed() {
        if (RatelManagerApp.getInstance() != null) {
            return;
        }
        // 运行在命令行模式下
        HiddenAPIEnforcementPolicyUtils.bypassHiddenAPIEnforcementPolicyIfNeeded();


    }

    private static Context sContext;

    @Override
    public void startActivity(Intent intent) throws RemoteException {
        try {
            Class<?> ActivityThreadClass = ReflectUtil.findClass("android.app.ActivityThread", ClassLoader.getSystemClassLoader());
            Object activityThread = ReflectUtil.callStaticMethod(ActivityThreadClass, "systemMain");

            Context systemContext = (Context) ReflectUtil.callMethod(activityThread, "getSystemContext");

            try {
                sContext = systemContext.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            } catch (Throwable throwable) {
                Log.e(RatelManagerApp.TAG, "err", throwable);
                throw new RuntimeException(throwable);
            }
            sContext.startActivity(intent);
        } catch (Throwable throwable) {

            Log.e(RatelManagerApp.TAG, "err", throwable);

            ComponentName component = intent.getComponent();
            if (component != null) {
                try {
                    String cmd = "am start -n " + component.getPackageName() + "/" + component.getClassName() + "\n";
                    java.lang.Process process = Runtime.getRuntime().exec("sh");
                    OutputStream os = process.getOutputStream();
                    os.write(cmd.getBytes());
                    os.flush();
                    os.close();
                    int exitCode = process.waitFor();
                    if (exitCode != 0) {
                        throw throwable;
                    }
                    Log.i(RatelManagerApp.TAG, "start activity from shell success!");
                    return;
                } catch (Throwable e) {
                    Log.e(RatelManagerApp.TAG, "err for shell: ", e);
                    throw new IllegalStateException(e.getMessage());
                }
            }


            throw throwable;
        }
    }
}
