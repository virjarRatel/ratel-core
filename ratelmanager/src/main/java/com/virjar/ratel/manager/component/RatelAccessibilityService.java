package com.virjar.ratel.manager.component;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.virjar.ratel.manager.AppDaemonTaskManager;
import com.virjar.ratel.manager.RatelManagerApp;

import java.util.HashSet;
import java.util.Set;

public class RatelAccessibilityService extends AccessibilityService {
    public static RatelAccessibilityService instance;

    private static final Set<String> phoneHomePackage = new HashSet<>();

    static {
        phoneHomePackage.add("com.android.systemui");
        phoneHomePackage.add("com.miui.home");
        //TODO 其他机型适配
    }

    private static Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    public static void requestStartRatelManager() {
        final RatelAccessibilityService ratelAccessibilityService = instance;
        if (ratelAccessibilityService == null) {
            return;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                ratelAccessibilityService.autoStartRatelManager();
            }
        });

    }

    private void autoStartRatelManager() {
        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        if (rootInActiveWindow == null) {
            Log.w(RatelManagerApp.TAG, "can not get root InActive Window");
            return;
        }
        if (!phoneHomePackage.contains(rootInActiveWindow.getPackageName().toString())) {
            //只有当app处于主页的时候，我们才干活儿，其他时候证明有其他任务
            //如果处于ratelManager页面，由于ratelManager在前台
            Log.w(RatelManagerApp.TAG, "we will do nothing if another app is running ：" + rootInActiveWindow.getPackageName().toString());
            return;
        }
        Log.i(RatelManagerApp.TAG, "rootInActiveWindow: " + rootInActiveWindow.toString());

        //rootInActiveWindow.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);

        Rect rect = new Rect();
        rootInActiveWindow.getBoundsInScreen(rect);

        Path path = new Path();
        path.moveTo(400, 0);
        path.lineTo(400, (int) (rect.bottom * 0.6));


        final GestureDescription.StrokeDescription sd;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            sd = new GestureDescription.StrokeDescription(path, 0, 1000);
            dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
                    if (rootInActiveWindow == null) {
                        Log.w(RatelManagerApp.TAG, "can not get root InActive Window");
                        return;
                    }
                    clickRatelNotification(rootInActiveWindow);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                }
            }, null);
        }

    }

    private boolean clickRatelNotification(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return false;
        }
        CharSequence text = nodeInfo.getText();
        //Log.i(CommonUtil.TAG, "access tag: " + text + "  class: " + nodeInfo.getClassName());
        if (text != null && text.toString().contains("RatelManager")) {
            return true;
        }
        int childCount = nodeInfo.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);
            if (clickRatelNotification(childNodeInfo)) {
                if (nodeInfo.isClickable()) {
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(RatelManagerApp.TAG, "onAccessibilityEvent: " + event.toString());
        if (event.getPackageName().equals("com.android.systemui")) {
            return;
        }
        Log.d(RatelManagerApp.TAG, "onAccessibilityEvent: " + event.toString());
    }

    @Override
    public void onInterrupt() {

    }


    public static void guideToAccessibilityPage(Context context) {
        if (isAccessibilitySettingsOn(context)) {
            return;
        }
        if (AppDaemonTaskManager.hasWatchTask()) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private static Boolean accessibilitySettingsOn = null;

    private static boolean isAccessibilitySettingsOn(Context mContext) {
        if (accessibilitySettingsOn != null && accessibilitySettingsOn) {
            return accessibilitySettingsOn;
        }
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + RatelAccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(RatelManagerApp.TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(RatelManagerApp.TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(RatelManagerApp.TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v(RatelManagerApp.TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(RatelManagerApp.TAG, "We've found the correct setting - accessibility is switched on!");
                        accessibilitySettingsOn = true;
                        // return true;
                    }
                }
            }
        } else {
            Log.v(RatelManagerApp.TAG, "***ACCESSIBILITY IS DISABLED***");
        }

        //return false;
        accessibilitySettingsOn = false;
        return accessibilitySettingsOn;
    }
}
