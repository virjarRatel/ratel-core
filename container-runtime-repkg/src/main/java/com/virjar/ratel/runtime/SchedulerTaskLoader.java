package com.virjar.ratel.runtime;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.scheduler.RatelTask;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dalvik.system.PathClassLoader;
import external.org.apache.commons.lang3.StringUtils;

public class SchedulerTaskLoader {

    static void callSchedulerTask() throws Exception {

        if (!RatelRuntime.isMainProcess) {
            //only execute on main thread
            return;
        }


        if (!XposedModuleLoader.isRatelManagerInstalled()) {
            return;
        }

        if (XposedModuleLoader.getRatelManagerVersionCode() < 8) {
            //not support ratel scheduler task before ratelManager 1.0.6
            return;
        }


        Bundle bundle;
        try {
            bundle = RatelRuntime.getOriginContext().getContentResolver().call(
                    Uri.parse("content://com.virjar.ratel.manager"),
                    Constants.METHOD_QUERY_SCHEDULER_TASK, RatelRuntime.originPackageName, null
            );
        } catch (Exception e) {
            //ignore
            return;
        }

        if (bundle == null) {
            //no task
            return;
        }

        Set<String> keySet = bundle.keySet();
        Map<String, String> params = new HashMap<>();
        for (String key : keySet) {
            String value = bundle.getString(key);
            if (value == null) {
                continue;
            }
            params.put(key, value);
        }
        callSchedulerTask(params);
    }

    public static void callSchedulerTask(Map<String, String> params) throws Exception {
        /**
         *         taskParams.put("ratel.taskImplementationClassName", schedulerTask.getTaskImplementationClassName());
         *         taskParams.put("ratel.ratelTaskId", schedulerTask.getTaskId());
         *         taskParams.put("ratel.modulePackageName", schedulerTask.getModuleApkPackage());
         */
        String taskImplementationClassName = params.get("ratel.taskImplementationClassName");
        ratelTaskId = params.get("ratel.ratelTaskId");
        String modulePackageName = params.get("ratel.modulePackageName");

        Log.i(Constants.SCHEDULER_TAG, "scheduler client-> ratelTaskId:" + ratelTaskId);
        Log.i(Constants.SCHEDULER_TAG, "scheduler client-> params:" + new JSONObject(params).toString());

        PackageInfo packageInfo;
        try {
            packageInfo = RatelRuntime.getOriginContext().getPackageManager().getPackageInfo(modulePackageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return;
        }

        ClassLoader classLoader = XposedModuleLoader.queryModuleClassLoaderCache(packageInfo.applicationInfo.sourceDir);
        if (classLoader == null) {
            classLoader = new PathClassLoader(packageInfo.applicationInfo.sourceDir, packageInfo.applicationInfo.nativeLibraryDir, RposedBridge.class.getClassLoader());
            XposedModuleLoader.cacheClassLoader(packageInfo.applicationInfo.sourceDir, classLoader);
        }

        String userId = StringUtils.trimToNull(params.get(Constants.KEY_RATEL_USER_ID));
        if (userId != null) {
            RatelToolKit.virtualEnv.switchEnv(userId);
        }

        RatelTask ratelTask = (RatelTask) classLoader.loadClass(taskImplementationClassName).newInstance();
        ratelTask.doRatelTask(params);
    }

    private static String ratelTaskId = null;


    static void finishTask() {
        if (ratelTaskId == null) {
            return;
        }
        if (!XposedModuleLoader.isRatelManagerInstalled()) {
            return;
        }

        if (XposedModuleLoader.getRatelManagerVersionCode() < 8) {
            //not support ratel scheduler task before ratelManager 1.0.6
            return;
        }


        RatelRuntime.getOriginContext().getContentResolver().call(
                Uri.parse("content://com.virjar.ratel.manager"),
                Constants.METHOD_FINISH_SCHEDULER_TASK, ratelTaskId, null
        );

        ratelTaskId = null;

    }
}
