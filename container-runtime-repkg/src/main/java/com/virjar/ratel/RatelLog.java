package com.virjar.ratel;

import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.runtime.RatelRuntime;


public class RatelLog {

    public static final String TAG = Constants.TAG;

    public static boolean DEBUG = RatelRuntime.isRatelDebugBuild;

    public static int v(String s) {
        return Log.v(TAG, s);
    }

    public static int i(String s) {
        return Log.i(TAG, s);
    }

    public static int d(String s) {
        return Log.d(TAG, s);
    }

    public static int w(String s) {
        return Log.w(TAG, s);
    }

    public static int e(String s) {
        return Log.e(TAG, s);
    }

    public static int e(String s, Throwable t) {
        return Log.e(TAG, s, t);
    }


}
