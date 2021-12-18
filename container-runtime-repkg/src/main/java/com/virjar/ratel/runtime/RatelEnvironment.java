package com.virjar.ratel.runtime;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.envmock.PropertiesStoreHolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import external.org.apache.commons.io.IOUtils;

/**
 * 各种目录定义
 */
public class RatelEnvironment {
    private static Set<FileDirHolder> allHolders = new HashSet<>();

    private static boolean userSdcardMockDir = false;

    public static void useSdcardAsMockDir() {
        userSdcardMockDir = true;
    }

    private static File makeSureDirExist(File file) {
        if (file.exists()) {
            if (!file.isDirectory()) {
                throw new IllegalStateException("the file: " + file.getAbsolutePath() + " is not directory");
            }
            return file;
        }
        if (!file.mkdirs()) {
            throw new IllegalStateException("can not create directory: " + file.getAbsolutePath());
        }
        return file;
    }


    private static FileDirHolder holderRatelRuntimeDir = FileDirHolder.create(() -> RatelRuntime.getOriginContext().getDir("ratel_runtime", Context.MODE_PRIVATE));

    /**
     * ratel框架释放目录，存储ratelRuntime的代码位置,指向：/data/data/pkg/app_ratel_runtime
     *
     * @return runtime存储目录
     */
    public static File ratelRuntimeDir() {
        return holderRatelRuntimeDir.get();
    }

    private static FileDirHolder holderRatelResourceDir = FileDirHolder.create(() -> RatelRuntime.getOriginContext().getDir("ratel_resource", Context.MODE_PRIVATE));

    /**
     * ratel资源目录，存储和虚拟环境无关的，ratel框架资源。如ratel编译序列号。ratel配置文件，ratel颁发证书，mock签名
     * 指向: /data/data/pkg/app_ratel_resource
     *
     * @return ratel运行框架资源
     */
    public static File ratelResourceDir() {
        return holderRatelResourceDir.get();
    }

    private static final FileDirHolder holderNativeCacheDir = FileDirHolder.create(() -> new File(ratelResourceDir(), Constants.nativeCacheDir));

    /**
     * @return
     */
    public static File nativeCacheDir() {
        return holderNativeCacheDir.get();
    }


    private static FileDirHolder holderRatelRuntimeTmp = FileDirHolder.create(() -> RatelRuntime.getOriginContext().getDir("ratel_tmp", Context.MODE_PRIVATE));

    /**
     * 运行时临时文件夹，主要实现实时hook替换，如：
     * <ul>
     * <li>/proc/maps</li>
     * <li>/proc/meminfo</li>
     * <li>/proc/status</li>
     * </ul>
     * 该文件夹，应该在app完全重启（主进程启动，且无子进程存活）的时候被清理
     *
     * @return runtime 临时文件夹
     */
    public static File ratelRuntimeTmp() {
        return holderRatelRuntimeTmp.get();
    }


    public static File anrDetectPIDS() {
        return makeSureDirExist(new File(ratelRuntimeTmp(), "anrDetect"));
    }

    private static FileDirHolder holderEnvMockBaseDir = FileDirHolder.create(() -> {
        File dir;
        if (userSdcardMockDir) {
            dir = new File(Environment.getExternalStorageDirectory(), "ratel_mock/" + RatelRuntime.nowPackageName + "/");
        } else {
            dir = RatelRuntime.getOriginContext().getDir("ratel_env_mock", Context.MODE_PRIVATE);
        }
        return dir;
    });

    /**
     * 虚拟环境目录，有两个可能，存储卡(必须有存储卡读写权限)或者data目录
     * 指向/sdcard/ratel_mock/pkg/ 或者  /data/data/pkg/app_ratel_env_mock
     *
     * @return 当前app的虚拟化环境根目录，该目录包含了所有的多用户数据资料
     */
    public static File envMockBaseDir() {
        return holderEnvMockBaseDir.get();
    }


    /**
     * @return ratel框架运行，需要保证IO重定向白名单
     */
    public static List<File> ratelWhiteFiles() {
        List<File> ret = new ArrayList<>();
        ret.add(ratelRuntimeDir());
        ret.add(ratelResourceDir());
        //mock目录不能放到白名单，否则在fd反查文件路径的时候，将会命中白名单规则，从而暴露原始路径
        // ret.add(envMockBaseDir());
        ret.add(ratelRuntimeTmp());
        return ret;
    }


    /**
     * @return 对于单个用户，其mock数据的根目录
     */
    public static File envMockDir() {
        if (RatelRuntime.isZeldaEngine()) {
            //zelda模式下，不需要mockUserId
            return envMockBaseDir();
        }
        String mockUserId = MultiUserManager.getMockUserId();
        if (TextUtils.isEmpty(mockUserId)) {
            return envMockBaseDir();
        }
        return new File(envMockBaseDir(), mockUserId);
    }


