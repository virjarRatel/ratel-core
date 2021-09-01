package com.virjar.ratel.envmock;

import android.provider.Settings;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.FingerPrintModel;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.envmock.providerhook.SettingsProviderHook;
import com.virjar.ratel.hook.RatelHookFlagMethodCallback;
import com.virjar.ratel.runtime.RatelEnvironment;

import java.io.File;

public class SettingsFake {
    public static void fakeSettings() {
//android.content.ContentProviderProxy#call
        //Class<?> contentProviderProxyClass = XposedHelpers.findClassIfExists("android.content.ContentProviderProxy", ClassLoader.getSystemClassLoader());
        //首先尝试在ipc层面hook

        Class<?> settingNameValueCacheClass = RposedHelpers.findClassIfExists("android.provider.Settings$NameValueCache", ClassLoader.getSystemClassLoader());
        if (settingNameValueCacheClass == null) {
            Log.w(Constants.TAG, "can not found android.provider.Settings$NameValueCache settings fake failed");
            return;
        }

        final Object nameValueCacheSystem = RposedHelpers.getStaticObjectField(Settings.System.class, "sNameValueCache");
        final Object nameValueCacheGlobal = RposedHelpers.getStaticObjectField(Settings.Global.class, "sNameValueCache");
        final Object nameValueCacheSecure = RposedHelpers.getStaticObjectField(Settings.Secure.class, "sNameValueCache");


        RposedBridge.hookAllMethods(settingNameValueCacheClass, "getStringForUser", new RatelHookFlagMethodCallback() {

            private String relocateGet(String k, String v, MethodHookParam param) {
                if (v == null) {
                    return null;
                }

                File propertiesHolderFile;
                Object thisObject = param.thisObject;

                String newValue = v;
                if (thisObject == nameValueCacheGlobal) {
                    propertiesHolderFile = RatelEnvironment.settingsGlobalMockFile();
                    if (RatelToolKit.fingerPrintModel.settingsGlobalFake.containsKey(k)) {
                        newValue = RatelToolKit.fingerPrintModel.settingsGlobalFake.get(k);
                    }
                } else if (thisObject == nameValueCacheSystem) {
                    propertiesHolderFile = RatelEnvironment.settingsSystemMockFile();
                    if (RatelToolKit.fingerPrintModel.settingsSystemFake.containsKey(k)) {
                        newValue = RatelToolKit.fingerPrintModel.settingsSystemFake.get(k);
                    }
                } else if (thisObject == nameValueCacheSecure) {
                    propertiesHolderFile = RatelEnvironment.settingsSecureMockFile();
                    if (RatelToolKit.fingerPrintModel.settingsSecureFake.containsKey(k)) {
                        newValue = RatelToolKit.fingerPrintModel.settingsSecureFake.get(k);
                    }
                } else {
                    Log.w(Constants.TAG, "get string for unknown nameValueCache:" + thisObject);
                    return v;
                }

                PropertiesStoreHolder propertiesHolder = PropertiesStoreHolder.getPropertiesHolder(propertiesHolderFile);
                if (propertiesHolder.hasProperty(k)) {
                    return propertiesHolder.getProperty(k);
                }
                if (thisObject == nameValueCacheSystem && !FingerPrintModel.settingsSystemAndroidKey.contains(k)) {
                    //系统属性自定义参数外部的，默认不允许放行
                    return null;
                }
                propertiesHolder.setProperty(k, newValue);
                return newValue;
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                String k = (String) param.args[1];
                String v = (String) param.getResult();
                if (v == null) {
                    return;
                }
                String finalValue = relocateGet(k, v, param);
                if (!v.equals(finalValue)) {
                    param.setResult(finalValue);
                }
                Log.i(Constants.TAG, "get settings value for key: " + k + " value: " + v + "  new value: " + finalValue);
            }
        });

        PropertiesStoreHolder propertiesHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.settingsSecureMockFile());
        propertiesHolder.setProperty(Settings.Secure.ANDROID_ID, SettingsProviderHook.fakeAndroidId);
        if (!RatelToolKit.fingerPrintModel.settingsSecureFake.containsKey(Settings.Secure.ANDROID_ID)) {
            RatelToolKit.fingerPrintModel.settingsSecureFake.put(Settings.Secure.ANDROID_ID, SettingsProviderHook.fakeAndroidId);
        }
    }
}
