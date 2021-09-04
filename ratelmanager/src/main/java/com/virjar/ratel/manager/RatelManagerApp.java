package com.virjar.ratel.manager;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.virjar.ratel.manager.component.FingerDataLoader;
import com.virjar.ratel.manager.util.BDLocationUtils;
import com.virjar.ratel.manager.util.HiddenAPIEnforcementPolicyUtils;
import com.virjar.ratel.manager.util.ShizukuToolkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class RatelManagerApp extends Application {
    public static final String TAG = "RatelManager";

    public static int WRITE_EXTERNAL_PERMISSION = 69;
    private static RatelManagerApp mInstance = null;

    public static RatelManagerApp getInstance() {
        return mInstance;
    }


    public void onCreate() {
        super.onCreate();
        mInstance = this;
        ManagerInitiazer.init(this);

        setNotifyChannel();

        BDLocationUtils bdLocationUtils = new BDLocationUtils(this);
        //开启定位
        bdLocationUtils.doLocation();
        //开始定位
        bdLocationUtils.mLocationClient.start();

        HiddenAPIEnforcementPolicyUtils.bypassHiddenAPIEnforcementPolicyIfNeeded();
        //Shizuku会大量访问hiddenAPI，所以我们需要在bypassHiddenAPI之后才能初始化Shizuku
        ShizukuToolkit.init();

        FingerDataLoader.refreshFinger();

        if (!checkFloatPermission(this)) {
            Log.i(RatelManagerApp.TAG,"请给软件设置悬浮窗权限，否则可能影响RatelScheduler！");
            Toast.makeText(getApplicationContext(), "请给软件设置悬浮窗权限，否则可能影响RatelScheduler！", Toast.LENGTH_SHORT).show();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(intent);
            }
        }

        if(!canBackgroundStart(this)){
            Toast.makeText(getApplicationContext(), "请给软件设置允许后台启动页面权限，否则可能影响RatelScheduler！", Toast.LENGTH_SHORT).show();
        }
    }

    private void setNotifyChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(BuildConfig.APPLICATION_ID,
                "channel", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.YELLOW);
        notificationChannel.setShowBadge(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }
        manager.createNotificationChannel(notificationChannel);
    }

    private boolean checkFloatPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                Class cls = Class.forName("android.content.Context");
                Field declaredField = cls.getDeclaredField("APP_OPS_SERVICE");
                declaredField.setAccessible(true);
                Object obj = declaredField.get(cls);
                if (!(obj instanceof String)) {
                    return false;
                }
                String str2 = (String) obj;
                obj = cls.getMethod("getSystemService", String.class).invoke(context, str2);
                cls = Class.forName("android.app.AppOpsManager");
                Field declaredField2 = cls.getDeclaredField("MODE_ALLOWED");
                declaredField2.setAccessible(true);
                Method checkOp = cls.getMethod("checkOp", Integer.TYPE, Integer.TYPE, String.class);
                int result = (Integer) checkOp.invoke(obj, 24, Binder.getCallingUid(), context.getPackageName());
                return result == declaredField2.getInt(cls);
            } catch (Exception e) {
                return false;
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AppOpsManager appOpsMgr = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                if (appOpsMgr == null)
                    return false;
                int mode = appOpsMgr.checkOpNoThrow("android:system_alert_window", android.os.Process.myUid(), context
                        .getPackageName());
                return mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_IGNORED;
            } else {
                return Settings.canDrawOverlays(context);
            }
        }
    }

    private boolean canBackgroundStart(Context context) {
        AppOpsManager ops = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        try {
            int op = 10021; // >= 23
            // ops.checkOpNoThrow(op, uid, packageName)
            Method method = ops.getClass().getMethod("checkOpNoThrow", int.class, int.class, String.class);
            Integer result = (Integer) method.invoke(ops, op, Process.myUid(), context.getPackageName());
            return result == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            Log.e(TAG, "not support exec checkOpNoThrow", e);
            return true;
        }
    }
}
