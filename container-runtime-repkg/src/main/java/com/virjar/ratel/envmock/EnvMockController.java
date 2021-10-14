package com.virjar.ratel.envmock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.RatelNative;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.VirtualEnv;
import com.virjar.ratel.api.inspect.Lists;
import com.virjar.ratel.envmock.binder.BinderFingerHookManager;
import com.virjar.ratel.envmock.providerhook.ContentProviderFake;
import com.virjar.ratel.runtime.MultiUserManager;
import com.virjar.ratel.runtime.RatelConfig;
import com.virjar.ratel.runtime.RatelEnvironment;
import com.virjar.ratel.runtime.RatelRuntime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnvMockController {

    private static VirtualEnv.VirtualEnvModel virtualEnvModel = null;

    public static VirtualEnv.VirtualEnvModel getVirtualEnvModel() {
        return virtualEnvModel;
    }

    public static void initEnvModel() {
        String virualEnvModelStr = RatelConfig.getConfig(Constants.virtualEnvModel);
        if (TextUtils.isEmpty(virualEnvModelStr) || virualEnvModelStr.equalsIgnoreCase(VirtualEnv.VirtualEnvModel.DISABLE.name())) {
            virtualEnvModel = VirtualEnv.VirtualEnvModel.DISABLE;
        } else if (virualEnvModelStr.equalsIgnoreCase(VirtualEnv.VirtualEnvModel.START_UP.name())) {
            virtualEnvModel = VirtualEnv.VirtualEnvModel.START_UP;
        } else if (virualEnvModelStr.equalsIgnoreCase(VirtualEnv.VirtualEnvModel.INSTALL.name())) {
            virtualEnvModel = VirtualEnv.VirtualEnvModel.INSTALL;
        } else if (virualEnvModelStr.equalsIgnoreCase(VirtualEnv.VirtualEnvModel.MULTI.name())) {
            virtualEnvModel = VirtualEnv.VirtualEnvModel.MULTI;
        } else {
            virtualEnvModel = VirtualEnv.VirtualEnvModel.DISABLE;
        }

        if (virtualEnvModel == VirtualEnv.VirtualEnvModel.MULTI) {
            String multiUserId = RatelConfig.getConfig(Constants.multiUserIdKey);
            MultiUserManager.cleanRemovedUser();
            MultiUserManager.setMockUserId(multiUserId);
        }
    }

    public static void switchEnvIfNeed(Context context) throws Exception {
        if (virtualEnvModel == VirtualEnv.VirtualEnvModel.DISABLE && !RatelRuntime.isZeldaEngine()) {
            return;
        }
        if (RatelRuntime.isRatelDebugBuild) {
            Log.i(Constants.TAG, "enable virtual device environment");
        }

        judgeAndReplaceDeviceEnv(context);


        //请注意，fakeDevice包含sdcard重定向逻辑，多用户模式下存在多个sdcard备份，此时需要保证sdcard重定向逻辑在virtualEnvModel初始化之后执行
        fakeDevice2(context);
    }

    /**
     * 替换设备信息，在虚拟环境开关开启之后才会生效，ratel framework自带的环境替换功能比较粗暴。作用方式是删除所有运行时文件。
     *
     * @param context 上下文
     */
    private static void judgeAndReplaceDeviceEnv(Context context) throws IOException {
        // multi user 模式下，不存在文件删除，但是需要在内存中配置数据。所以对所有进程都需要配置
        if (virtualEnvModel == VirtualEnv.VirtualEnvModel.MULTI) {
            if (RatelRuntime.isZeldaEngine()) {
                Log.w(Constants.TAG, "zelda本身即为多开引擎，无需配置为multi模式,Ratel早期MULTI模式和Zelda引擎冲突");
            } else {
                handleMultiUserEnv(context);
            }
            return;
        }
        //其他模式下，需要删除数据，这个时候只针对于主进程实现
        if (!RatelRuntime.isMainProcess) {
            return;
        }

        if (virtualEnvModel == VirtualEnv.VirtualEnvModel.START_UP) {
            //startUp模式下，一定进行数据删除
            handleSingleUserEnv(context);
        }
    }

    @SuppressLint("SdCardPath")
    private static void handleMultiUserEnv(Context context) throws IOException {
        //now add all file redirect

        List<File> files = RatelEnvironment.ratelWhiteFiles();
        Set<String> whiteDir = new HashSet<>();
        for (File file : files) {
            whiteDir.add(file.getName());
        }
        whiteDir.add("lib");

        //handle /data/data
        String dataDataPath = "/data/data/" + RatelRuntime.nowPackageName + "/";
        for (String str : whiteDir) {
            RatelNative.whitelist(dataDataPath + str);
        }
        RatelNative.redirectDirectory(dataDataPath, RatelEnvironment.envMockData().getCanonicalPath());

        //handle /data/user/0
        String dataUser0Path = "/data/user/0/" + RatelRuntime.nowPackageName + "/";
        for (String str : whiteDir) {
            RatelNative.whitelist(dataUser0Path + str);
        }

        RatelNative.redirectDirectory(dataUser0Path, RatelEnvironment.envMockData().getCanonicalPath());


        //handle /data/user_de/0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String dataUserDe0Path = "/data/user_de/0/";
            for (String str : whiteDir) {
                RatelNative.whitelist(dataUserDe0Path + str);
            }
            //RatelNative.redirectDirectory(dataUserDe0Path, FileManager.DeMockDir(context).getCanonicalPath());
            RatelNative.redirectDirectory(dataUserDe0Path, RatelEnvironment.envMockData_de().getCanonicalPath());
        }

        // redirect /tmp
        //RatelNative.redirectDirectory("/tmp/", FileManager.tmpMockDir(context).getCanonicalPath());
        RatelNative.redirectDirectory("/tmp/", RatelEnvironment.envMockTmp().getCanonicalPath());

        //TODO sdk_26之前，可以访问 /proc/stat 需要伪造
        //以下硬件信息需要伪造
        //TODO /sys/class/net/wlan0/address
        //TODO /sys/class/net/eth0/address
        //TODO /sys/class/net/wifi/address
        RatelEnvironment.ensureEnvMockSubDirs();

        for (String envMockBackDir : dataDataPath(RatelEnvironment.envMockBaseDir().getCanonicalPath())) {
            RatelNative.whitelist(envMockBackDir);
        }

        for (String envMockDir : dataDataPath(RatelEnvironment.envMockDir().getCanonicalPath())) {
            //这个目录不允许访问 https://bbs.pediy.com/thread-255212.htm
            RatelNative.forbid(envMockDir, true);
        }

        File unpackDir = new File(RatelToolKit.sContext.getFilesDir(), "ratel_unpack");
        RatelNative.whitelist(unpackDir.getAbsolutePath());

        // RatelNative.whitelist("/data/user/0/com.csair.mbp/.vCache/");
        if (RatelRuntime.nowPackageName.equals("com.csair.mbp")) {
            //TODO 这是南航加壳的目录，这个目录重定向会导致南航无限重启，VA一样的表现，具体愿意不明
            for (String czCacheDir : dataDataPath("/data/user/0/com.csair.mbp/.vCache/")) {
                RatelNative.whitelist(czCacheDir);
            }
        }
    }

    private static List<String> dataDataPath(String inputPath) {
        if (inputPath == null) {
            return Collections.emptyList();
        }

        int index = inputPath.indexOf(RatelRuntime.nowPackageName);
        if (index < 0) {
            return Lists.newArrayList(inputPath);
        }

        if (!inputPath.startsWith("/data/")) {
            return Lists.newArrayList(inputPath);
        }
        String content = inputPath.substring(index);
        return Lists.newArrayList("/data/data/" + content,
                "/data/user/0/" + content,
                "/data/user_de/0/" + content);
    }

    public static void handleSingleUserEnv(Context context) throws IOException {
        if (RatelRuntime.isRatelDebugBuild) {
            Log.i(Constants.TAG, "replace device env...");
        }
        File scanRootDir = context.getFilesDir().getParentFile();
        //需要keep
        File ratelRuntimeRootDir = context.getDir("ratel_runtime", Context.MODE_PRIVATE);
        //TODO
        File ratelResourceRootDir = RatelEnvironment.ratelResourceDir();
        List<File> keepFiles = new ArrayList<File>();
        keepFiles.add(ratelRuntimeRootDir);
        keepFiles.add(ratelResourceRootDir);

        List<String> keepFilesStrs = canonicalizeKeepFiles(keepFiles);
        doEnvReplaceClean(scanRootDir, keepFilesStrs);
    }


    private static List<String> canonicalizeKeepFiles(List<File> files) throws IOException {
        Set<String> ret = new HashSet<>();
        for (File file : files) {
            String canonicalPath = file.getCanonicalPath();
            if (file.isDirectory() && !canonicalPath.endsWith("/")) {
                canonicalPath = canonicalPath + "/";
            }
            ret.add(canonicalPath);
        }

        List<String> realFiles = new ArrayList<>(ret.size());
        realFiles.addAll(ret);
        return realFiles;
    }


    private static void doEnvReplaceClean(File rootDir, List<String> keepFiles) throws IOException {
        String canonicalPath = rootDir.getCanonicalPath();
        if (rootDir.isDirectory() && !canonicalPath.endsWith("/")) {
            canonicalPath = canonicalPath + "/";
        }
        boolean judgeSubDir = false;
        for (String keepPath : keepFiles) {
            if (canonicalPath.startsWith(keepPath)) {
                return;
            }
            if (!judgeSubDir && keepPath.startsWith(canonicalPath)) {
                judgeSubDir = true;
            }
        }

        if (!judgeSubDir) {
            deleteDir(rootDir);
            return;
        }

        if (rootDir.isDirectory()) {
            File[] files = rootDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    doEnvReplaceClean(file, keepFiles);
                }
            }
        } else {
            deleteDir(rootDir);
        }
    }

    private static boolean deleteDir(File dir) {
        if (!dir.exists()) {
            return true;
        }
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String file : children) {
                boolean success = deleteDir(new File(dir, file));
                if (!success) {
                    return false;
                }
            }
        }

        Log.i(Constants.VENV_TAG, "remove file: " + dir.getAbsolutePath());
        return dir.delete();
    }


    private static void fakeDevice2(final Context context) throws Exception {
        // 1
        SystemPropertiesFake.fakeSystemProperties();

        // 2
        TelephonyManagerFake.fakeTelephony();

        // 3
        // fake location, cell info etc
        LocationFake.fakeLocation();

        // 4
        //不允许app访问内存卡，如果要访问，那么将它重定向到自己的私有目录，避免程序卸载重装数据仍然没有被删除。避免app使用sdcard存储硬件信息。避免apk相互check，比如美团读取点评的私有目录
        SdcardFake.fakeFileSystem(context);

        // 5
        //fake settings and mock android_id,
        //这个类似注册表，可以往这里存放数据，即使apk被卸载重装，数据也不会被清空。所以我们这里把它给mock一下，让数据全部存储在自己的目录空间，这样卸载之后，数据将会被清空
        //避免app通过这个方式存储硬件id做防抓判断
        SettingsFake.fakeSettings();

        // 6
        //TODO 这个增加了就会变成 FP_CRITICAL
        //隐藏vpn
        new NetworkMockController().doMock();

        // 7
        //隐藏Wi-Fi的BSSID

        WifiXSSIDFakeV2.fakeWifi();

        //8
        // 系统的各种时间，毫秒值，需要增加抖动
        TimeFake.fakeTime();

        // 9
        //内存大小，磁盘大小，sdcard大小
        SizeFake.fakeSize();

        // 10
        //一些linux相关的特征文件
        LinuxFileFake.fakeLinuxFeatureFile();

        // 11
        //拦截contentProvider
        ContentProviderFake.fakeContentProvider();

        // 12
        //不同版本的序列号策略不一致，单独抽取一个类来实现
        SerialFake.fakeSerial();

        // 13
        MSAFake.doFake();

        // 14
        BinderFingerHookManager.doMockBinder();

        // 15
        MediaDrmFake.fakeDeviceId();
    }
}
