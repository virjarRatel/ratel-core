package com.virjar.ratel.envmock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import com.virjar.ratel.RatelNative;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.runtime.RatelEnvironment;
import com.virjar.ratel.runtime.RatelRuntime;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;

public class SdcardFake {

    private static String[] getAllPoints(Context context) {
        StorageManager manager = (StorageManager)
                context.getSystemService(Activity.STORAGE_SERVICE);
        String[] points = null;
        try {
            Method method = manager.getClass().getMethod("getVolumePaths");
            points = (String[]) method.invoke(manager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return points;
    }

    @SuppressLint("SdCardPath")
    public static HashSet<String> allSdcardPoint() {
        HashSet<String> mountPoints = new HashSet<>(3);
        // android 10+ 可能存在外部分区存储
        mountPoints.add(RatelRuntime.originContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        mountPoints.add("/mnt/sdcard/");
        mountPoints.add("/sdcard/");
        mountPoints.add("/storage/emulated/0/");
        String[] points = getAllPoints(RatelRuntime.originContext);
        if (points != null) {
            Collections.addAll(mountPoints, points);
        }
        return mountPoints;
    }


    static void fakeFileSystem(Context context) throws IOException {
        if (!Environment.MEDIA_MOUNTED.equalsIgnoreCase(Environment.getExternalStorageState())) {
            Log.w(Constants.TAG, "the sdcard not mounted");
            return;
        }

        for (String mountPoint : allSdcardPoint()) {
            //RatelNative.redirectDirectory(mountPoint, FileManager.sdcardMockDir(context).getCanonicalPath());
            RatelNative.redirectDirectory(mountPoint, RatelEnvironment.envMockSdcard().getCanonicalPath());
        }

        String dataData = new File(Environment.getDataDirectory(), "data").getCanonicalPath();
        String mDataPath = context.getFilesDir().getParentFile().getCanonicalPath();
        //String mockDir = FileManager.dataDataMockDir(context).getCanonicalPath();
        //TODO 特殊逻辑，访问 /data/data 但是不在 /data/data/mPackage/ 下的，不允许访问。
        //正常清空下是不允许访问的，但是如果是同一个公司发行的不同app，他们是有权限相互访问的。这里禁止他们，并且设定为这些app相互不可见
        // 不过有一个特殊点，就是在高版本的Android机器上面，文件权限限制很严格。美团和点评虽然对文件做了特殊设置，也是无法相互访问的

        //TODO 为了避免检测常见文件夹，如相册、下载文件夹等。还需要把sdcard相关文件夹同步到mock目录

    }
}
