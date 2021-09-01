package com.virjar.ratel.runtime.apibridge;

import android.content.Context;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.HookProvider;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.hook.ContextAttachCollector;
import com.virjar.ratel.hook.sandcompat.XposedCompat;

import java.lang.reflect.Member;
import java.lang.reflect.Method;


public class HookProviderImpl implements HookProvider {
    @Override
    public RC_MethodHook.Unhook hookMethod(Member hookMethod, RC_MethodHook callback) {
        if (ContextAttachCollector.needCollect && (hookMethod.getName().equals("attachBaseContext") || hookMethod.getName().equals("attach"))
                && (hookMethod instanceof Method)
                && Context.class.isAssignableFrom(hookMethod.getDeclaringClass())
                && ((Method) hookMethod).getParameterTypes().length == 1
                && ((Method) hookMethod).getParameterTypes()[0] == Context.class
        ) {
            ContextAttachCollector.collect((Method) hookMethod, callback);
        }

        return XposedCompat.hookMethod(hookMethod, callback);

    }

    @Override
    public void unhookMethod(Member method, RC_MethodHook callback) {
        Log.w(Constants.TAG, "unsupported operation for call  UnHookMethod");
    }

    @Override
    public Object invokeOriginalMethod(Member method, Object thisObject, Object[] args) throws Throwable {
        //  return ProviderConfig.getHookProvider().invokeOriginalMethod(method, thisObject, args);
        return XposedCompat.invokeOriginalMethod(method, thisObject, args);
    }
}
