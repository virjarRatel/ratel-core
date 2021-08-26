package com.virjar.ratel.hook.sandcompat.methodgen;

import android.util.Log;

import com.virjar.ratel.sandhook.SandHook;
import com.virjar.ratel.hook.sandcompat.XposedCompat;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

public class ErrorCatch {

    public static Object callOriginError(Member originMethod, Method backupMethod, Object thiz, Object[] args) throws Throwable {
        if (XposedCompat.retryWhenCallOriginError) {
            Log.w("SandHook", "method <" + originMethod.toString() + "> use invoke to call origin!");
            return SandHook.callOriginMethod(originMethod, backupMethod, thiz, args);
        } else {
            return null;
        }
    }

}
