package com.virjar.ratel.manager.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DefaultSharedPreferenceHolder {
    private SharedPreferences mPref;

    private DefaultSharedPreferenceHolder(Context context) {
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static DefaultSharedPreferenceHolder instance = null;

    public static DefaultSharedPreferenceHolder getInstance(Context context) {
        if (instance != null) {
            return instance;
        }
        synchronized (DefaultSharedPreferenceHolder.class) {
            if (instance != null) {
                return instance;
            }
            instance = new DefaultSharedPreferenceHolder(context);
        }
        return instance;
    }

    public SharedPreferences getPreferences() {
        return mPref;
    }

    private static final String switchKey = "totalSwitchKey";

    public void totalSwitch(Boolean enabled) {
        if (enabled == null) {
            enabled = false;
        }
        getPreferences().edit().putBoolean(switchKey, enabled).apply();
    }

    public boolean isRatelSwitchOn() {
        //TODO 默认不是true??
        if (!getPreferences().contains(switchKey)) {
            totalSwitch(true);
        }
        return getPreferences().getBoolean(switchKey, true);
    }
}
