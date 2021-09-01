package com.virjar.ratel.envmock;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.NativeBridge;
import com.virjar.ratel.RatelNative;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.SuffixTrimUtils;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.hook.RatelHookFlagMethodCallback;
import com.virjar.ratel.runtime.RatelEnvironment;
import com.virjar.ratel.runtime.RatelRuntime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import external.org.apache.commons.io.FileUtils;

public class TimeFake {

    private static Integer uptimeMillisDiff = null;
    private static Integer elapsedRealtimeDiff = null;

    public static void fakeTime() {

        //TODO 这个不能hook，目前会发现导致app严重卡顿
//        // 非休眠时间
//        RposedHelpers.findAndHookMethodSafe(SystemClock.class, "uptimeMillis", new RatelHookFlagMethodCallback() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                long uptimeMillis = (long) param.getResult();
//                if (uptimeMillisDiff == null) {
//                    PropertiesStoreHolder propertiesStoreHolder = PropertiesStoreHolder.getPropertiesHolder(FileManager.timeMockFile());
//                    String uptimeMillisDiffStr = propertiesStoreHolder.getProperty(Constants.uptimeMillisDiffKey);
//                    if (TextUtils.isEmpty(uptimeMillisDiffStr)) {
//                        uptimeMillisDiffStr = String.valueOf((int) (SuffixTrimUtils.getRandom().nextGaussian() * 30000));
//                        propertiesStoreHolder.setProperty(Constants.uptimeMillisDiffKey, uptimeMillisDiffStr);
//                    }
//                    uptimeMillisDiff = Integer.parseInt(uptimeMillisDiffStr);
//                }
//                param.setResult(uptimeMillis + uptimeMillisDiff);
//            }
//        });

        //系统从启动到现在的时间
        RposedHelpers.findAndHookMethodSafe(SystemClock.class, "elapsedRealtime", new RatelHookFlagMethodCallback() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                long elapsedRealtime = (long) param.getResult();
                param.setResult(elapsedRealtime + getElapsedRealtimeDiff());
            }
        });

        //系统从启动到现在的时间
        RposedHelpers.findAndHookMethodSafe(SystemClock.class, "elapsedRealtimeNanos", new RatelHookFlagMethodCallback() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                long elapsedRealtime = (long) param.getResult();
                param.setResult(elapsedRealtime + getElapsedRealtimeDiff() * 1000000);
            }
        });


        fakeBuildProp();

        fakeSourceDir();

        fakeLibDir();
    }

    private static void fakeLibDir() {
        File libFile = new File(RatelToolKit.sContext.getFilesDir().getParentFile(), "lib");
        if (!libFile.exists() || !libFile.canRead()) {
            return;
        }

        long lastModified = libFile.lastModified();
        PropertiesStoreHolder propertiesStoreHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.timeMockFile());
        String key = "fake_install_time_" + RatelRuntime.nowPackageName;
        String installTimeStr = propertiesStoreHolder.getProperty(key);

        if (TextUtils.isEmpty(installTimeStr)) {
            installTimeStr = String.valueOf(SuffixTrimUtils.getRandom().nextInt(120000) + RatelRuntime.getH().firstInstallTime);
            propertiesStoreHolder.setProperty(key, installTimeStr);
        }
        long diff = Long.parseLong(installTimeStr) - RatelRuntime.getH().firstInstallTime;

        long fakeLastModified = lastModified + diff;

        RatelNative.mockPathStat(libFile.getAbsolutePath(), fakeLastModified);
    }


    private static void fakeSourceDir() {
        File originSourceDir = new File(RatelRuntime.getOriginApplicationInfo().sourceDir);
        long sourceDirLastModified = originSourceDir.lastModified();

        PropertiesStoreHolder propertiesStoreHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.timeMockFile());
        String key = "fake_install_time_" + RatelRuntime.nowPackageName;
        String installTimeStr = propertiesStoreHolder.getProperty(key);

        if (TextUtils.isEmpty(installTimeStr)) {
            installTimeStr = String.valueOf(SuffixTrimUtils.getRandom().nextInt(120000) + RatelRuntime.getH().firstInstallTime);
            propertiesStoreHolder.setProperty(key, installTimeStr);
        }
        long diff = Long.parseLong(installTimeStr) - RatelRuntime.getH().firstInstallTime;

        long fakeLastModified = sourceDirLastModified + diff;

        File fakeSourceDirApk = RatelEnvironment.originApkDir();
        if (!fakeSourceDirApk.setLastModified(fakeLastModified)) {
            Log.w(Constants.TAG, "set up last modified for fake apk failed:" + fakeSourceDirApk.getAbsolutePath());
        }
    }


    private static void fakeBuildProp() {
        File buildPropFile = new File("/system/build.prop");
        if (!buildPropFile.exists()) {
            return;
        }
        File fakeBuildPropFile = RatelEnvironment.buildPropMockFile();
        long originLastModified = buildPropFile.lastModified();
        RatelNative.redirectFile(buildPropFile.getAbsolutePath(), fakeBuildPropFile.getAbsolutePath());


        if (fakeBuildPropFile.exists()) {
            RatelToolKit.addOnRatelStartUpCallback(() -> {
                RatelNative.readOnlyFile(fakeBuildPropFile.getAbsolutePath());
            });
            return;
        }
        if (!buildPropFile.canRead()) {
            try {
                FileUtils.writeStringToFile(fakeBuildPropFile, "fuck=fake", StandardCharsets.UTF_8);
            } catch (IOException e) {
                Log.e(Constants.TAG, "failed to create fake buildPropFile file", e);
            }

        } else {
            try {
                FileUtils.copyFile(buildPropFile, RatelEnvironment.buildPropMockFile());
            } catch (IOException e) {
                Log.w(Constants.TAG, "copy /system/build.prop failed!!", e);
                return;
            }
        }
        RatelToolKit.addOnRatelStartUpCallback(() -> {
            if (!fakeBuildPropFile.canRead()) {
                return;
            }
            try {
                rebuildBuildProperties();
            } catch (IOException e) {
                Log.w(Constants.TAG, "rebuild /system/build.prop failed!!", e);
            }

            if (!buildPropFile.setLastModified(originLastModified + SuffixTrimUtils.randomMockDiff(2048))) {
                Log.w(Constants.TAG, "update last usage for fake /system/build.prob failed");
            }
            RatelNative.readOnlyFile(fakeBuildPropFile.getAbsolutePath());
        });

    }

    private static void rebuildBuildProperties() throws IOException {
        StringBuilder newOut = new StringBuilder();
        char[] convtBuf = new char[1024];
        String line;
        int keyLen;
        int valueStart;
        char c;
        boolean hasSep;
        boolean precedingBackslash;
        BufferedReader lr = new BufferedReader(new InputStreamReader(new FileInputStream(RatelEnvironment.buildPropMockFile())));
        while ((line = lr.readLine()) != null) {

            if (line.trim().isEmpty()) {
                newOut.append("\n");
                continue;
            }
            if (line.trim().startsWith("#")) {
                newOut.append(line).append("\n");
                continue;
            }
            line = line.trim();

            keyLen = 0;
            valueStart = line.length();
            hasSep = false;

            //System.out.println("line=<" + new String(lineBuf, 0, limit) + ">");
            precedingBackslash = false;
            while (keyLen < line.length()) {
                c = line.charAt(keyLen);
                //need check if escaped.
                if ((c == '=' || c == ':') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    hasSep = true;
                    break;
                }
                // Android-changed: use of Character.isWhitespace(c) b/25998006
                else if (Character.isWhitespace(c) && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    break;
                }
                if (c == '\\') {
                    precedingBackslash = !precedingBackslash;
                } else {
                    precedingBackslash = false;
                }
                keyLen++;
            }
            while (valueStart < line.length()) {
                c = line.charAt(valueStart);
                // Android-changed: use of Character.isWhitespace(c) b/25998006
                if (!Character.isWhitespace(c)) {
                    if (!hasSep && (c == '=' || c == ':')) {
                        hasSep = true;
                    } else {
                        break;
                    }
                }
                valueStart++;
            }
            char[] chars = line.toCharArray();
            String key = loadConvert(chars, 0, keyLen, convtBuf);
            String value = loadConvert(chars, valueStart, chars.length - valueStart, convtBuf);

            String newValue = NativeBridge.onGetSystemProperties(key, value);
            if (newValue != null) {
                String newLine = saveConvert(key, false, false) + "=" + saveConvert(value, false, false);
                newOut.append(newLine).append("\n");
            } else {
                newOut.append(line).append("\n");
            }
        }

        FileUtils.writeStringToFile(RatelEnvironment.buildPropMockFile(), newOut.toString(), StandardCharsets.UTF_8);
    }


    private synchronized static int getElapsedRealtimeDiff() {
        if (elapsedRealtimeDiff != null) {
            return elapsedRealtimeDiff;
        }
        PropertiesStoreHolder propertiesStoreHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.timeMockFile());
        String elapsedRealtimeStr = propertiesStoreHolder.getProperty(Constants.elapsedRealtimeDiffKey);
        if (TextUtils.isEmpty(elapsedRealtimeStr)) {
            elapsedRealtimeStr = String.valueOf(Math.abs((int) (SuffixTrimUtils.getRandom().nextGaussian() * 60000)));
            propertiesStoreHolder.setProperty(Constants.elapsedRealtimeDiffKey, elapsedRealtimeStr);
        }
        elapsedRealtimeDiff = Integer.parseInt(elapsedRealtimeStr);
        return elapsedRealtimeDiff;
    }

    public static void fakeInstallTime(PackageInfo packageInfo) {
        String packageName = packageInfo.packageName;
        //过滤系统app
        if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
            return;
        }
        if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            return;
        }
        PropertiesStoreHolder propertiesStoreHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.timeMockFile());
        String key = "fake_install_time_" + packageName.trim();
        String installTimeStr = propertiesStoreHolder.getProperty(key);

        if (TextUtils.isEmpty(installTimeStr)) {
            installTimeStr = String.valueOf(SuffixTrimUtils.getRandom().nextInt(120000) + packageInfo.firstInstallTime);
            propertiesStoreHolder.setProperty(key, installTimeStr);
        }
        long originFirstInstallTime = packageInfo.firstInstallTime;
        packageInfo.firstInstallTime = Long.parseLong(installTimeStr);
        if (originFirstInstallTime == packageInfo.lastUpdateTime) {
            packageInfo.lastUpdateTime = packageInfo.firstInstallTime;
        } else {
            packageInfo.lastUpdateTime = packageInfo.lastUpdateTime + packageInfo.firstInstallTime - originFirstInstallTime;
        }
    }


    private static String loadConvert(char[] in, int off, int len, char[] convtBuf) {
        if (convtBuf.length < len) {
            int newLen = len * 2;
            if (newLen < 0) {
                newLen = Integer.MAX_VALUE;
            }
            convtBuf = new char[newLen];
        }
        char aChar;
        char[] out = convtBuf;
        int outLen = 0;
        int end = off + len;

        while (off < end) {
            aChar = in[off++];
            if (aChar == '\\') {
                aChar = in[off++];
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = in[off++];
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed \\uxxxx encoding.");
                        }
                    }
                    out[outLen++] = (char) value;
                } else {
                    if (aChar == 't') aChar = '\t';
                    else if (aChar == 'r') aChar = '\r';
                    else if (aChar == 'n') aChar = '\n';
                    else if (aChar == 'f') aChar = '\f';
                    out[outLen++] = aChar;
                }
            } else {
                out[outLen++] = aChar;
            }
        }
        return new String(out, 0, outLen);
    }

    /*
     * Converts unicodes to encoded &#92;uxxxx and escapes
     * special characters with a preceding slash
     */
    private static String saveConvert(String theString,
                                      boolean escapeSpace,
                                      boolean escapeUnicode) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuffer outBuffer = new StringBuffer(bufLen);

        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\');
                    outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch (aChar) {
                case ' ':
                    if (x == 0 || escapeSpace)
                        outBuffer.append('\\');
                    outBuffer.append(' ');
                    break;
                case '\t':
                    outBuffer.append('\\');
                    outBuffer.append('t');
                    break;
                case '\n':
                    outBuffer.append('\\');
                    outBuffer.append('n');
                    break;
                case '\r':
                    outBuffer.append('\\');
                    outBuffer.append('r');
                    break;
                case '\f':
                    outBuffer.append('\\');
                    outBuffer.append('f');
                    break;
                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    outBuffer.append('\\');
                    outBuffer.append(aChar);
                    break;
                default:
                    if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >> 8) & 0xF));
                        outBuffer.append(toHex((aChar >> 4) & 0xF));
                        outBuffer.append(toHex(aChar & 0xF));
                    } else {
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }

    /**
     * Convert a nibble to a hex character
     *
     * @param nibble the nibble to convert.
     */
    private static char toHex(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }

    /**
     * A table of hex digits
     */
    private static final char[] hexDigit = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
}
