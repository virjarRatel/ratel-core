package com.virjar.ratel.manager;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;

import com.virjar.ratel.manager.component.FingerDataLoader;
import com.virjar.ratel.manager.util.BDLocationUtils;
import com.virjar.ratel.manager.util.HiddenAPIEnforcementPolicyUtils;
import com.virjar.ratel.manager.util.ShizukuToolkit;

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
}
