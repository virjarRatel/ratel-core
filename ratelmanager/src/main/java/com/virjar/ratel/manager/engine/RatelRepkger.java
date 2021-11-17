package com.virjar.ratel.manager.engine;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.allcommon.ClassNames;
import com.virjar.ratel.allcommon.ReflectUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import dalvik.system.DexClassLoader;
import external.org.apache.commons.io.FileUtils;

public class RatelRepkger {

    public static void mainReflect(Context context, String workDir, String[] args) throws Exception {
        String repkgDexPath = workDir + File.separator + "container-builder-repkg-dex.jar";
        FileUtils.copyInputStreamToFile(context.getAssets().open("container-builder-repkg-dex.jar"), new File(repkgDexPath));
        DexClassLoader loader = new DexClassLoader(repkgDexPath, context.getCacheDir().getAbsolutePath(), null, context.getClassLoader());
        Class<?> mainClass = ReflectUtil.findClass(ClassNames.BUILDER_MAIN.getClassName(), loader);
        Log.d("RatelRepkger", "mainClass=" + mainClass);
        ReflectUtil.callStaticMethod(mainClass, "main", (Object) args);
    }

    public static void mainShell(Context context, String[] args, LogCallback logCallback) throws Exception {
        File repkgDexFilePath = RatelEngineLoader.releaseReBuilderDexResource(context);

        // -Xms256m
        String command = "dalvikvm -XX:HeapGrowthLimit=512m -cp " + repkgDexFilePath.getAbsolutePath() + " " + ClassNames.BUILDER_MAIN.getClassName() + " " + TextUtils.join(" ", args);
        Log.d("RatelRepkger", "command=" + command);
        Process process = Runtime.getRuntime().exec(command);
        InputStream errorStream = process.getErrorStream();
        InputStream inputStream = process.getInputStream();
        printStream("repkg-cmd-error", errorStream, true, logCallback);
        printStream("repkg-cmd-stand", inputStream, false, logCallback);
    }

    private static void printStream(final String tName, final InputStream inputStream, boolean isError, LogCallback logCallback) {
        new Thread(tName) {
            @Override
            public void run() {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        Log.i(tName, line);
                        if (logCallback != null) {
                            if (isError) {
                                logCallback.onError(line);
                            } else {
                                logCallback.onLog(line);
                            }
                        }
                    }
                    bufferedReader.close();
                    inputStreamReader.close();
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(tName, "read error", e);
                }
            }
        }.start();
    }

    public interface LogCallback {
        void onLog(String msg);

        void onError(String msg);
    }
}
