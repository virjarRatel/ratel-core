package com.virjar.ratel.allcommon;


/**
 * 构建环境，对于他的修改会被直接打入到各个阶段的字节码当中，
 */
public class BuildEnv {
    // will be false on release build
    public static boolean DEBUG = true;
    /**
     * 当被转换到dex的时候，这个flag将会被设置为true
     */
    public static boolean ANDROID_ENV = false;
    public static long buildTimestamp = System.currentTimeMillis();


    public static int engineVersionCode = 35;
    public static String engineVersion = "2.0.0-SNAPSHOT";


}
