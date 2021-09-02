package com.virjar.ratel.inject.template.kratos;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import dalvik.system.DexClassLoader;

public class KratosBootstrap {
    private static boolean startup = false;
    private static final String RATEL_PACKAGE_NAME = "com.virjar.ratel.manager";
    private static final String RATEL_RUNTIME_CLASS_NAME = "com.virjar.ratel.runtime.RatelRuntime";

    private static final String DREAMLAND_RUNTIME_PACKAGE_NAME = "com.dreamland.runtime";
    private static final String DREAMLAND_RUNTIME_CLASS = "com.dreamland.runtime.DreamlandRuntime";

    /**
     * ratel manager VersionCode >= 13 才支持Kratos引擎
     */
    private static final long SUPPORT_KRATOS_RM_VERSION = 13;

    public static void bootstrap(Context context) {
        if (startup) {
            return;
        }
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            //for system_server ,there no pm ready when application attach first
            return;
        }

        if (startupForRatelFramework(packageManager, context)) {
            startup = true;
            return;
        }

        if (startupForDreamlandFramework(packageManager, context)) {
            startup = true;
        }
    }

    private static boolean startupForDreamlandFramework(PackageManager packageManager, Context context) {
        Context dreamlandContext;
        try {
            dreamlandContext = context.createPackageContext(DREAMLAND_RUNTIME_PACKAGE_NAME, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        try {
            dreamlandContext.getClassLoader().loadClass(DREAMLAND_RUNTIME_CLASS)
                    .getDeclaredMethod("callApplicationAttach", Context.class)
                    .invoke(null, context);
            return true;
        } catch (Throwable throwable) {
            Log.e("DreamLand", "DreamLand kratos engine startup failed!!", throwable);
            throw new RuntimeException(throwable);
        }
    }

    private static boolean startupForRatelFramework(PackageManager packageManager, Context context) {
        if (RATEL_PACKAGE_NAME.equals(context.getPackageName())) {
            // rm 不允许被注入
            return false;
        }
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(RATEL_PACKAGE_NAME, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (packageInfo.getLongVersionCode() < SUPPORT_KRATOS_RM_VERSION) {
                return false;
            }
        } else {
            if (packageInfo.versionCode < SUPPORT_KRATOS_RM_VERSION) {
                return false;
            }
        }

        Context kratosContex;
        try {
            kratosContex = context.createPackageContext(packageInfo.packageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }

        File ratelRuntimeDir = context.getDir("ratel_runtime", Context.MODE_PRIVATE);
        try {
            boolean codeUpdate = false;
            InputStream stream = kratosContex.getAssets().open("ratel_serialNo.txt");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int n;
            int EOF = -1;
            byte[] buffer = new byte[1024];
            while (EOF != (n = stream.read(buffer))) {
                byteArrayOutputStream.write(buffer, 0, n);
            }
            stream.close();
            byteArrayOutputStream.close();
            String serialNo = byteArrayOutputStream.toString("utf8");
            //序列号文件，需要放置在work 目录下，后置决定是否覆盖或者删除
            File serialNoFile = new File(context.getDir("ratel_resource", Context.MODE_PRIVATE), "ratel_serialNo.txt");
            if (!serialNoFile.exists()) {
                codeUpdate = true;
            } else {
                FileInputStream fileInputStream = new FileInputStream(serialNoFile);
                byteArrayOutputStream = new ByteArrayOutputStream();
                EOF = -1;
                buffer = new byte[1024];
                while (EOF != (n = fileInputStream.read(buffer))) {
                    byteArrayOutputStream.write(buffer, 0, n);
                }
                stream.close();
                byteArrayOutputStream.close();
                String oldSerialNo = byteArrayOutputStream.toString("utf8");
                if (!serialNo.equals(oldSerialNo)) {
                    codeUpdate = true;
                }
            }

            File dest = new File(ratelRuntimeDir, "ratel_container-driver.jar");
            if (!dest.exists() || !dest.isFile()) {
                codeUpdate = true;
            }

            File optimizedDirectory = new File(ratelRuntimeDir, "runtime_code_dex");

            if (codeUpdate) {
                File parentFile = dest.getParentFile();
                if (!parentFile.exists()) {
                    if (!parentFile.mkdirs()) {
                        throw new IllegalStateException("can not create dir: " + parentFile);
                    }
                }
                InputStream input = kratosContex.getAssets().open("ratel_container-driver.jar");
                FileOutputStream fileOutputStream = new FileOutputStream(dest);

                EOF = -1;
                buffer = new byte[1024 * 4];
                while (EOF != (n = input.read(buffer))) {
                    fileOutputStream.write(buffer, 0, n);
                }
                input.close();
                fileOutputStream.close();
                deleteDir(optimizedDirectory);
            }

            if (!optimizedDirectory.exists()) {
                if (!optimizedDirectory.mkdirs()) {
                    throw new RuntimeException("can not create dir: " + optimizedDirectory);
                }
            }

            String nativeLibDir = getNativeLibDir(packageInfo.applicationInfo, kratosContex.getClassLoader());
            DexClassLoader dexClassLoader = new DexClassLoader(dest.getCanonicalPath(), optimizedDirectory.getCanonicalPath(), nativeLibDir, ClassLoader.getSystemClassLoader().getParent());
            dexClassLoader
                    .loadClass("com.virjar.ratel.runtime.RatelRuntime")
                    .getMethod("applicationAttachWithKratosEngine", Context.class, Context.class)
                    .invoke(null, context, kratosContex);
            return true;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private static String getNativeLibDir(ApplicationInfo applicationInfo, ClassLoader baseClassloader) {

        Set<String> nativeLibs = new HashSet<>();
        nativeLibs.add(applicationInfo.nativeLibraryDir);

        String classLoaderStr = baseClassloader.toString();
        int i = classLoaderStr.indexOf("nativeLibraryDirectories=");
        if (i > 0) {
            classLoaderStr = classLoaderStr.substring(i);
            int start = classLoaderStr.indexOf("[");
            int end = classLoaderStr.indexOf("]");
            classLoaderStr = classLoaderStr.substring(start + 1, end);
            for (String libItem : classLoaderStr.split(",")) {
                String trim = libItem.trim();
                if (trim.startsWith("/system/")) {
                    continue;
                }
                nativeLibs.add(trim);
            }
            //主要为了处理splitApk的问题。
        } else {
            Log.w("RAEL", "can not get nativeLibraryDirectories from classLoader: " + baseClassloader);
        }
        StringBuilder buf = new StringBuilder(nativeLibs.size() * 16);

        boolean isFirst = true;
        for (String str : nativeLibs) {
            if (!isFirst) {
                buf.append(":");
            }
            isFirst = false;
            buf.append(str);
        }
        return buf.toString();
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
        Log.i("RATEL", "remove file:" + dir.getAbsolutePath());
        return dir.delete();
    }
}
