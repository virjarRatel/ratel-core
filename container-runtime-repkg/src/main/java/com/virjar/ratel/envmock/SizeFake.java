package com.virjar.ratel.envmock;

import android.app.ActivityManager;
import android.content.Context;
import android.system.StructStatVfs;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.RatelNative;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.SuffixTrimUtils;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.hook.RatelHookFlagMethodCallback;
import com.virjar.ratel.runtime.RatelEnvironment;
import com.virjar.ratel.runtime.RatelRuntime;

public class SizeFake {
    private static final String IActivityManagerIPCClassName = "android.app.IActivityManager$Stub$Proxy";
    private static final String ActivityManagerProxyClassName = "android.app.ActivityManagerProxy";

    private static final String androidSystemOsClassName = "android.system.Os";

    public static void fakeSize() {
        fakeMemoryInfo();
        fakeTotalSdcardSize();

    }

    private static Integer fileSystemBlockDiff = null;

    private static void fakeTotalSdcardSize() {
//        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
//        long blockSize = (long) statFs.getBlockCount();


        //android.system.Os.statvfs
        Class androidSystemOsClass = RposedHelpers.findClassIfExists(androidSystemOsClassName, ClassLoader.getSystemClassLoader());
        if (androidSystemOsClass == null) {
            Log.w(Constants.TAG, "can not find class: " + androidSystemOsClassName);
            return;
        }

        RposedHelpers.findAndHookMethod(androidSystemOsClass, "statvfs", String.class, new RatelHookFlagMethodCallback() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                StructStatVfs structStatVfs = (StructStatVfs) param.getResult();
                if (fileSystemBlockDiff == null) {
                    PropertiesStoreHolder propertiesStoreHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.sizeMockFile());
                    String fileSystemDiffStr = propertiesStoreHolder.getProperty(Constants.fileSystemBlockDiff);
                    if (TextUtils.isEmpty(fileSystemDiffStr)) {
                        fileSystemBlockDiff = SuffixTrimUtils.randomMockDiff(1024);
                        propertiesStoreHolder.setProperty(Constants.fileSystemBlockDiff, String.valueOf(fileSystemBlockDiff));
                    } else {
                        fileSystemBlockDiff = Integer.parseInt(fileSystemDiffStr);
                    }
                }
                RposedHelpers.setObjectField(structStatVfs, "f_blocks", structStatVfs.f_blocks + fileSystemBlockDiff);
            }
        });
    }

    private static void fakeMemoryInfo() {
        ActivityManager activityManager = (ActivityManager) RatelRuntime.getOriginContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            Log.e(Constants.TAG, "can not get ActivityManager");
            return;
        }

//        if (RatelRuntime.isRatelDebugBuild) {
//            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
//            activityManager.getMemoryInfo(memoryInfo);
//            Log.i(Constants.VENV_TAG, "origin totalMem: " + memoryInfo.totalMem);
//
//            try {
//                String meminfoStr = FileUtils.readFileToString(new File("/proc/meminfo"), StandardCharsets.UTF_8);
//                Log.i(Constants.VENV_TAG, "/proc/meminfo: " + meminfoStr);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        PropertiesStoreHolder propertiesStoreHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.sizeMockFile());
        String totalMemoryStr = propertiesStoreHolder.getProperty(Constants.totalMemoryKey);
        if (TextUtils.isEmpty(totalMemoryStr)) {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);

            int totalMemoryDiff = SuffixTrimUtils.randomMockDiff(1024);
            propertiesStoreHolder.setProperty(Constants.totalMemoryDiff, String.valueOf(totalMemoryDiff));
            totalMemoryStr = String.valueOf(memoryInfo.totalMem + totalMemoryDiff * 1024);
            propertiesStoreHolder.setProperty(Constants.totalMemoryKey, totalMemoryStr);
        }

        String totalMemoryDiffStr = propertiesStoreHolder.getProperty(Constants.totalMemoryDiff);

        RatelNative.setMemoryDiff(Long.parseLong(totalMemoryDiffStr));

        RatelHookFlagMethodCallback fakeMemoryHook = new RatelHookFlagMethodCallback() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ActivityManager.MemoryInfo memoryInfo = (ActivityManager.MemoryInfo) param.args[0];

                PropertiesStoreHolder propertiesStoreHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.sizeMockFile());
                String totalMemoryStr = propertiesStoreHolder.getProperty(Constants.totalMemoryKey);
                if (TextUtils.isEmpty(totalMemoryStr)) {
                    Log.w(Constants.TAG, "status warning, total memory not save");
                    totalMemoryStr = String.valueOf(memoryInfo.totalMem + SuffixTrimUtils.randomMockDiff(1024) * 1024);
                    propertiesStoreHolder.setProperty(Constants.totalMemoryKey, totalMemoryStr);
                }
                memoryInfo.totalMem = Long.parseLong(totalMemoryStr);
            }
        };

        Class IActivityManagerIPCClass = RposedHelpers.findClassIfExists(IActivityManagerIPCClassName, ClassLoader.getSystemClassLoader());
        if (IActivityManagerIPCClass == null) {
            //老版本是这个
            IActivityManagerIPCClass = RposedHelpers.findClassIfExists(ActivityManagerProxyClassName, ClassLoader.getSystemClassLoader());
        }
        if (IActivityManagerIPCClass == null) {
            Log.w(Constants.TAG, "can not find ipc class: " + IActivityManagerIPCClassName);
        } else {

            RposedBridge.hookAllMethodsSafe(IActivityManagerIPCClass, "getMemoryInfo", fakeMemoryHook);
        }

        RposedBridge.hookAllMethodsSafe(ActivityManager.class, "getMemoryInfo", fakeMemoryHook);

        //TODO /proc/meminfo
    }
}
