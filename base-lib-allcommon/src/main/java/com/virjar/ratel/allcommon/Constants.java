package com.virjar.ratel.allcommon;

public class Constants {

    public static final String ratelPreffix = "ratel_";

    public static final String originAPKFileName = ratelPreffix + "container_origin_apk.apk";
    public static final String xposedBridgeApkFileName = ratelPreffix + "container_xposed_module.apk";
    public static final String containerPatchAPKPath = ratelPreffix + "container-patch.apk";
    public static final String originXposedLoaderAPKPath = ratelPreffix + "xposed-loader-original.apk";
    public static final String whaleXposedLoaderAPKPath = ratelPreffix + "xposed-loader-whale.apk";


    public static final String dexmakerOptFileName = ratelPreffix + "SandHookerNew_opt.jar";
    public static final String dexmakerOptResource = "dex_maker_opt.zip";
    public static final String ratelEngineProperties = ratelPreffix + "engine.properties";

    public static final String rePkgRuntimeAPKPath = "ratel_repkg_runtime.apk";


    public static final String serialNoKey = ratelPreffix + "serialNo";
    public static final String buildTimestampKey = ratelPreffix + "buildTimestamp";

    public static final String serialNoFile = ratelPreffix + "serialNo.txt";
    //public static final String originDexFile = Constants.ratelPreffix + "origin_dex.dex";


    public static final String sandHookCache = "sandHookCache";

    public static final String multiUserIdKey = "multiUserIdKey";
    public static final String multiUserListKey = "multiUserListKey";
    public static final String needRemoveUserListKey = "multiUserNeedRemoveKey";


    public static final String nativeCacheDir = "nativeCache";


    /**
     * 如果原始的apk文件，配置了Application，那么我们需要加载原始的apk
     */
    public static final String APPLICATION_CLASS_NAME = "RATEL_ORIGIN_APPLICATION_CLASS_NAME";

    public static final String ratelLaunchActivityName = "LAUNCH_INTENT_ACTIVITY_NAME";


    public static final String containerMultiDexBootstrapClass = "com.virjar.ratel.container_multi_dex_bootstrap.RatelMultiDexApplication";
    public static final String containerShellEngineBootstrapClass = "com.virjar.ratel.shellengine.ShellEngineEntryApplication";
    public static final String containerShellEngineJumpActivityClass = "com.virjar.ratel.shellengine.ShellJumpActivity";
    public static final String manifestFileName = "AndroidManifest.xml";
    public static final String NATIVE_DIR = "libs";

    public static final String originXposedLoaderDriverClass = "com.kanxue.container.xposed_loader_original.XposedModuleLoader";
    public static final String whaleXposedLoaderDriverClass = "com.kanxue.container.xposed_loader_whale.XposedModuleLoader";

    public static final String containerPatchManagerClass = "com.kanxue.container.container_patch.PatchManager";

    public static final String OPTIMIZE_DIR = "dex";

    public static final boolean COMBINE_RESOURCES = true;
    public static final boolean COMBINE_CLASSLOADER = true;

    public static final String TAG = "RATEL";
    public static final String TAG_PREFIX = TAG + ".";
    public static final String RATEL_WARNING_TAG = "RATEL-Warning";

    public static final String TAG_UNPACK = "unpack";

    public static final String VENV_TAG = "RATEL_VENV";

    public static final String RATEL_NATIVE_LIB_NAME = "ratelnative";
    public static final String RATEL_NATIVE_LIB_32 = "lib" + RATEL_NATIVE_LIB_NAME + "_32.so";
    public static final String RATEL_NATIVE_LIB_64 = "lib" + RATEL_NATIVE_LIB_NAME + "_64.so";

    public static final String xcMethodTransformerWrapperClassName = "com.virjar.container_xc_method_wrapper.XC_MethodWrapper";
    public static final String ratelConfigFileName = "ratelConfig.properties";

    public static final String shellFeatureFileKey = "shellFeatureFileKey";
    public static final String shellName = "shellName";
    public static final String preferHookProviderKey = "preferHookProvider";
    /**
     * 开启这个开关，那么app拿不到真正的硬件数据信息，包括IMEI，serial，cellInfo，location等
     */
    public static final String enableVirtualDevice = "enableVirtualDevice";

//    public static final String switchVirtualDeviceOnStartup = "Startup";
//
//    public static final String switchVirtualDeviceOnReInstall = "ReInstall";
//
//    public static final String switchVirtualDeviceForMultiUser = "Multi";


