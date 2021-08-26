package com.virjar.ratel.sandhook.wrapper;

import com.virjar.ratel.sandhook.SandHook;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

public class HookWrapper {


    public static class HookEntity {

        public Member target;
        public Method hook;
        public Method backup;

        public boolean hookIsStub = false;
        public boolean resolveDexCache = true;
        public boolean backupIsStub = true;

        public Class[] pars;
        public int hookMode;

        public HookEntity(Member target, Method hook, Method backup) {
            this.target = target;
            this.hook = hook;
            this.backup = backup;
        }

        public HookEntity(Member target, Method hook, Method backup, boolean resolveDexCache) {
            this.target = target;
            this.hook = hook;
            this.backup = backup;
            this.resolveDexCache = resolveDexCache;
        }

        public boolean isCtor() {
            return target instanceof Constructor;
        }

        public Object callOrigin(Object thiz, Object... args) throws Throwable {
            return SandHook.callOriginMethod(backupIsStub, target, backup, thiz, args);
        }
    }

}
