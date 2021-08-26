package com.virjar.ratel.api.extension.superappium;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.LocalActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.PopupWindow;

import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * 基于页面实现控制逻辑抽象，包括activity和fragment两个维度
 * TODO 还需要增加对webview的监控
 */
public class PageTriggerManager {
    public interface ActivityFocusHandler {
        boolean handleActivity(Activity activity, ViewImage root);
    }

    public interface FragmentFocusHandler {
        boolean handleFragmentPage(Object fragment, Activity activity, ViewImage root);
    }

    private static HashMap<String, ActivityFocusHandler> activityFocusHandlerMap = new HashMap<>();

    private static HashMap<String, FragmentFocusHandler> fragmentFocusHandlerHashMap = new HashMap<>();

    private static Handler mainLooperHandler = new Handler(Looper.getMainLooper());

    @SuppressLint("StaticFieldLeak")
    private static Activity topActivity = null;

    private static Map<String, Object> topFragmentMaps = new ConcurrentHashMap<>();


    private static int taskDuration = 200;

    private static boolean hasPendingActivityTask = false;

    private static boolean disable = false;

    private static Set<WeakReference<LocalActivityManager>> localActivityManagers = new CopyOnWriteArraySet<>();

    private static Set<WeakReference<Window>> dialogWindowsSets = new CopyOnWriteArraySet<>();

    private static Set<WeakReference<PopupWindow>> popupWindowSets = new CopyOnWriteArraySet<>();

    /**
     * 设置任务时间间隔，这会影响case执行速度
     *
     * @param taskDuration case间隔时间，默认200毫秒，也就是0.2秒
     */
    public static void setTaskDuration(int taskDuration) {
        PageTriggerManager.taskDuration = taskDuration;
    }

    public static void setDisable(boolean disable) {
        PageTriggerManager.disable = disable;
    }

    public static Handler getMainLooperHandler() {
        return mainLooperHandler;
    }

    public static void addHandler(String activityClassName, ActivityFocusHandler activityFocusHandler) {
        activityFocusHandlerMap.put(activityClassName, activityFocusHandler);
    }

    public static void addHandler(String activityClassName, FragmentFocusHandler fragmentFocusHandler) {
        fragmentFocusHandlerHashMap.put(activityClassName, fragmentFocusHandler);
    }

    public static Activity getTopActivity() {
        return topActivity;
    }

    public static Window getTopDialogWindow() {
        for (WeakReference<Window> windowWeakReference : dialogWindowsSets) {
            Window window = windowWeakReference.get();
            if (window == null) {
                dialogWindowsSets.remove(windowWeakReference);
                continue;
            }
            if (window.getDecorView().getVisibility() != View.VISIBLE) {
                continue;
            }
            if (!window.getDecorView().hasWindowFocus()) {
                continue;
            }
            Log.i(SuperAppium.TAG, "get getTopDialogWindow: " + window.peekDecorView().hasWindowFocus());
            return window;
        }
        return null;
    }

    public static View getTopPupWindowView() {
        for (WeakReference<PopupWindow> popupWindowWeakReference : popupWindowSets) {
            PopupWindow popupWindow = popupWindowWeakReference.get();
            if (popupWindow == null) {
                popupWindowSets.remove(popupWindowWeakReference);
                continue;
            }
            View mDecorView = (View) RposedHelpers.getObjectField(popupWindow, "mDecorView");
            if (mDecorView == null) {
                continue;
            }
            if (mDecorView.getVisibility() != View.VISIBLE) {
                continue;
            }
            return mDecorView;
        }
        return null;
    }


    public static View getTopRootView() {
        Activity topActivity = PageTriggerManager.getTopActivity();
        if (topActivity != null) {
            View rootView = topActivity.getWindow().getDecorView();
            if (rootView.getVisibility() == View.VISIBLE) {
                return rootView;
            }
            Log.w(SuperAppium.TAG, "target activity : " + topActivity + " not visible!!");
        }

        Window dialogWindow = PageTriggerManager.getTopDialogWindow();
        if (dialogWindow != null) {
            View rootView = dialogWindow.peekDecorView();
            if (rootView.getVisibility() == View.VISIBLE) {
                return rootView;
            }
        }
        return getTopPupWindowView();

    }