    //TODO 后面继续做 FileDirHolder

    /**
     * @return 内存卡模拟目录
     */
    public static File envMockSdcard() {
        return makeSureDirExist(new File(envMockDir(), "sdcard"));
    }

    /**
     * @return 模拟 /data/data/pkg/
     */
    public static File envMockData() {
        return makeSureDirExist(new File(envMockDir(), "data"));
    }

    /**
     * @return 模拟 /data/user_de/pkg
     */
    public static File envMockData_de() {
        return makeSureDirExist(new File(envMockDir(), "user_de"));
    }


    /**
     * @return 模拟 /tmp/
     */
    public static File envMockTmp() {
        return makeSureDirExist(new File(envMockDir(), "tmp"));
    }

    /**
     * 其他文件一箩筐<br>
     * 如：
     * <pre>
     * /proc/meminfo
     * /proc/sys/kernel/osrelease
     * /proc/sys/kernel/random/boot_id
     * /proc/sys/kernel/random/uuid
     * /proc/uptime
     * /sys/block/mmcblk0/device/cid
     * /sys/class/net/eth0/address -> /sys/class/net/eth0/ && cat address
     * /sys/class/net/eth1/address
     * /sys/class/net/eth2/address
     * /sys/class/net/wlan0/address -> /sys/class/net/wlan0/ && cat address
     * /proc/cpuinfo -> /proc/;cat cpuinfo
     * /proc/net/arp -> /proc/net/ && cat arp
     * /proc/self/status ->/proc/self/;cat status
     * /dev/socket
     * /system/fonts
     * </pre>
     *
     * @return 模拟系统根目录 /
     */
    public static File envMockRoot() {
        return makeSureDirExist(new File(envMockDir(), "root"));
    }

    public static File kernelBootIdFile() {
        return new File(envMockRoot(), "kernel_random_boot_id");
    }

    public static File soc0SerialNumberFile() {
        return new File(envMockRoot(), "soc0_serial_number");
    }

    /**
     * 关于本用户特有的配置,如imei，serial，安装时间差值等
     *
     * @return 用户配置文件地址
     */
    public static File userConfigProperties() {
        return new File(envMockRoot(), "userConfig.properties");
    }

