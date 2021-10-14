package com.virjar.ratel.api;

import com.virjar.ratel.api.rposed.RC_MethodHook;

import java.lang.reflect.Member;


public interface HookProvider {

    RC_MethodHook.Unhook hookMethod(Member method, RC_MethodHook callback);

    void unhookMethod(Member method, RC_MethodHook callback);

    Object invokeOriginalMethod(Member method,
                                Object thisObject, Object[] args) throws Throwable;
}
