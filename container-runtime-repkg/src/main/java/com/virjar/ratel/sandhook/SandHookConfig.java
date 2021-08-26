package com.virjar.ratel.sandhook;

import android.os.Build;


public class SandHookConfig {

    public volatile static int SDK_INT = Build.VERSION.SDK_INT;
    //Debug status of hook target process
    //public volatile static boolean DEBUG = BuildConfig.DEBUG;
    //Enable compile with jit
    // 8.0以下才需要编译，编译可能需要保证内存充足。stop the world之后如果依赖GC，那么编译可能引发死锁
    // 请注意这个字段是在native层引用的
    public volatile static boolean compiler = SDK_INT < 26;
    public volatile static ClassLoader initClassLoader;
    public volatile static int curUse = 0;
    public volatile static boolean delayHook = true;
}
