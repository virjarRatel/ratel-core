package com.virjar.ratel.envmock;

import android.util.Log;

import com.virjar.ratel.RatelNative;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.hook.RatelHookFlagMethodCallback;
import com.virjar.ratel.runtime.RatelEnvironment;

import java.util.Map;

import external.org.apache.commons.lang3.StringUtils;

public class SystemPropertiesFake {


    public static void fakeSystemProperties() {
        Map<String, String> systemPropertiesFake = RatelToolKit.fingerPrintModel.systemPropertiesFakeMap;
        if (systemPropertiesFake == null) {
            return;
        }
        for (Map.Entry<String, String> entry : systemPropertiesFake.entrySet()) {
            RatelNative.nativeAddMockSystemProperty(entry.getKey(), entry.getValue());
        }

        if (systemPropertiesClass == null) {
            Log.w(Constants.TAG, "can not find class: android.os.SystemProperties,disable system properties fake ");
        } else {
            RposedHelpers.findAndHookMethod(systemPropertiesClass, "get", String.class, new RatelHookFlagMethodCallback() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    String propertiesKey = (String) param.args[0];
                    String newRet = onGetSystemProperties(propertiesKey, (String) param.getResult());
                    if (newRet != null) {
                        param.setResult(newRet);
                    }
                }
            });
        }

//        // Build 缓存
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//            //小于O才会在android.os.SystemProperties中有数据
//            String serialNo = get(FingerPrintModel.serialKey);
//            if (StringUtils.isNotBlank(Build.SERIAL) && !StringUtils.equals(serialNo, Build.SERIAL)) {
//                RposedHelpers.setStaticObjectField(Build.class, "SERIAL", serialNo);
//            }
//        }
    }


    /**
     * TODO 迁移这个函数
     */
    private static String get(String key) {
        try {
            return (String) systemPropertiesClass.getDeclaredMethod("get", String.class).invoke(key, new Object[]{key});
        } catch (Exception e) {
            Log.w(Constants.TAG, "call system properties get error");
            throw new RuntimeException(e);
        }
    }

    private static Class<?> systemPropertiesClass = RposedHelpers.findClassIfExists("android.os.SystemProperties", ClassLoader.getSystemClassLoader());

    public static String onGetSystemProperties(String key, String value) {
        String newValue = onGetSystemPropertiesInternal(key, value);
//        if (RatelRuntime.isRatelDebugBuild) {
//            //Log.i("SystemPropertiesHook", "get system properties key: " + key + "  value: " + value + "  replace:" + newValue, new Throwable());
//            Log.i(Constants.VENV_TAG, "get system properties key: " + key + "  value: " + value + "  replace:" + newValue, new Throwable());
//        }
        return newValue;

    }

    private static String onGetSystemPropertiesInternal(String key, String value) {
        // Log.i(Constants.VENV_TAG, "get property ", new Throwable());
        if (StringUtils.isBlank(value)) {
            //如果本来就是空，那么返回空
            return value;
        }
        PropertiesStoreHolder propertiesHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.systemPropertiesFakeFile());
        String cacheValue = propertiesHolder.getProperty(key);
        if (cacheValue != null) {
            return cacheValue;
        }
        PropertiesMockItem propertiesMockItem = RatelNative.nativeQueryMockSystemProperty(key);
        if (propertiesMockItem == null) {
            return value;
        }


        String replace = propertiesMockItem.getProperties_value();
        if (value.equals(replace)) {
            return value;
        }


        if (replace != null) {
            propertiesHolder.setProperty(key, replace);
        }
        return replace;
    }
}
