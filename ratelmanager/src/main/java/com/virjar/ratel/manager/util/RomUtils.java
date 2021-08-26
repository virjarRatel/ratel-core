package com.virjar.ratel.manager.util;

import android.content.Context;
import android.content.pm.PackageManager;

public class RomUtils {


    //检测手机上是否安装某应用
    public static boolean checkApkExist(Context context, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            context.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


}