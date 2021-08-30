package com.virjar.ratel.api.extension.debug;

import android.os.Build;
import android.os.Debug;
import android.os.Process;
import android.util.Log;

import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.rposed.RposedHelpers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import external.org.apache.commons.io.FileUtils;
import external.org.apache.commons.io.IOUtils;

/**
 * IDA debugger.
 * <br>
 * 支持免root使用ida调试app，请注意需要平头哥在构造apk的时候加入 "-d"参数，将app指定为debug模式<br>
 * ratel引擎自1.4.9之后，默认会提供native反调试绕过功能<br>
 * 本功能可能依然没有自定义手机或者root手机强大，仅限部分场景下使用。<br>
 * 不过ratel可以和配合ida或者其他框架进行调试:frida或者ida<br>
 * 使用方法：
 * 1.将ida的server文件放到插件的assets下，分别重命名为：as_adi_7_0_64（64位），as_adi_7_0（32位）
 * 2.调用方法 startupIdaDebug(int debugPort)传入特定端口
 * 3.adb forward
 * 4.ida附加调试
 */
public class IDADebug {
    private static final int LOLLIPOP = 21;
    private static final int M = 23;

    public static String tag = "IDA_DEBUGGER";

    private static final String DEFAULT_ANDROID_SERVER_7_0_64 = "as_adi_7_0_64";
    private static final String DEFAULT_ANDROID_SERVER_7_0_32 = "as_adi_7_0";

    public static void setIDaServerBindLoader(IDAServerBinLoader iDaServerBindLoader) {
        if (binLoader == null) {
            return;
        }
        binLoader = iDaServerBindLoader;
    }

    public static void startupIdaDebug(int debugPort) {
        startupIdaDebug(debugPort, false);
    }

    public static void startupIdaDebug(int debugPort, boolean waitForDebugger) {
        boolean is64 = is64bit();
        String idaBinName = is64 ? "as_adi_ratel_64" : "as_adi_ratel_32";
        File idaBinFile = new File(RatelToolKit.sContext.getFilesDir(), idaBinName);
        byte[] bytes = binLoader.loadAndroidServerBin(is64);
        if (bytes != null) {
            try {
                FileUtils.writeByteArrayToFile(idaBinFile, bytes);
            } catch (IOException e) {
                Log.e(tag, "error to load ida bin file", e);
            }
        }
        if (idaBinFile.exists() && runIdaBinProcess(idaBinFile, debugPort) && waitForDebugger) {
            Debug.waitForDebugger();
        }
    }


    private static IDAServerBinLoader binLoader = new IDAServerBinLoader() {
        @Override
        public byte[] loadAndroidServerBin(boolean is64Bit) {
            String idaBinName = is64Bit ? DEFAULT_ANDROID_SERVER_7_0_64 : DEFAULT_ANDROID_SERVER_7_0_32;
            try (InputStream inputStream = IDADebug.class.getResourceAsStream("/assets/" + idaBinName)) {
                if (inputStream == null) {
                    Log.e(tag, "can not open resource: " + idaBinName);
                    return null;
                }
                return IOUtils.toByteArray(inputStream);
            } catch (IOException e) {
                Log.e(tag, "failed to extract ida bin file", e);
            }
            return null;
        }
    };

    public interface IDAServerBinLoader {
        byte[] loadAndroidServerBin(boolean is64Bit);
    }


    public static boolean is64bit() {
        if (Build.VERSION.SDK_INT < LOLLIPOP) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= M) {
            //return Process.is64Bit();
            RposedHelpers.callStaticMethod(Process.class, "is64Bit");
        }
        Object runtime = RposedHelpers.callStaticMethod(
                RposedHelpers.findClass("dalvik.system.VMRuntime", ClassLoader.getSystemClassLoader()),
                "getRuntime"
        );
        return (boolean) RposedHelpers.callMethod(runtime, "is64Bit");
        //return VMRuntime.is64Bit.call(VMRuntime.getRuntime.call());
    }

    private static boolean runIdaBinProcess(final File binFile, int debugPort) {
        if (debugPort <= 0) {
            debugPort = 4689;
        }
        if (!binFile.canExecute()) {
            if (!binFile.setExecutable(true)) {
                Log.w(tag, "set executable failed for file:  " + binFile.getAbsolutePath());
            }
        }
        final int finalDebugPort = debugPort;
        new Thread("adi") {
            @Override
            public void run() {
                try {
                    java.lang.Process process = Runtime.getRuntime().exec(new String[]{
                            binFile.getAbsolutePath(), "-p" + finalDebugPort
                    });
                    Log.i(tag, "ida debug process: " + process + " onPort:" + finalDebugPort);
                    process.waitFor();
                } catch (IOException e) {
                    Log.e(tag, "start process failed", e);
                } catch (InterruptedException interruptedException) {
                    Log.e(tag, "interruptedException", interruptedException);
                }
            }
        }.start();
        return true;
    }

}
