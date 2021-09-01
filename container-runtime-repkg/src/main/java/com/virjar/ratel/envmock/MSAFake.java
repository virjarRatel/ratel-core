package com.virjar.ratel.envmock;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.SuffixTrimUtils;
import com.virjar.ratel.api.inspect.ClassLoadMonitor;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.runtime.MultiUserManager;
import com.virjar.ratel.runtime.RatelEnvironment;
import com.virjar.ratel.runtime.RatelRuntime;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MSAFake {
    private static final String HUAWEI_AdvertisingIdClientClassName = "com.huawei.android.hms.pps.AdvertisingIdClient";

    static void doFake() {
        fake4Huawei();
    }

    private static void fake4Huawei() {
        if (!"HUAWEI".equalsIgnoreCase(Build.MANUFACTURER)) {
            return;
        }

        ClassLoadMonitor.addClassLoadMonitor(HUAWEI_AdvertisingIdClientClassName, clazz -> {
            Log.i(Constants.TAG, "on class Init class: " + clazz.getName() + " load, classLoader: " + clazz.getClassLoader());
            RposedHelpers.findAndHookMethod(clazz, "getAdvertisingIdInfo", Context.class, new RC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object AdvertisingIdClient_Info = param.getResult();
                    if (AdvertisingIdClient_Info == null) {
                        return;
                    }
                    String advertisingId = RposedHelpers.getObjectField(AdvertisingIdClient_Info, "advertisingId");

                    advertisingId = mockMSA(advertisingId);
                    RposedHelpers.setObjectField(AdvertisingIdClient_Info, "advertisingId", advertisingId);

                }
            });
        });

    }

    public static String mockMSA(String input) {
        if (input == null) {
            return input;
        }
        //华为是一个uuid

        String uuid = UUID.nameUUIDFromBytes(("HUAWEI_OAID:" + RatelToolKit.userIdentifierSeedInt).getBytes(StandardCharsets.UTF_8)).toString();

        Log.i(Constants.TAG, "fake oaid for : " + Build.MANUFACTURER + " from:" + input + " to: " + uuid);
        return uuid;
//        PropertiesStoreHolder propertiesHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.MSAMockFile());
//
//        String key = "fake_" + input;
//        if (propertiesHolder.hasProperty(key)) {
//            input = propertiesHolder.getProperty(key);
//        } else {
//            input = SuffixTrimUtils.mockSuffix(input, 7);
//            propertiesHolder.setProperty(key, input);
//        }
//        Log.i(Constants.TAG, "fake oaid for : " + Build.MANUFACTURER);
//        return input;
    }

}
