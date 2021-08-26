package com.virjar.ratel.demoapp;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 多开检测方案测试
 * https://bbs.pediy.com/thread-255212.htm
 */
public class DualAppCheckTest {
    private static final String TAG = MainActivity.LogTag;

    public static void checkDualApp(TextView textView, ApplicationInfo applicationInfo) {
        StringBuilder stringBuffer = new StringBuilder();
        String sb = "系统级多开              :" +
                (isSysDualApp() ? "阳性 +" : "阴性") +
                "\n";
        stringBuffer.append(sb);
//        String sb2 = "用户级多开 Native :" +
//                (isDualAppNative(applicationInfo.dataDir) ? "阳性 +" : "阴性") +
//                "\n";
//        stringBuffer.append(sb2);
        String sb3 = "用户级多开 Java    :" +
                (isDualApp(applicationInfo.dataDir) ? "阳性 +" : "阴性") +
                "\n";
        stringBuffer.append(sb3);
        if (Build.VERSION.SDK_INT >= 26) {
            String sb4 = "用户级多开 EX        :" +
                    (isDualAppEx(applicationInfo.dataDir) ? "阳性 +" : "阴性") +
                    "\n";
            stringBuffer.append(sb4);
        } else {
            stringBuffer.append("用户级多开 EX        :Java 版本公开实现不支持 8.0 以下系统\n");
        }
        textView.setText(stringBuffer.toString());
    }

    // public static native boolean isDualAppNative(String str);


    private static boolean isSysDualApp() {
        return android.os.Process.myUid() / 100000 != 0;
    }

    @RequiresApi(api = 26)
    private static boolean isDualAppEx(String str) {
        try {
            String testFdPath = str + File.separator + "wtf_jack";
            Log.i(TAG, "the testFdPath:" + testFdPath);
            FileDescriptor fd = new FileOutputStream(testFdPath).getFD();
            Field declaredField = fd.getClass().getDeclaredField("descriptor");
            declaredField.setAccessible(true);
            @SuppressLint("DefaultLocale") String absolutePath =
                    Files.readSymbolicLink(
                            Paths.get(String.format("/proc/self/fd/%d", (Integer) declaredField.get(fd)))
                    ).toFile().getAbsolutePath();
            Log.i(TAG, "absolutePath for wtf_jack:" + absolutePath);
            String substring = absolutePath.substring(absolutePath.lastIndexOf(File.separator));
            if (!substring.equals(File.separator + "wtf_jack")) {
                Log.i(TAG, "");
                return true;
            }
            String replace = absolutePath.replace("wtf_jack", "..");

            Log.d(TAG, "isDualAppEx: " + replace);
            if (new File(replace).canRead()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private static boolean isDualApp(String str) {
        return new File(str + File.separator + "..").canRead();
    }
}
