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


        //????????????fakeDevice??????sdcard????????????????????????????????????????????????sdcard???????????????????????????sdcard??????????????????virtualEnvModel?????????????????????
        fakeDevice2(context);
    }

    /**
     * ?????????????????????????????????????????????????????????????????????ratel framework???????????????????????????????????????????????????????????????????????????????????????
     *
     * @param context ?????????
     */
    private static void judgeAndReplaceDeviceEnv(Context context) throws IOException {
        // multi user ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
        if (virtualEnvModel == VirtualEnv.VirtualEnvModel.MULTI) {
            if (RatelRuntime.isZeldaEngine()) {
                Log.w(Constants.TAG, "zelda??????????????????????????????????????????multi??????,Ratel??????MULTI?????????Zelda????????????");
            } else {
                handleMultiUserEnv(context);
            }
            return;
        }
        //??????????????????????????????????????????????????????????????????????????????
        if (!RatelRuntime.isMainProcess) {
            return;
        }

        if (virtualEnvModel == VirtualEnv.VirtualEnvModel.START_UP) {
            //startUp????????????????????????????????????
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

        //TODO sdk_26????????????????????? /proc/stat ????????????
        //??????????????????????????????
        //TODO /sys/class/net/wlan0/address
        //TODO /sys/class/net/eth0/address
        //TODO /sys/class/net/wifi/address
        RatelEnvironment.ensureEnvMockSubDirs();

        for (String envMockBackDir : dataDataPath(RatelEnvironment.envMockBaseDir().getCanonicalPath())) {
            RatelNative.whitelist(envMockBackDir);
        }

        for (String envMockDir : dataDataPath(RatelEnvironment.envMockDir().getCanonicalPath())) {
            //??????????????????????????? https://bbs.pediy.com/thread-255212.htm
            RatelNative.forbid(envMockDir, true);
        }

        File unpackDir = new File(RatelToolKit.sContext.getFilesDir(), "ratel_unpack");
        RatelNative.whitelist(unpackDir.getAbsolutePath());

        // RatelNative.whitelist("/data/user/0/com.csair.mbp/.vCache/");
        if (RatelRuntime.nowPackageName.equals("com.csair.mbp")) {
            //TODO ?????????????????????????????????????????????????????????????????????????????????VA????????????????????????????????????
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
        //??????keep
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
        //?????????app????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????app??????sdcard???????????????????????????apk??????check??????????????????????????????????????????
        SdcardFake.fakeFileSystem(context);

        // 5
        //fake settings and mock android_id,
        //????????????????????????????????????????????????????????????apk????????????????????????????????????????????????????????????????????????mock???????????????????????????????????????????????????????????????????????????????????????????????????
        //??????app??????????????????????????????id???????????????
        SettingsFake.fakeSettings();

        // 6
        //TODO ??????????????????????????? FP_CRITICAL
        //??????vpn
        new NetworkMockController().doMock();

        // 7
        //??????Wi-Fi???BSSID

        WifiXSSIDFakeV2.fakeWifi();

        //8
        // ??????????????????????????????????????????????????????
        TimeFake.fakeTime();

        // 9
        //??????????????????????????????sdcard??????
        SizeFake.fakeSize();

        // 10
        //??????linux?????????????????????
        LinuxFileFake.fakeLinuxFeatureFile();

        // 11
        //??????contentProvider
        ContentProviderFake.fakeContentProvider();

        // 12
        //????????????????????????????????????????????????????????????????????????
        SerialFake.fakeSerial();

        // 13
        MSAFake.doFake();

        // 14
        BinderFingerHookManager.doMockBinder();

        // 15
        MediaDrmFake.fakeDeviceId();
    }
}