    public static int userIdentifierSeed() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException var3) {
            throw new IllegalStateException("MD5 not supported", var3);
        }

        byte[] md5Bytes = md.digest(userIdentifier().getBytes(StandardCharsets.UTF_8));
        int retSeed = (md5Bytes[0] & 0xFF) << 24 | (md5Bytes[1] << 16 & 0xFF) | (md5Bytes[2] << 8 & 0xFF) | (md5Bytes[3] & 0xFF);

        if (retSeed == -1) {
            // -1 有特殊标记
            retSeed = 1;
        }
        return retSeed;
    }

    public static String userIdentifier() {

        PropertiesStoreHolder propertiesHolder = PropertiesStoreHolder.getPropertiesHolder(userConfigProperties());
        String userIdentifierValue = propertiesHolder.getProperty("userIdentifier");
        if (!TextUtils.isEmpty(userIdentifierValue)) {
            return userIdentifierValue;
        }
        String nowUser = MultiUserManager.nowUser();

        if (TextUtils.isEmpty(nowUser)) {
            nowUser = UUID.randomUUID().toString();
        }

        propertiesHolder.setProperty("userIdentifier", nowUser);
        return nowUser;
    }

    public static void ensureEnvMockSubDirs() {
        envMockSdcard();
        envMockData();
        envMockData_de();
        envMockTmp();
        envMockRoot();
        fakeConfigDir();
    }

    public static File fakeConfigDir() {
        return makeSureDirExist(new File(envMockDir(), "fake_conf"));
    }

    public static File systemPropertiesFakeFile() {
        return new File(fakeConfigDir(), "systemProperties.properties");
    }

    public static File telephonyFakeFile() {
        return new File(fakeConfigDir(), "telephonyProperties.properties");
    }

    public static File settingsGlobalMockFile() {
        return new File(fakeConfigDir(), "settingGlobal.properties");
    }

    public static File settingsSystemMockFile() {
        return new File(fakeConfigDir(), "settingSystem.properties");
    }

    public static File settingsSecureMockFile() {
        return new File(fakeConfigDir(), "settingSecure.properties");
    }

    public static File wifiMockFile() {
        return new File(fakeConfigDir(), "ratel_wifi_fake.properties");
    }

    public static File timeMockFile() {
        return new File(fakeConfigDir(), "ratel_time_fake.properties");
    }

    public static File buildPropMockFile() {
        return new File(fakeConfigDir(), "build.prop");
    }

    public static File sizeMockFile() {
        return new File(fakeConfigDir(), "ratel_size_fake.properties");
    }

    public static File MSAMockFile() {
        return new File(fakeConfigDir(), "ratel_mas_fake.properties");
    }

    //ratel内部资源释放目录

    public static File sandHookCacheDir() {
        return makeSureDirExist(new File(ratelResourceDir(), Constants.sandHookCache));
    }

    public static File xposedModulePath() {
        return new File(ratelResourceDir(), Constants.xposedBridgeApkFileName);
    }

    public static File originApkDir() {
        return new File(ratelResourceDir(), Constants.originAPKFileName);
    }

    public static File ratelDexMakerOptDir() {
        return new File(ratelResourceDir(), Constants.dexmakerOptFileName);
    }

    public static File xposedApiBridgeJar() {
        return new File(ratelResourceDir(), Constants.xposedApiBridgeJarFileName);
    }

    public static File ratelConfigFile() {
        return new File(ratelResourceDir(), Constants.ratelConfigFileName);
    }

    public static File ratelNativeLib32Path() {
        return new File(ratelResourceDir(), Constants.RATEL_NATIVE_LIB_32);
    }

    public static File ratelNativeLib64Path() {
        return new File(ratelResourceDir(), Constants.RATEL_NATIVE_LIB_64);
    }


    public static File originAPKSignatureFile() {
        return new File(ratelResourceDir(), "signature.ini");
    }

    public static File hotModuleConfigFile() {
        return new File(ratelResourceDir(), "hotModuleConfig.json");
    }

    public static File hotModuleDir() {
        return makeSureDirExist(new File(ratelResourceDir(), "hotModuleDownloadDir"));
    }


    public static File APKOptimizedDirectory() {
        return makeSureDirExist(ratelResourceDir());
    }

    public static File deadLoadKillerShellDir() {
        return makeSureDirExist(new File(ratelResourceDir(), "ratel_shell"));
    }

    public static void releaseApkFiles() {
        if (!RatelRuntime.isKratosEngine()) {
            // kratos引擎不存在apk改包，所以也就没有原包的概念
            releaseAssetResource(Constants.originAPKFileName);
            releaseAssetResource(Constants.xposedBridgeApkFileName);
        }
        releaseAssetResource(Constants.ratelConfigFileName);
        releaseAssetResource(Constants.ratelCertificate, false);
        releaseAssetResource(Constants.dexmakerOptFileName);
        releaseAssetResource(Constants.xposedApiBridgeJarFileName);
        releaseAssetResource(Constants.RATEL_NATIVE_LIB_32);
        releaseAssetResource(Constants.RATEL_NATIVE_LIB_64);
    }

    private static void releaseAssetResource(String name) {
        releaseAssetResource(name, true);
    }

    private static void releaseAssetResource(String name, boolean force) {
        File destinationFileName = new File(ratelResourceDir(), name);
        //测试环境下，每次强刷代码，否则可能导致代码没有更新
        if (destinationFileName.exists()) {
            //存在则不能强刷，否则多进程会存在文件被删除，导致句柄异常的问题
            //TODO 梳理一下这里的逻辑
            return;
        }
        File parentDir = destinationFileName.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        synchronized (RatelEnvironment.class) {
            if (destinationFileName.exists() && !force) {
                return;
            }

            AssetManager assets = RatelRuntime.getRatelEnvContext().getAssets();
            InputStream inputStream;
            try {
                try {
                    inputStream = assets.open(name);
                } catch (FileNotFoundException fie) {
                    //ignore
                    return;
                }
                if (RatelRuntime.isRatelDebugBuild) {
                    Log.i("ratel", "copy resource: " + destinationFileName);
                }
                IOUtils.copy(inputStream, new FileOutputStream(destinationFileName));
            } catch (IOException e) {
                Log.e("weijia", "copy assets resource failed!!", e);
                throw new IllegalStateException(e);
            }
        }

    }

    private static class FileDirHolder {
        private File theDir = null;
        private DirCreator dirCreator;

        FileDirHolder(DirCreator dirCreator) {
            this.dirCreator = dirCreator;
            //首次需要加载所有数据，控制dir一致性
            // 主要为了解决zelda引擎在io重定向瞬间各种目录结构还不一致的问题，也即java层和c层不是瞬时同步
            get();
        }

        public File get() {
            if (theDir != null) {
                return theDir;
            }
            theDir = makeSureDirExist(dirCreator.create());
            return theDir;
        }

        public static FileDirHolder create(DirCreator dirCreator) {
            FileDirHolder fileDirHolder = new FileDirHolder(dirCreator);
            allHolders.add(fileDirHolder);
            return fileDirHolder;
        }
    }

    private interface DirCreator {
        File create();
    }

    public static void resetCache() {
        for (FileDirHolder fileDirHolder : allHolders) {
            fileDirHolder.theDir = null;
        }
    }
}
