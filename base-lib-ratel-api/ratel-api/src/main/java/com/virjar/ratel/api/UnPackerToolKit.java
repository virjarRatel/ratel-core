package com.virjar.ratel.api;

import android.app.Activity;
import android.util.Log;

import com.virjar.ratel.api.hint.RatelEngineHistory;
import com.virjar.ratel.api.hint.RatelEngineVersion;
import com.virjar.ratel.api.inspect.ClassLoadMonitor;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedHelpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import external.org.apache.commons.io.IOUtils;
import external.org.apache.commons.lang3.NumberUtils;
import external.org.apache.commons.lang3.StringUtils;

/**
 * 脱壳工具类封装
 *
 * @author virjar
 * @since 1.3.5
 */
@RatelEngineVersion(RatelEngineHistory.V_1_4_6)
public class UnPackerToolKit {
    private static final String TAG_UNPACK = "unpack";

    /**
     * 开启脱壳机，ratel框架将会影响虚拟机代码执行流程。这可能导致框架不稳定，已经影响app执行性能<br>
     * 一般情况不建议随时开启脱壳机<br>
     * 请注意，脱壳机开启需要在app运行前执行，否则错过dump时间
     *
     * @param workDir    需要指定一个工作目录，让脱壳机dump相关加密的指令.参数可以为空，为空系统自动分配
     * @param dumpMethod 是否需要dump指令，大部分情况下dex整体dump就可以，此时不存在dex修复过程，相对来说性能更好
     */
    public void enableUnPack(File workDir, boolean dumpMethod) {
        RatelToolKit.ratelUnpack.enableUnPack(workDir, dumpMethod);
    }

    public void enableUnPack(File workDir) {
        RatelToolKit.ratelUnpack.enableUnPack(workDir, false);
    }

    public void enableUnPack() {
        RatelToolKit.ratelUnpack.enableUnPack(new File(RatelToolKit.sContext.getFilesDir(), "ratel_unpack"), false);
    }

    /**
     * 自动开启脱壳组件,他在
     */
    public static void autoEnable() {
        autoEnable(false);
    }

    public interface UnpackEvent {
        void onFinish(File file);
    }

    public static void unpack(UnpackEvent unpackEvent) {
        autoEnable();
        new Thread("construct-unpack") {
            @Override
            public void run() {
                try {
                    Thread.sleep(30 * 1000 + new Random().nextInt(2000));
                    Log.e(TAG_UNPACK, "begin constructUnpackedApk");
                    File apkFile = constructUnpackedApkInternal();
                    if (apkFile == null) {
                        Log.e(TAG_UNPACK, "constructUnpackedApk failed");
                        return;
                    }
                    unpackEvent.onFinish(apkFile);
                } catch (Exception e) {
                    Log.e(TAG_UNPACK, "unpack error", e);
                }
            }
        }.start();
    }

