# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 这个暂时加上，测试没问题的时候关闭他
#-keepattributes SourceFile,LineNumberTable

# lody的反射工具类，使用名称反射的方式。需要keep
-keepclassmembernames class mirror.** {
    public static <fields>;
}

-keepclassmembers class mirror.*{*;}

# xposed作为插件API，需要keep
-keep class de.robv.android.xposed.**{*;}

-keep class external.org.apache.commons.**{*;}


# 所有的native method都需要keep，混淆框架默认也会keep被使用到的method，但是没有被引用的代码将会被remove。这可能导致native加载失败
#  art/runtime/java_vm_ext.cc:410] JNI DETECTED ERROR IN APPLICATION: JNI NewGlobalRef called with pending exception java.lang.NoSuchMethodError: no static or non-static method "Lcom/virjar/base_lib_ratel_native/RatelNative;.nativeReverseRedirectedPath(Ljava/lang/String;)Ljava/lang/String;"
#-keepclasseswithmembernames class ** {
#    native <methods>;
#}

-keep class android.content.pm.**{*;}


-keep class com.virjar.ratel.api.**{*;}
#-keep class com.virjar.ratel.hook.**{*;}
-keep class com.virjar.ratel.hook.sandcompat.hookstub.MethodHookerStubs32{*;}
-keep class com.virjar.ratel.hook.sandcompat.hookstub.MethodHookerStubs64{*;}
-keep class com.virjar.ratel.hook.sandcompat.hookstub.HookStubManager{*;}
-keep class com.virjar.ratel.hook.sandcompat.RposedAdditionalHookInfo{*;}
-keep class com.virjar.ratel.hook.sandcompat.utils.DexLog{*;}

#-keep class com.virjar.ratel.runtime.**{*;}
-keep  class com.virjar.ratel.runtime.RatelRuntime {
    # rebuild dex 模式入口
    public static void startUp(android.content.Context,android.content.Context,boolean);
    # append multi dex模式的启动入口
    public static void applicationAttachWithMultiDexMode(android.content.Context);
    public static void applicationOnCreateWithMultiMode();
    #shell engine入口可以被混淆，所以不修腰单独排除
    # zelda入口
    public static void applicationAttachWithZeldaEngine(android.content.Context);

    # kratos引擎入口
    public static void applicationAttachWithKratosEngine(android.content.Context,android.content.Context);

    # 此乃手动混淆，我们在native层会访问h对象，
    private static android.content.pm.PackageInfo h;
}

#-keep class com.virjar.ratel.envmock.**{*;}
-keep class com.virjar.ratel.envmock.EnvMockController{
    #看起来是代码优化框架的bug，如果混淆了这个函数，部分手机上无法正常打开手机百度app
    public static void initEnvModel();
    public static void switchEnvIfNeed(android.content.Context);
}

#-keep class com.virjar.ratel.sandhook.**{*;}
-keepclassmembers class com.virjar.ratel.sandhook.SandHook{
    native <methods>;
    public static int testAccessFlag;
    public static long getThreadId();
}
-keepclassmembers class com.virjar.ratel.sandhook.ClassNeverCall{*;}
-keepclassmembers class com.virjar.ratel.sandhook.ArtMethodSizeTest{*;}
-keepclassmembers class com.virjar.ratel.sandhook.SandHookMethodResolver{*;}
-keepclassmembers class com.virjar.ratel.sandhook.wrapper.BackupMethodStubs{*;}
-keepclassmembers class com.virjar.ratel.sandhook.SandHookConfig{
    public volatile static boolean compiler;
}
-keepclassmembers class com.virjar.ratel.sandhook.PendingHookHandler{
    public static void onClassInit(long);
}

#-keep class com.virjar.ratel.utils.**{*;}
-keep class com.virjar.ratel.utils.HiddenApiBypass$*{*;}

#-keep class com.virjar.ratel.manager.**{*;}
-keep class com.virjar.ratel.manager.bridge.**{*;}

#-keep class com.virjar.ratel.allcommon.**{*;}
#-keep class com.virjar.ratel.shellengine.**{*;}
-keep class com.virjar.ratel.shellengine.ShellEngineEntryApplication{*;}
-keep class com.virjar.ratel.shellengine.ShellJumpActivity{*;}

# test
#-keep class com.virjar.ratel.core.**{*;}
-keep class com.virjar.ratel.nativehide.**{*;}

-keep class com.virjar.ratel.NativeBridge{*;}
-keepclassmembers class com.virjar.ratel.RatelNative{*;}

-dontwarn
-dontnote
-ignorewarnings

# 开源环境，我们不做混淆
-dontobfuscate


# 不能优化代码，sandhook有一些空调用。优化后代码可能被remove
# todo 这可以后续处理
-dontoptimize
