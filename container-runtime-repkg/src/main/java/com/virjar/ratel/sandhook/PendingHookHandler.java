package com.virjar.ratel.sandhook;

import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.inspect.ClassLoadMonitor;
import com.virjar.ratel.runtime.RatelRuntime;
import com.virjar.ratel.sandhook.wrapper.HookErrorException;
import com.virjar.ratel.sandhook.wrapper.HookWrapper;

import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

// Pending for hook static method
// When Init class error!
public class PendingHookHandler {

    private static Map<Class, Vector<HookWrapper.HookEntity>> pendingHooks = new ConcurrentHashMap<>();

    private static boolean canUsePendingHook;

    static {
        //init native hook
//        if (SandHookConfig.delayHook) {
//            canUsePendingHook = SandHook.initForPendingHook();
//        }
        // pendingHook一定为true
        canUsePendingHook = SandHook.initForPendingHook();
    }

    public static boolean canWork() {
        //return canUsePendingHook && SandHook.canGetObject() && !SandHookConfig.DEBUG;
        return canUsePendingHook && SandHook.canGetObject() && !RatelRuntime.isHostPkgDebug;
        //return canUsePendingHook && SandHook.canGetObject();
    }

    public static synchronized void addPendingHook(HookWrapper.HookEntity hookEntity) {
        Vector<HookWrapper.HookEntity> entities = pendingHooks.get(hookEntity.target.getDeclaringClass());
        if (entities == null) {
            entities = new Vector<>();
            pendingHooks.put(hookEntity.target.getDeclaringClass(), entities);
        }
        entities.add(hookEntity);
    }

    @SuppressWarnings("unused")
    public static void onClassInit(long clazz_ptr) {
        if (clazz_ptr == 0)
            return;
        Class clazz = (Class) SandHook.getObject(clazz_ptr);
        if (clazz == null)
            return;
//        if (RatelRuntime.isRatelDebugBuild) {
//            Log.i(Constants.TAG, "on class Init:" + clazz);
//        }
        ClassLoadMonitor.notifyClassInit(clazz);

        Vector<HookWrapper.HookEntity> entities = pendingHooks.get(clazz);
        if (entities == null)
            return;
        for (HookWrapper.HookEntity entity : entities) {
            if (RatelRuntime.isRatelDebugBuild) {
                Log.w(Constants.TAG, "do pending hook for method: " + entity.target.toString());
            }
            try {
                SandHook.hook(entity);
            } catch (HookErrorException e) {
                Log.w(Constants.TAG, "Pending Hook Error!", e);
            }
        }
        pendingHooks.remove(clazz);
    }

}
