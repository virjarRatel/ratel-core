package com.virjar.ratel.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.ArrayMap;
import android.util.Log;

import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.inspect.ClassLoadMonitor;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.core.runtime.BuildConfig;
import com.virjar.ratel.runtime.RatelRuntime;

import java.util.List;

public class ProcessUtil {

    private static Class<?> pageTriggerManagerClass;

    private static boolean setup = false;

    private static final String pageTriggerManagerClassName = "com.virjar.ratel.api.extension.superappium.PageTriggerManager";

    public static void collectSupperAppiumClass() {
        if (setup) {
            return;
        }
        setup = true;
        ClassLoadMonitor.addClassLoadMonitor(pageTriggerManagerClassName
                , clazz -> pageTriggerManagerClass = clazz
        );
    }

    public static void killMe() {
        if(BuildConfig.DEBUG){
            Log.i(RatelToolKit.TAG,"kill me:",new Throwable());
        }
        //com.virjar.ratel.api.extension.superappium.PageTriggerManager.setDisable
        if (pageTriggerManagerClass != null) {
            RposedHelpers.callStaticMethod(pageTriggerManagerClass, "setDisable", true);
        } else {
            Log.w(RatelToolKit.TAG, "can not find pageTriggerManagerClass: " + pageTriggerManagerClassName);
        }

        MainThreadRunner.runOnFocusActivity(new MainThreadRunner.FocusActivityOccurEvent() {
            @Override
            public boolean onFocusActivityOccur(Activity activity) {
                activity.finish();
                return true;
            }

            @Override
            public void onActivityEmpty() {
                doKill();
            }

            @Override
            public boolean onLostFocus() {
                ArrayMap mActivities = RposedHelpers.getObjectField(RatelRuntime.mainThread, "mActivities");
                if (mActivities.size() == 0) {
                    doKill();
                    return true;
                }

                Object activityClientRecord = mActivities.valueAt(mActivities.size() - 1);
                Activity tempActivity = RposedHelpers.getObjectField(activityClientRecord, "activity");
                tempActivity.finish();
                return false;
            }

        });

        new Handler(Looper.getMainLooper()).postDelayed(ProcessUtil::doKill, 10000);
    }

    private static void doKill() {
        ActivityManager activityManager = (ActivityManager) RatelRuntime.getOriginContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            Process.killProcess(Process.myPid());
        } else {
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
                if (runningAppProcessInfo.processName.contains(RatelRuntime.nowPackageName) && runningAppProcessInfo.pid != Process.myPid()) {
                    Process.killProcess(runningAppProcessInfo.pid);
                }
            }
            Process.killProcess(Process.myPid());
        }
    }
}