    public static List<Object> getTopFragment() {
        List<Object> ret = new ArrayList<>();
        for (String theFragmentClassName : topFragmentMaps.keySet()) {
            Object topFragment = getTopFragment(theFragmentClassName);
            if (topFragment == null) {
                continue;
            }
            ret.add(topFragment);
        }
        return ret;
    }

    public static Object getTopFragment(String fragmentClassName) {
        Object fragmentObject = topFragmentMaps.get(fragmentClassName);
        if (fragmentObject == null) {
            return null;
        }
        boolean isVisible = (boolean) RposedHelpers.callMethod(fragmentObject, "isVisible");
        if (isVisible) {
            return fragmentObject;
        } else {
            topFragmentMaps.remove(fragmentClassName);
        }
        return null;
    }

    private static void enablePageMonitor() {
        RposedHelpers.findAndHookMethod(Activity.class, "onResume", new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {

                final Activity activity = (Activity) param.thisObject;
                for (WeakReference<LocalActivityManager> localActivityManagerWeakReference : localActivityManagers) {
                    LocalActivityManager localActivityManager = localActivityManagerWeakReference.get();
                    if (localActivityManager == null) {
                        localActivityManagers.remove(localActivityManagerWeakReference);
                        continue;
                    }
                    ArrayList arrayList = (ArrayList) RposedHelpers.getObjectField(localActivityManager, "mActivityArray");
                    for (Object localActivityRecord : arrayList) {
                        Activity localActivityObj = (Activity) RposedHelpers.getObjectField(localActivityRecord, "activity");
                        if (activity.equals(localActivityObj)) {
                            //这个activity 也有焦点，但是他是作为一个组件放到容器里面的，所以不应该被我们监听
                            return;
                        }
                    }
                }

                topActivity = activity;
                Log.i(SuperAppium.TAG, "onWindow resume: " + activity.getClass());
                trigger();
            }
        });