    /**
     * 自动开启脱壳组件
     *
     * @param dumpMethod 标志进行整体dump还是方法粒度dump。方法粒度dump可能引发更多不稳定性
     */
    public static void autoEnable(boolean dumpMethod) {
        if (RatelToolKit.processName.equals(RatelToolKit.packageName)) {
            RposedHelpers.findAndHookMethod(Activity.class, "onResume", new RC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    RatelToolKit.ratelUnpack.enableUnPack(
                            new File(RatelToolKit.sContext.getFilesDir(), "ratel_unpack"),
                            dumpMethod
                    );
                }
            });
        }
    }

    /**
     * 确保某个特定的class被脱壳。
     *
     * @param className 一个特定的class
     */
    public static void ensureClassDump(String className) {
        if (StringUtils.isBlank(className)) {
            Log.e(TAG_UNPACK, "empty class:" + className);
            return;
        }
        ClassLoadMonitor.addClassLoadMonitor(className, clazz -> {
            //make sure unpack component enable
            RatelToolKit.ratelUnpack.enableUnPack(
                    new File(RatelToolKit.sContext.getFilesDir(), "ratel_unpack"),
                    false
            );

            // force set dex image valid
            Method[] declaredMethods = clazz.getDeclaredMethods();
            if (declaredMethods.length > 0) {
                RatelToolKit.ratelUnpack.methodDex(declaredMethods[0]);
                return;
            }
            Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
            if (declaredConstructors.length > 0) {
                RatelToolKit.ratelUnpack.methodDex(declaredConstructors[0]);
                return;
            }

            Log.w(TAG_UNPACK, "no available method found for class:" + className);
        });
    }

    /**
     * 直接将所有的dex组装到一个apk文件中，这样可以方便使用各种自动化的反编译工具打开
     *
     * @return 一个apk文件，该文件包括代码和资源，但是没有签名，无法直接运行
     */
    public static File constructUnpackedApk() {
        try {
            return constructUnpackedApkInternal();
        } catch (IOException e) {
            Log.e(TAG_UNPACK, "construct unpacked apk failed", e);
            return null;
        }
    }

    public static final Pattern classesIndexPattern = Pattern.compile("classes(\\d+)\\.dex");

    private static File constructUnpackedApkInternal() throws IOException {
        File workDir = RatelToolKit.ratelUnpack.getWorkDir();
        if (workDir == null || !workDir.exists()) {
            return null;
        }
        List<byte[]> dumpedDex = RatelToolKit.ratelUnpack.findDumpedDex(null);

        File apkFile = new File(RatelToolKit.sContext.getPackageCodePath());

        int maxIndex = 0;
        File target = new File(workDir, "unpacked.apk");
        try (FileOutputStream fileOutputStream = new FileOutputStream(target)) {
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
                try (ZipFile zipFile = new ZipFile(apkFile)) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        if (zipEntry.getName().startsWith("META-INF/")) {
                            // 去掉签名内容
                            continue;
                        }

                        zipOutputStream.putNextEntry(new ZipEntry(zipEntry.getName()));
                        try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
                            IOUtils.copy(inputStream, zipOutputStream);
                        }

                        if (zipEntry.getName().startsWith("classes")) {
                            Matcher matcher = classesIndexPattern.matcher(zipEntry.getName());
                            if (matcher.matches()) {
                                int nowIndex = NumberUtils.toInt(matcher.group(1));
                                Log.e(TAG_UNPACK, "original dex: " + "classes" + nowIndex + ".dex");
                                if (nowIndex > maxIndex) {
                                    maxIndex = nowIndex;
                                }
                            }
                        }
                    }
                    maxIndex++;
                    // append dex
                    for (byte[] dexData : dumpedDex) {
                        if (dexData == null) {
                            continue;
                        }
                        String classesFileName = "classes" + maxIndex + ".dex";
                        Log.e(TAG_UNPACK, "append new dex: " + classesFileName);
                        zipOutputStream.putNextEntry(new ZipEntry(classesFileName));
                        zipOutputStream.write(dexData);
                        maxIndex++;
                    }

                    zipOutputStream.flush();
                }
            }
        }
        Log.e(TAG_UNPACK, "dump apk to :" + target.getAbsolutePath());
        return target;
    }

    /**
     * 给定任意class，获取改class的dex数据
     *
     * @param clazz 任意class
     * @return dex内容
     */
    public byte[] dumpDex(Class<?> clazz) {
        Method[] declaredMethods = clazz.getDeclaredMethods();
        if (declaredMethods.length > 0) {
            return RatelToolKit.ratelUnpack.methodDex(declaredMethods[0]);

        }
        Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
        if (declaredConstructors.length > 0) {
            return RatelToolKit.ratelUnpack.methodDex(declaredConstructors[0]);
        }
        return null;
    }

    /**
     * 根据一个class名称，搜索对应的dex文件
     *
     * @param className class名称
     * @return 存在重复类的情况下，可能有多条；搜索结果为空的情况下，返回空List
     */
    public List<byte[]> searchDex(String className) {
        return RatelToolKit.ratelUnpack.findDumpedDex(className);
    }


}