    public static final String virtualEnvModel = "virtualEnvModel";

    public static final String hasEmbedXposedModuleKey = "hasEmbedXposedModule";
    public static final String xposedModuleApkPackageNameKey = "EmbedXposedModulePackagename";

    // for ratelManager
    public static final String ratelManagerApplicationClass = "com.virjar.ratel.manager.RatelManagerApp";
    public static final String ratelManagerPackage = "com.virjar.ratel.manager";

    // for sdcard env
    public static final String ratelSDCardRoot = "com.virjar.ratel.manager";
    public static final String modulePackageGlobal = "globalModule.txt";
    public static final String modulePackageItem = "ratelModule.txt";
    public static final String backDoorPackageConfigFile = "noneManagerConfig.txt";
    public static final String fakeSignatureDir = "fake_signature";


    public static final String hasShellEngineInstalledKey = "hasShellEngineInstalledKey";


    public static final String debugCertificate = "vnwHAd9N+oY#xuPFGalZBOmb129024d1uP8d7PI#BNLTV58dPY9Q2JpOGGdhQhhXHVS8oN6CbmuSWq2E2yc4Lzrz+FBWiDCPGDkr4FufH7ttcuanrj+a7dc73mu4KbS81DDBcwz4Azg2nNxC85n9YwvPwFQ4CVAB49QGbf8+UKiNh8fW+xxWFAMWlIvaj1YWFQ46IWD+oAaZtOB81xZbmhyy6gBsmY4lk8PZRqXB6cPuRGvb+VpX6mHy+3VsE2OrF5ZQ2+zsHbeEj1uqSOLlCskDeKhumdr5SEThVhkJPLW3DAw6WYTlgaDd+U3Sb0kKnj4jYWwN1aF+pOyNGSU7SaQApvkk8Qg0WuS4bhUa8TZvF5KSQvdXV9==";
    public static final String ratelCertificate = "ratel_certificate";

    public static final String moduleHasNativeCode = "moduleHasNativeCode";

    public static final String ratelPropertiesSuffix = "ratel_properties_";

    public static final String systemPropertiesMock = "systemProperties.properties";
    public static final String telephonyPropertiesMock = "telephonyProperties.properties";
    public static final String settingGlobalMockProperties = "settingGlobal.properties";
    public static final String settingSystemMockProperties = "settingSystem.properties";
    public static final String settingSecureMockProperties = "settingSecure.properties";

    public static final String venvLocationAuto = "vLocationAutoSift";
    public static final String elapsedRealtimeDiffKey = "elapsedRealtimeDiff";
    public static final String uptimeMillisDiffKey = "uptimeMillisDiff";

    public static final String totalMemoryKey = "totalMemory";
    public static final String totalMemoryDiff = "totalMemoryDiff";
    public static final String fileSystemBlockDiff = "fileSystemBlockDiff";

    public static final String disableSwitchEnvForAPI = "disable_switch_env_for_api";

    public static final String dexMakerOptConfigClassId = "Lcom/virjar/ratel/DexMakerOptConfig;";

    public static final String ratelDefaultApkSignatureKey = "hermes_key";

    public static final String android_AppComponentFactoryKey = "android:appComponentFactory";

    public static final String ratelSchedulerFile = "assets/ratel_scheduler.json";

    public static final String METHOD_QUERY_SCHEDULER_TASK = "query_scheduler_task";
    public static final String METHOD_FINISH_SCHEDULER_TASK = "finish_scheduler_task";
    public static final String SCHEDULER_TAG = "RatelScheduler";
    public static final String KEY_RATEL_USER_ID = "ratel.userId";

    public static final String RATEL_MANAGER_PACKAGE = "com.virjar.ratel.manager";


    public static boolean devBuild = true;

    /**
     * ratel 支持的4中一引擎模式，分别为: rebuildDex 、 appendDex 、 shell 、zelda
     */
    public static final String RATEL_ENGINE_ENUM_REBUILD = "rebuildDex";
    public static final String RATEL_ENGINE_ENUM_APPENDEX = "appendDex";
    public static final String RATEL_ENGINE_ENUM_SHELL = "shell";
    public static final String RATEL_ENGINE_ENUM_ZELDA = "zelda";
    /**
     *     //choose ratelEngine
     *     public static final String ratelEngineAppendDex = "appendDex";
     *     public static final String ratelEnginERebuildDex = "rebuildDex";
     *     public static final String ratelEngineShell = "shell";
     */


