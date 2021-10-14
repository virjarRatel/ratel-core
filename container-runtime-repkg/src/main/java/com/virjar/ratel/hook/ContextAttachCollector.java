package com.virjar.ratel.hook;

import android.content.Context;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.runtime.RatelRuntime;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ContextAttachCollector {
    private static Map<Method, Set<RC_MethodHook>> runtimeXCMethodHook = new HashMap<>();

    public static boolean needCollect = true;

    public static void collect(Method method, RC_MethodHook xc_methodHook) {
        Set<RC_MethodHook> xc_methodHooks = runtimeXCMethodHook.get(method);
        if (xc_methodHooks == null) {
            xc_methodHooks = new HashSet<>();
            runtimeXCMethodHook.put(method, xc_methodHooks);
        }
        xc_methodHooks.add(xc_methodHook);
    }


    public static void fireContextAttach() {
        needCollect = false;
        Context paramContext = RatelRuntime.getOriginContext();
        Context thisContext = RatelRuntime.entryContext;
        if (thisContext == null) {
            // append multi dex 和shell 模式下，不需要主动attach，因为delegate application在框架加载之后才会构造
            runtimeXCMethodHook.clear();
            return;
        }
        // the callback in ratel env
        Set<Method> methods = runtimeXCMethodHook.keySet();
        for (Method method : methods) {
            if (!method.getDeclaringClass().isAssignableFrom(thisContext.getClass())) {
                continue;
            }
            Set<RC_MethodHook> xc_methodHooks = runtimeXCMethodHook.get(method);
            if (xc_methodHooks == null) {
                continue;
            }
            for (RC_MethodHook xc_methodHook : xc_methodHooks) {

                RC_MethodHook.MethodHookParam methodHookParam = new RC_MethodHook.MethodHookParam();
                methodHookParam.method = method;
                methodHookParam.args = new Object[]{paramContext};
                methodHookParam.thisObject = thisContext;

                try {
                    xc_methodHook.callBeforeHookedMethod(methodHookParam);
                } catch (Throwable throwable) {
                    Log.e(Constants.TAG, "callback error", throwable);
                }

                try {
                    xc_methodHook.callAfterHookedMethod(methodHookParam);
                } catch (Throwable throwable) {
                    Log.e(Constants.TAG, "callback error", throwable);
                }
            }
        }
    }
}
