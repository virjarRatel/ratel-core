package com.virjar.ratel.inject.template.zelda;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import dalvik.system.DexClassLoader;

@SuppressLint("Registered")
public class RatelZeldaApplication extends Application {
    private static Class<?> ratelRuntimeClass = null;

    @Override
    protected void attachBaseContext(Context base) {
        try {
            if (ratelRuntimeClass == null) {
                ratelRuntimeClass = createRatelRuntimeClass(base);
            }
            ratelRuntimeClass.getMethod("applicationAttachWithZeldaEngine", Context.class).invoke(null, base);
            // RatelCore.getInstance().callBeforeAttach(base);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // RatelCore.getInstance().callBeforeApplicationOnCreate();
        try {
            ratelRuntimeClass.getMethod("applicationOnCreateWithMultiMode").invoke(null);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }

    private static Class createRatelRuntimeClass(Context context) {
        File ratelRuntimeDir = context.getDir("ratel_runtime", Context.MODE_PRIVATE);
        try {
            boolean codeUpdate = false;
            InputStream stream = context.getAssets().open("ratel_serialNo.txt");
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
                InputStream input = context.getAssets().open("ratel_container-driver.jar");
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

            DexClassLoader dexClassLoader = new DexClassLoader(dest.getCanonicalPath(), optimizedDirectory.getCanonicalPath(), context.getApplicationInfo().nativeLibraryDir, ClassLoader.getSystemClassLoader().getParent());

            return dexClassLoader.loadClass("com.virjar.ratel.runtime.RatelRuntime");
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static boolean deleteDir(File dir) {
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
        return dir.delete();
    }
}
