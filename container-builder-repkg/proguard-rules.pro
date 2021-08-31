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

-keepattributes *Annotation*

# 这个暂时加上，测试没问题的时候关闭他
#-keepattributes SourceFile,LineNumberTable

# 貌似大小写会出问题，导致文件名大写，对应class小写
-dontusemixedcaseclassnames

# ratel 框架入口class
-keep  class com.virjar.ratel.builder.ratelentry.Main {
    public static void main( java.lang.String[]);
}

-keep class org.jf.dexlib2.Opcode{*;}

-keep class com.android.apksig.internal.**{*;}

-dontwarn