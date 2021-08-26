package com.virjar.ratel.utils;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.ArrayMap;

import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.runtime.RatelRuntime;

public class MainThreadRunner {
    public static void runOnFocusActivity(FocusActivityOccurEvent focusActivityOccurEvent) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            findAndFire(focusActivityOccurEvent);
        } else {
            new Handler(Looper.getMainLooper()).post(() -> findAndFire(focusActivityOccurEvent));
        }
    }

    public interface FocusActivityOccurEvent {
        boolean onFocusActivityOccur(Activity activity);

        void onActivityEmpty();

        boolean onLostFocus();
    }

    private static void findAndFire(FocusActivityOccurEvent focusActivityOccurEvent) {
        ArrayMap mActivities = (ArrayMap) RposedHelpers.getObjectField(RatelRuntime.mainThread, "mActivities");
        Activity topActivity = null;

        if (mActivities.values().size() == 0) {
            focusActivityOccurEvent.onActivityEmpty();
        }
        for (Object activityClientRecord : mActivities.values()) {
            Activity tempActivity = (Activity) RposedHelpers.getObjectField(activityClientRecord, "activity");
            if (tempActivity.hasWindowFocus()) {
                topActivity = tempActivity;
                break;
            }
        }

        if (topActivity == null) {
            if (!focusActivityOccurEvent.onLostFocus()) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> findAndFire(focusActivityOccurEvent), 1000);
            }
            return;
        }
        if (focusActivityOccurEvent.onFocusActivityOccur(topActivity)) {
            new Handler(Looper.getMainLooper()).post(() -> findAndFire(focusActivityOccurEvent));
        }
    }
}
