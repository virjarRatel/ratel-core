package com.virjar.ratel.runtime.fixer;

import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.runtime.RatelRuntime;

public class StorageManagerFixer {
    public static void relocateStorageManager() {
        Class<?> storageManageStubProxyClass = RposedHelpers.findClassIfExists("android.os.storage.IStorageManager$Stub$Proxy", ClassLoader.getSystemClassLoader());
        if (storageManageStubProxyClass == null) {
            return;
        }
        // public int mkdirs(java.lang.String callingPkg, java.lang.String path) throws android.os.RemoteException
        RposedBridge.hookAllMethods(storageManageStubProxyClass, "mkdirs", new RC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                for (int i = 0; i < param.args.length; i++) {
                    if (param.args[i] instanceof String) {
                        String str = (String) param.args[i];
                        if (str.contains(RatelRuntime.originPackageName)) {
                            param.args[i] = str.replace(RatelRuntime.originPackageName, RatelRuntime.nowPackageName);
                        }
                    }
                }
            }
        });
    }
}
