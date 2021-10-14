package com.virjar.ratel.runtime;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipFile;

public class AnrDetector {
    public static void setup() throws IOException {
        File pidDirs = RatelEnvironment.anrDetectPIDS();
        if (RatelRuntime.isMainProcess) {
            //clear
            FileUtils.cleanDirectory(pidDirs);
        }
        int pid = Process.myPid();
        File file = new File(pidDirs, String.valueOf(pid));
        try {
            FileUtils.writeToFile(String.valueOf(pid).getBytes(StandardCharsets.UTF_8), file);
        } catch (IOException e) {
            Log.e(Constants.TAG, "error", e);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (file.exists()) {
                if (!file.delete()) {
                    Log.e(Constants.TAG, "can not remove anr detector pid file: " + file.getAbsolutePath());
                }
            }
        }, 3000);

        if (RatelRuntime.isMainProcess) {
            startDetectProcess(pidDirs);
        }
    }

    private static void startDetectProcess(File pidDirs) throws IOException {
        // 启动一个新的进程，在4s之后探测PID文件是否被删除了。如果没有被删除，那么证明出现了ANR
        // 此时新进程强杀当前进程
        // todo
        String zipEntryName = "lib/" + Build.SUPPORTED_ABIS[0] + "/libratelanr.so";

        File soPath = new File(RatelEnvironment.ratelRuntimeDir(), "libratelanr.so");
        if (!soPath.exists()) {
            //copy soFile
            File apkFile = new File(RatelRuntime.getRatelEnvContext().getApplicationInfo().sourceDir);
            try (ZipFile zipFile = new ZipFile(apkFile)) {
                try (InputStream inputStream = zipFile.getInputStream(zipFile.getEntry(zipEntryName))) {
                    FileUtils.writeToFile(inputStream, soPath);
                    if (!soPath.setExecutable(true)) {
                        throw new IllegalStateException("can not make so executeable: " + soPath.getAbsolutePath());
                    }
                }
            }

        }
        if (!soPath.exists()) {
            throw new IOException("can not inject anr detect so path: " + soPath.getAbsolutePath());
        }
        Runtime.getRuntime().exec(new String[]{soPath.getAbsolutePath(), pidDirs.getAbsolutePath()});
    }
}
