package com.virjar.ratel.inject.template;

import android.util.Log;

public class RatelSmaliLog {
    private static String TAG = "RATEL_SAMLI";

    public static void resetTag(String tag) {
        TAG = tag;
    }

    public static void log(String message) {
        Log.i(TAG, message, new Throwable());
    }

    public static void printStack() {
        Log.i(TAG, "stackTrace", new Throwable());
    }

    public static void logObject(Object o) {
        if (o == null) {
            return;
        }
        Log.i(TAG, "logObject class: " + o.getClass() + " objectToString: " + o);
        Log.i(TAG, "stackTrace", new Throwable());
    }
}