    /**
     * 脚本联动
     */

    public static final String RATEL_ENGINE_APK = ratelPreffix + "container-driver.apk";

    public static final String RATEL_ENGINE_JAR = ratelPreffix + "container-driver.jar";

    //入口重编模式下，启动植入代码入口
    public static final String bootstrapAPKPath = ratelPreffix + "repkg_bootstrap.apk";
    //appendDex 模式下，启动植入代码入口
    public static final String ratelMultiDexBootstrapApkPath = ratelPreffix + "multi_dex_bootstrap.apk";
    public static final String ratelMultiDexBootstrapJarPath = ratelPreffix + "multi_dex_bootstrap.jar";


    //zelda 模式下，启动植入代码入口
    public static final String ratelZeldaBootstrapApkPath = ratelPreffix + "zelda_bootstrap.apk";

    // 需要对apk进行清理，否则可能植入Android相关class，导致出现问题
    // CaughtExceptionReport: cooperation.qzone.util.exception.QZoneStartupFailException: QzoneCatchedException:java.lang.NoSuchMethodError: No virtual method a(Landroid/arch/lifecycle/Lifecycle$Event;)V in class Landroid/arch/lifecycle/LifecycleRegistry; or its super classes (declaration of 'android.arch.lifecycle.LifecycleRegistry' appears in base.apk!classes11.dex)
    public static final String ratelZeldaBootstrapJarPath = ratelPreffix + "zelda_bootstrap.jar";


    //xposed桥接器，通过他兼容原生Xposed模块，这是apk模式，为gradle打包产生结果
    public static final String xposedApiBridgeAPKFileName = ratelPreffix + "xposedBridge.apk";
    //xposed桥接器，通过他兼容原生Xposed模块，这是jar模式，是ratel优化器经过处理的数据，相比apk模式文件更小，且删除了一些无意义文件
    public static final String xposedApiBridgeJarFileName = ratelPreffix + "xposedBridge.jar";

    public static final String RDP_BIN_JAR_NAME = "RDP-1.0.jar.bin";

    public static final String CONSTANTS_DEFINE_PROPERTIES = "ratelConstants.properties";


    /**
     * zelda模块相关常量定义
     */

    public static final String declaredComponentListConfig = "zelda.declaredComponent.txt";
    public static final String zeldaKeep = "zeldaKeep.";
    public static String KEY_ORIGIN_PKG_NAME = "originPackageName";
    public static String KEY_ORIGIN_APPLICATION_NAME = "originApplicationName";
    public static String KEY_NEW_PKG_NAME = "ZeldaNewPackageName";
    public static String kEY_SUFFER_KEY = "zeldaSufferKey";
    public static final String containerZeldaBootstrapClass = "com.virjar.ratel.zelda.RatelZeldaApplication";
    public static final String KEY_REPLACE_AUTHORITIES = "AUTHORITIES:";
    public static final String KEY_KEEP_CLASS_NAMES = "CLASS_NAME:";
    public static final String KEY_CHILD_PROCESS = "CHILD_PROCESS:";

    /**
     * APK 文件结构常量定义
     */

    public static final String CONSTANTS_RESOURCES_ARSC = "resources.arsc";


    /**
     * 其他
     */
    public static int ratelEngineVersionCode = 21;
    public static String ratelEngineVersion = "1.3.8-SNPSHOT";
    public static final String RATEL_KEY_ratelEngineVersionCode = "ratelEngineVersionCode";
    public static final String RATEL_KEY_ratelEngineVersion = "ratelEngineVersion";
    public static final String RATEL_CONSTANTS_PREFIX = "ratel_constant.";

    /**
     * ratel 服务器相关配置,热发模块功能将会请求服务器
     */
    public static final String DEFAULT_RATEL_SERVER_URL = "https://ratel.virjar.com/";
    public static final String RATEL_KEY_SERVER_URL = "RATEL_SERVER_URL";
    public static final String RATEL_KEY_HOT_MODULE_GROUP = "hot_module_group";

}
