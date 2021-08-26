package com.virjar.ratel.hook.sandcompat.methodgen;

import com.virjar.ratel.hook.sandcompat.RposedAdditionalHookInfo;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

public interface HookMaker {
    void start(Member member, RposedAdditionalHookInfo hookInfo,
               ClassLoader appClassLoader, String dexDirPath) throws Exception;
    Method getHookMethod();
    Method getBackupMethod();
    Method getCallBackupMethod();
}
