package com.virjar.ratel.hook.sandcompat;

import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedBridge;


public class RposedAdditionalHookInfo {
    public final RposedBridge.CopyOnWriteSortedSet<RC_MethodHook> callbacks;
    public final Class<?>[] parameterTypes;
    public final Class<?> returnType;

    public RposedAdditionalHookInfo(RposedBridge.CopyOnWriteSortedSet<RC_MethodHook> callbacks, Class<?>[] parameterTypes, Class<?> returnType) {
        this.callbacks = callbacks;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
    }
}
