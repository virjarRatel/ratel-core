package com.virjar.ratel.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.manager.component.AppWatchDogService;
import com.virjar.ratel.manager.model.RatelApp;
import com.virjar.ratel.manager.repo.RatelAppRepo;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class AppDaemonTaskManager {

    private static Set<String> needWatchProcess = new CopyOnWriteArraySet<>();
    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    static void init(Context context) {
        sContext = context;
        if (!AppWatchDogService.hasServiceStarted) {
            Intent intent = new Intent(sContext, AppWatchDogService.class);
            try {
                sContext.startService(intent);
            } catch (Exception e) {
                Log.w(Constants.TAG, "start AppWatchDogService failed", e);
            }
        }

        for (RatelApp ratelApp : RatelAppRepo.installedApps()) {
            updateDaemonStatus(ratelApp.getPackageName(), ratelApp.isDaemon());
        }
    }

    public static void updateDaemonStatus(String targetAppPackage, boolean watchStatus) {
        if (!watchStatus) {
            needWatchProcess.remove(targetAppPackage);
            return;
        }
        try {
            sContext.getPackageManager().getPackageInfo(targetAppPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(RatelManagerApp.TAG, "the app:" + targetAppPackage + " not installed", e);
            return;
        }
        needWatchProcess.add(targetAppPackage);

        // RatelAccessibilityService.guideToAccessibilityPage(sContext);
    }

    public static Set<String> getWatchAppTask() {
        return new CopyOnWriteArraySet<>(needWatchProcess);
    }

    public static boolean needWatch(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        return needWatchProcess.contains(packageName);
    }

    public static boolean hasWatchTask() {
        return needWatchProcess.size() > 0;
    }
}
