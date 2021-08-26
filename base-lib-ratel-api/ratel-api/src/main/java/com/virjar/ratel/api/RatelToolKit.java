package com.virjar.ratel.api;

import android.annotation.SuppressLint;
import android.content.Context;

import com.virjar.ratel.api.hint.PostInited;
import com.virjar.ratel.api.hint.RatelEngineHistory;
import com.virjar.ratel.api.hint.RatelEngineVersion;
import com.virjar.ratel.api.providers.ContentProviderFakeRegister;
import com.virjar.ratel.api.scheduler.SchedulerTaskBeanHandler;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class RatelToolKit {
    /**
     * 1。 以下对象，是暴露给调用方的额外API，可以通过他们操作ratel提供的额外功能（除开xposed本身功能之外）
     */
    //全局的一个context，context是调用Android系统功能的重要对象。有这个对象之后，无需手动通过拦截attach的方式获取context
    @SuppressLint("StaticFieldLeak")
    public static Context sContext = null;

    /**
     * 宿主apk的classLoader，可以替代llparma
     */
    public static ClassLoader hostClassLoader = null;
    /**
     * ratel框架的配置信息，代表了ratel编码、打包、运行过程产生的一些特定flag
     */
    public static RatelConfig ratelConfig = null;

    /**
     * ratel支持对文件进行重定向
     */
    public static IORelocator ioRelocator = null;

    /**
     * 当前进程名称
     */
    public static String processName = null;

    /**
     * 当成packageName
     */
    public static String packageName = null;


    public static String ratelVersionName = null;

    public static int ratelVersionCode = 0;

    /**
     * 用户ID，可以作为一个设备ID，MULTI模式下和nowUser相等。其他模式可能为随机值，但是除非设备被删除，该ID不会改变
     * 请注意他是PostInited的
     */
    @PostInited
    public static String userIdentifier;

    /**
     * 也是用户ID，不过转化为一个long类型，方便在ID空间震荡算法中作为随机数起点种子
     * 请注意他是PostInited的<br>
     * 另外请注意这个字段废弃了，请使用int类型的
     *
     * @see RatelToolKit#userIdentifierSeedInt
     */
    @PostInited
    @Deprecated
    public static long userIdentifierSeed;

    /**
     * 也是用户ID，不过转化为一个int类型，方便在ID空间震荡算法中作为随机数起点种子
     * 请注意他是PostInited的
     */
    @PostInited
    public static int userIdentifierSeedInt;

    /**
     * 平头哥的手机ID，来自RM管理端的设置页面
     */
    public static String ratelPhoneIdentifier;
    /**
     * 虚拟化环境功能支持
     */
    public static VirtualEnv virtualEnv = null;

    /**
     * 指纹fake接口
     */
    public static FingerPrintModel fingerPrintModel = null;


    /**
     * contentProvider模拟接口
     */
    public static ContentProviderFakeRegister contentProviderFakeRegister = null;

    /**
     * 基于dexmaker的动态代理支持
     */
    public static DexMakerProxyBuilderHelper dexMakerProxyBuilderHelper = null;

    /**
     * 给调度任务使用的，用户操作调度任务状态
     */
    public static SchedulerTaskBeanHandler schedulerTaskBeanHandler = null;

    /**
     * 提供主动访问模块布局资源的能力
     */
    public static RatelResourceInterface ratelResourceInterface = null;

    /**
     * 设置为true之后，框架将会自动检测apk处于ANR态，并且ANR态将会自杀程序，避免apk卡屏
     */
    public static boolean killAppIfDetectANR = false;

    public static RatelUnpack ratelUnpack = null;

    /**
     * native层相关的工具类，方便进行so相关逻辑逆向
     */
    public static NativeHelper nativeHelper = null;

    /**
     * 当前app运行在那个引擎下
     */
    @RatelEngineVersion(RatelEngineHistory.V_1_3_9)
    public static RatelEngine ratelEngine = RatelEngine.APPEND_DEX;

    /**
     * 虚拟化环境下，sdcard将会被隔离，导致无法往sdcard写入数据。但是如果ratel模块期望通过sdcard和其他app交换数据，那么需要通过一个sdcard白名单进行放行<br>
     * 该路径规则为：  /sdcard/ratel_white_dir/packageName/ <br>
     * 如： /sdcard/ratel_white_dir/com.kanxue.container.demoapp <br>
     * 比如一般来说，通过文件实现多账户身份切换，指定文件为: /sdcard/ratel_white_dir/com.kanxue.container.demoapp/userId.txt
     */
    public static String whiteSdcardDirPath = null;
    /**
     * @hidden
     */
    public static Set<RatalStartUpCallback> ratalStartUpCallbackSet = new CopyOnWriteArraySet<>();

    public static Set<RatelEngineUpgradeEvent> ratelEngineUpgradeEventSet = new CopyOnWriteArraySet<>();


    @Deprecated
    public static void setOnRatelStartUpCallback(RatalStartUpCallback ratelStartUpCallback) {
        addOnRatelStartUpCallback(ratelStartUpCallback);
    }

    public static void addOnRatelStartUpCallback(RatalStartUpCallback ratalStartUpCallback) {
        ratalStartUpCallbackSet.add(ratalStartUpCallback);
    }

    public static void addOnEngineUpgradeListener(RatelEngineUpgradeEvent ratelEngineUpgradeEvent) {
        ratelEngineUpgradeEventSet.add(ratelEngineUpgradeEvent);
    }


    public static ProcessUtils processUtils = null;

    /**
     * 2。 以下以下对象，是用户层不需要关心的。我也不会做解释
     */
    public static HookProvider usedHookProvider;

    public static String TAG = null;


    /**
     * 设置系统代理
     *
     * @param host host
     * @param port port
     */
    public static void setupHttpProxy(String host, int port) {
        Properties prop = System.getProperties();
        prop.setProperty("http.proxyHost", host);
        prop.setProperty("http.proxyPort", String.valueOf(port));

        prop.setProperty("https.proxyHost", host);
        prop.setProperty("https.proxyPort", String.valueOf(port));
    }
}