        Class<?> fragmentClass;
        try {

            RC_MethodHook rc_methodHook = new RC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Log.i(SuperAppium.TAG, "onFragment resume: " + param.thisObject.getClass().getName());
                    topFragmentMaps.put(param.thisObject.getClass().getName(), param.thisObject);
                }
            };
            RposedHelpers.findAndHookMethod(Fragment.class, "onResume", rc_methodHook);

            fragmentClass = RatelToolKit.sContext.getClassLoader().loadClass("android.support.v4.app.Fragment");
            RposedHelpers.findAndHookMethod(fragmentClass, "onResume", rc_methodHook);
        } catch (ClassNotFoundException e) {
            //ignore
        }

        //android.app.LocalActivityManager.LocalActivityManager
        RposedHelpers.findAndHookConstructor(LocalActivityManager.class, Activity.class, boolean.class, new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                localActivityManagers.add(new WeakReference<>((LocalActivityManager) param.thisObject));
            }
        });


        //弹窗不被 activity管理
        RposedBridge.hookAllConstructors(Dialog.class, new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Window mWindow = (Window) RposedHelpers.getObjectField(param.thisObject, "mWindow");
                if (mWindow == null) {
                    Log.w(SuperAppium.TAG, "can not get windows object for dialog: " + param.thisObject.getClass().getName());
                    return;
                }
                Log.i(SuperAppium.TAG, "create dialog: " + param.thisObject.getClass().getName());
                dialogWindowsSets.add(new WeakReference<>(mWindow));
            }
        });

        //popupWindow不被activity管理
        RposedHelpers.findAndHookConstructor(PopupWindow.class, View.class, int.class, int.class, boolean.class, new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.i(SuperAppium.TAG, "create PopupWindow: " + param.thisObject.getClass().getName());
                popupWindowSets.add(new WeakReference<>((PopupWindow) param.thisObject));
            }
        });


        //popupWindow不被activity管理
        try {
            RposedHelpers.findAndHookConstructor(PopupWindow.class, Context.class, AttributeSet.class, int.class, int.class, new RC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Log.i(SuperAppium.TAG, "create PopupWindow: " + param.thisObject.getClass().getName());
                    popupWindowSets.add(new WeakReference<>((PopupWindow) param.thisObject));
                }
            });
        } catch (NoSuchMethodError e) {
            //ignore
        }

    }

    public static void trigger(int delay) {
        if (delay <= taskDuration) {
            trigger();
            return;
        }
        mainLooperHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                trigger();
            }
        }, delay);
    }


    public static void trigger() {
        final Activity activity = topActivity;
        if (activity == null) {
            Log.i(SuperAppium.TAG, "no top activity found");
            return;
        }

        final ActivityFocusHandler iActivityHandler = activityFocusHandlerMap.get(activity.getClass().getName());
        if (iActivityHandler != null) {
            if (hasPendingActivityTask) {
                return;
            }
            hasPendingActivityTask = true;
            triggerActivityActive(activity, iActivityHandler, 0);
        }

        for (String theFragmentClassName : topFragmentMaps.keySet()) {
            Object topFragment = getTopFragment(theFragmentClassName);
            if (topFragment == null) {
                continue;
            }
            FragmentFocusHandler fragmentFocusHandler = fragmentFocusHandlerHashMap.get(theFragmentClassName);
            if (fragmentFocusHandler == null) {
                continue;
            }
            triggerFragmentActive(activity, topFragment, fragmentFocusHandler, 0);
        }
    }


    private static void triggerFragmentActive(final Activity activity, final Object fragment, final FragmentFocusHandler fragmentFocusHandler, final int triggerCount) {
        if (disable) {
            Log.i(SuperAppium.TAG, "Page Trigger manager disabled");
            return;
        }
        mainLooperHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!activity.hasWindowFocus()) {
                    return;
                }
                try {
                    if (fragmentFocusHandler.handleFragmentPage(fragment, activity, new ViewImage((View) RposedHelpers.callMethod(fragment, "getView")))) {
                        return;
                    }
                } catch (Throwable throwable) {
                    Log.e(SuperAppium.TAG, "error to handle fragment: " + fragment.getClass().getName(), throwable);
                }
                if (triggerCount > 10) {
                    Log.w(SuperAppium.TAG, "the activity event trigger failed too many times: " + fragmentFocusHandler.getClass());
                    return;
                }
                triggerFragmentActive(activity, fragment, fragmentFocusHandler, triggerCount + 1);
            }
        }, taskDuration);
    }

    private static void triggerActivityActive(final Activity activity, final ActivityFocusHandler activityFocusHandler, final int triggerCount) {
        if (disable) {
            Log.i(SuperAppium.TAG, "Page Trigger manager disabled");
            return;
        }
        mainLooperHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                if (!activity.hasWindowFocus()) {
//                    return;
//                }
                try {
                    Log.i(SuperAppium.TAG, "triggerActivityActive activity: " + activity.getClass().getName() + " for ActivityFocusHandler:" + activityFocusHandler.getClass().getName());
                    hasPendingActivityTask = false;
                    if (activityFocusHandler.handleActivity(activity, new ViewImage(activity.getWindow().getDecorView()))) {
                        return;
                    }
                } catch (Throwable throwable) {
                    Log.e(SuperAppium.TAG, "error to handle activity:" + activity.getClass().getName(), throwable);
                }
                if (triggerCount > 10) {
                    Log.w(SuperAppium.TAG, "the activity event trigger failed too many times: " + activityFocusHandler.getClass());
                    return;
                }
                triggerActivityActive(activity, activityFocusHandler, triggerCount + 1);
            }
        }, taskDuration);
    }

    static {
        enablePageMonitor();
    }
}
