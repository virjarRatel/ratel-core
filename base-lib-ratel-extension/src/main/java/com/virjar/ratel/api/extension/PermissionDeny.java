package com.virjar.ratel.api.extension;

import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.SDK_VERSION_CODES;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;

import java.util.Arrays;

public class PermissionDeny {
    public static void denyAllPermission() {
        if (Build.VERSION.SDK_INT < SDK_VERSION_CODES.M) {
            return;
        }

        RposedBridge.hookAllMethods(Activity.class, "requestPermissions", new RC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(null);
                final Activity activity = (Activity) param.thisObject;
                final String[] permissions = (String[]) param.args[0];
                Log.i(RatelToolKit.TAG, "Activity hook permissions " + Arrays.toString(permissions));
                final int requestCode = (int) param.args[1];
                final int[] ints = new int[permissions.length];
                Arrays.fill(ints, PackageManager.PERMISSION_DENIED);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @RequiresApi(api = SDK_VERSION_CODES.M)
                    @Override
                    public void run() {
                        RposedHelpers.callMethod(activity,
                                "onRequestPermissionsResult",
                                requestCode, permissions, ints
                        );

                        // activity.onRequestPermissionsResult(requestCode, permissions, ints);
                    }
                }, 1500);
            }
        });

        RposedBridge.hookAllMethods(Fragment.class, "requestPermissions", new RC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(null);
                final Fragment fragment = (Fragment) param.thisObject;
                final String[] permissions = (String[]) param.args[0];

                Log.i(RatelToolKit.TAG, "Fragment hook permissions " + Arrays.toString(permissions));
                final int requestCode = (int) param.args[1];
                final int[] ints = new int[permissions.length];
                Arrays.fill(ints, PackageManager.PERMISSION_DENIED);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @RequiresApi(api = SDK_VERSION_CODES.M)
                    @Override
                    public void run() {
                        // fragment.onRequestPermissionsResult(requestCode, permissions, ints);
                        RposedHelpers.callMethod(fragment, "onRequestPermissionsResult",
                                requestCode, permissions, ints);
                    }
                }, 1500);
            }

        });
    }
}