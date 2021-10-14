package com.virjar.ratel.inject.template;

import android.util.Log;

public class EventNotifier {

    private static final String TAG = "RATEL_EVENT";

    public static void send(String str1, String str2) {
        Log.i(TAG, "str1: " + str1 + " str2:" + str2);
    }

    public static void send(String str1, String str2, String str3) {
        Log.i(TAG, "str1: " + str1 + " str2:" + str2 + "  str3:" + str3);
    }

    public static void send(String str1, String str2, String str3, String str4) {
        Log.i(TAG, "str1: " + str1 + " str2:" + str2 + "  str3:" + str3 + " str4:" + str4);
    }

}
