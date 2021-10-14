package com.virjar.ratel.runtime;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;


import com.virjar.ratel.allcommon.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import external.org.apache.commons.lang3.StringUtils;

public class RatelConfig {
    private static Properties properties = null;
    private static File theConfigFile = null;
    private static long lastUpdate = 0;

    public static void init() throws IOException {
        theConfigFile = RatelEnvironment.ratelConfigFile();
        properties = new Properties();
        FileInputStream fileInputStream = new FileInputStream(theConfigFile);
        properties.load(fileInputStream);
        fileInputStream.close();
        lastUpdate = theConfigFile.lastModified();

        RatelRuntime.originPackageName = getConfig(Constants.KEY_ORIGIN_PKG_NAME, RatelRuntime.originPackageName);
        RatelRuntime.originApplicationClassName = getConfig(Constants.KEY_ORIGIN_APPLICATION_NAME, RatelRuntime.originApplicationClassName);
        RatelRuntime.nowPackageName = getConfig(Constants.KEY_NEW_PKG_NAME, RatelRuntime.nowPackageName);
        RatelRuntime.sufferKey = getConfig(Constants.kEY_SUFFER_KEY, RatelRuntime.sufferKey);
    }

    public static void loadConfig(ApplicationInfo applicationInfo) {
        Bundle metaData = applicationInfo.metaData;
        Set<String> strings = metaData.keySet();
        boolean hasSetValue = false;
        for (String key : strings) {
            if (properties.containsKey(key)) {
                //TODO log warning
                continue;
            }

            Object value = metaData.get(key);
            if (value instanceof String) {
                String propertiesValue = (String) value;
                properties.setProperty(key, propertiesValue);
            } else if (value == null) {
                properties.setProperty(key, null);
            } else if (value instanceof Number) {
                Number number = (Number) value;
                String propertiesValue = String.format(Locale.CHINESE, "%.0f", number.doubleValue());
                properties.setProperty(key, propertiesValue);
            } else {
                properties.setProperty(key, String.valueOf(value));
            }
            hasSetValue = true;
        }
        if (hasSetValue) {
            save();
        }
    }

    public static String getConfig(String key, String defaultValue) {
        String config = getConfig(key);
        if (TextUtils.isEmpty(config)) {
            return defaultValue;
        }
        return config;
    }

    public static String getConfig(String key) {
        if (lastUpdate < theConfigFile.lastModified()) {
            reload();
        }
        return properties.getProperty(key);
    }

    private static void reload() {
        try (FileInputStream fileInputStream = new FileInputStream(theConfigFile)) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            Log.w(Constants.RATEL_WARNING_TAG, "reload properties failed ,read file: " + theConfigFile.getAbsolutePath(), e);
            throw new IllegalStateException(e);
        }
    }

    public static void setConfig(String key, String value) {
        if (RatelRuntime.isRatelDebugBuild) {
            Log.i(Constants.TAG, "set ratel config key: " + key + " value: " + value);
        }
        FileLock fileLock = null;
        FileOutputStream fileOutputStream = null;
        try {
            File lockFile = new File(RatelEnvironment.ratelResourceDir(), "lock.lock");
            if (!lockFile.exists()) {
                lockFile.createNewFile();
            }
            fileOutputStream = new FileOutputStream(lockFile);
            // block to wait lock to solve the possible of data overwrite loss caused by concurrent writing under multiple processes
            fileLock = fileOutputStream.getChannel().lock();
            Log.i(Constants.TAG, "[config] get file lock success:" + fileLock);
            if (StringUtils.equals(getConfig(key), value)) {
                return;
            }
            if (value == null) {
                properties.remove(key);
            } else {
                properties.setProperty(key, value);
            }
            save();
        } catch (Exception e) {
            Log.e(Constants.TAG, "[config] ratel config set property error:" + key + "," + value, e);
        } finally {
            try {
                if (fileLock != null) {
                    Log.e(Constants.TAG, "[config] release file lock");
                    fileLock.release();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                Log.e(Constants.TAG, "[config] release config file lock error:", e);
            }
        }
    }

    private static void save() {
        try (FileOutputStream fileOutputStream = new FileOutputStream(theConfigFile)) {
            properties.store(fileOutputStream, "auto saved by ratel");
        } catch (IOException e) {
            Log.e(Constants.TAG, "save config failed", e);
            throw new IllegalStateException(e);
        }
    }
}

