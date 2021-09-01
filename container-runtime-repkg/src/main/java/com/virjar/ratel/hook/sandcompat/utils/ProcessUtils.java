package com.virjar.ratel.hook.sandcompat.utils;

import android.os.Process;
import android.text.TextUtils;
import android.util.Log;


import com.virjar.ratel.allcommon.Constants;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Created by swift_gan on 2017/11/23.
 */

public class ProcessUtils {

    // Copied from UserHandle, indicates range of uids allocated for a user.
    public static final int PER_USER_RANGE = 100000;
    public static final int USER_SYSTEM = 0;


    private static volatile String processName = null;

    public static String getProcessName() {
        if (!TextUtils.isEmpty(processName))
            return processName;
        processName = getProcessName(Process.myPid());
        return processName;
    }


    public static String getProcessName(int pid) {
        BufferedReader cmdlineReader = null;
        try {
            cmdlineReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(
                            "/proc/" + pid + "/cmdline"),
                    StandardCharsets.ISO_8859_1));
            int c;
            StringBuilder processName = new StringBuilder();
            while ((c = cmdlineReader.read()) > 0) {
                processName.append((char) c);
            }
            return processName.toString();
        } catch (Throwable throwable) {
            Log.w(Constants.TAG, "getProcessName: " + throwable.getMessage());
        } finally {
            try {
                if (cmdlineReader != null) {
                    cmdlineReader.close();
                }
            } catch (Throwable throwable) {
                Log.e(Constants.TAG, "getProcessName: " + throwable.getMessage());
            }
        }
        return "";
    }


}
