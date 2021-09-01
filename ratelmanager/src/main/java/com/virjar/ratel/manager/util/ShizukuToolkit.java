package com.virjar.ratel.manager.util;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.virjar.ratel.manager.BuildConfig;
import com.virjar.ratel.manager.RatelManagerApp;
import com.virjar.ratel.manager.ShellStartActivityService;
import com.virjar.ratel.manager.component.ShellStartActivityServiceImpl;

import rikka.shizuku.Shizuku;
import rikka.sui.Sui;

public class ShizukuToolkit {
    private static final boolean isSui;

    public static boolean isSui() {
        return isSui;
    }

    static {
        // 面具模式下这个才为真
        isSui = Sui.init(BuildConfig.APPLICATION_ID);
    }


    public static void init() {
        Shizuku.addBinderReceivedListener(
                () -> new Handler(Looper.getMainLooper()).postDelayed(ShizukuToolkit::onBinderEstablish, 500)
        );
    }


    private static ShellStartActivityService shellStartActivityService;

    public static boolean startActivity(Intent intent) {
        ShellStartActivityService copy = shellStartActivityService;
        if (copy == null) {
            return false;
        }
        try {
            copy.startActivity(intent);
            return true;
        } catch (Throwable throwable) {
            Log.e(RatelManagerApp.TAG, "shizhuku activity error", throwable);
            return false;
        }
    }

    private static final ServiceConnection userServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            Log.i(RatelManagerApp.TAG, "onShizuku userServiceConnection...");
            if (binder != null && binder.pingBinder()) {
                shellStartActivityService = ShellStartActivityService.Stub.asInterface(binder);
            } else {
                Log.i(RatelManagerApp.TAG, "Shizuku userServiceConnection binder error");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            shellStartActivityService = null;
        }
    };

    private static final Shizuku.UserServiceArgs userServiceStandaloneProcessArgs =
            new Shizuku.UserServiceArgs(new ComponentName(BuildConfig.APPLICATION_ID, ShellStartActivityServiceImpl.class.getName()))
                    .processNameSuffix("ratelWatchDog")
                    .debuggable(BuildConfig.DEBUG)
                    .version(BuildConfig.VERSION_CODE);

    private static void onBinderEstablish() {
        Log.i(RatelManagerApp.TAG, "onShizukuBinderEstablish");
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            try {
                Shizuku.bindUserService(userServiceStandaloneProcessArgs, userServiceConnection);
                Log.i(RatelManagerApp.TAG, "bindUserServiceFinished");
            } catch (Throwable throwable) {
                Log.w(RatelManagerApp.TAG, "error", throwable);
                new Handler(Looper.getMainLooper()).postDelayed(
                        ShizukuToolkit::onBinderEstablish,
                        500);
            }
            return;

        }
        Shizuku.requestPermission("RM-shizhuku".hashCode());
        Shizuku.OnRequestPermissionResultListener onRequestPermissionResultListener = new Shizuku.OnRequestPermissionResultListener() {
            @Override
            public void onRequestPermissionResult(int requestCode, int grantResult) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    Shizuku.bindUserService(userServiceStandaloneProcessArgs, userServiceConnection);
                }
                Shizuku.removeRequestPermissionResultListener(this);
            }
        };
        Shizuku.addRequestPermissionResultListener(onRequestPermissionResultListener);
    }
}
