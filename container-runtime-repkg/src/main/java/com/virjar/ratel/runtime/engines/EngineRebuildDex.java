package com.virjar.ratel.runtime.engines;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.RatelEngine;
import com.virjar.ratel.core.runtime.BuildConfig;
import com.virjar.ratel.runtime.RatelRuntime;
import com.virjar.ratel.sandhook.SandHookConfig;
import com.virjar.ratel.utils.HiddenAPIEnforcementPolicyUtils;

public class EngineRebuildDex {
    public static void startUp(Context applicationContext, Context context, boolean bypassHiddenAPIEnforcementPolicy) throws Throwable {
        RatelRuntime.ratelEngine = RatelEngine.REBUILD_DEX;
        if (BuildConfig.DEBUG) {
            Log.i(Constants.TAG, "dex rebuild entry");
        }
        if (bypassHiddenAPIEnforcementPolicy && SandHookConfig.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenAPIEnforcementPolicyUtils.passApiCheck();
        }
        try {
            RatelRuntime.init(applicationContext, context, context);
            RatelRuntime.ratelStartUp(context);
        } catch (Throwable throwable) {
            Log.e(Constants.TAG, "ratel engine startup error", throwable);
            throw throwable;
        }

        if (BuildConfig.DEBUG) {
            Log.i(Constants.TAG, "ratel framework startup finished!!");
        }
    }
}
