package com.virjar.ratel.envmock;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.RatelNative;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.FingerPrintModel;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.hook.RatelHookFlagMethodCallback;
import com.virjar.ratel.runtime.RatelEnvironment;

import external.org.apache.commons.lang3.StringUtils;

import static com.virjar.ratel.utils.SystemPropertiesCompat.get;

public class SerialFake {

    private static final String deviceIdentifiersPolicyServiceBinderProxyStubClassName = "android.os.IDeviceIdentifiersPolicyService$Stub$Proxy";

    private static Class deviceIdentifiersPolicyServiceBinderProxyStubClass = RposedHelpers.findClassIfExists(deviceIdentifiersPolicyServiceBinderProxyStubClassName, ClassLoader.getSystemClassLoader());


    @SuppressLint({"HardwareIds"})
    static void fakeSerial() {
        String originSerial = null;
        Log.i("HS_HOOK", "fakeSerial");
        boolean featureProperties = false;
        boolean featureBuildSerial = false;
       // boolean featureIPC = false;


        String serial = get(FingerPrintModel.serialKey);
        if (!TextUtils.isEmpty(serial)) {
            featureProperties = true;
            originSerial = serial;
        }
        //

        serial = android.os.Build.SERIAL;
        if (!TextUtils.isEmpty(serial)) {
            featureBuildSerial = true;
            originSerial = serial;
        }

        if (!TextUtils.isEmpty(originSerial)) {
            originSerial = RatelToolKit.fingerPrintModel.serial.replace(null, originSerial);
            if (TextUtils.isEmpty(originSerial)) {
                Log.w(Constants.VENV_TAG, "empty replacer for RatelToolKit.fingerPrintModel.serial setup a default");
                originSerial = FingerPrintModel.ValueHolder.defaultStringHolder().replace(null, originSerial);
            }
        }

        if (featureProperties) {
            RatelNative.nativeAddMockSystemProperty(FingerPrintModel.serialKey, originSerial);
            PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.systemPropertiesFakeFile()).setProperty(FingerPrintModel.serialKey, originSerial);
        }
        if (featureBuildSerial) {
            //Android 8.0
            RposedHelpers.setStaticObjectField(Build.class, "SERIAL", originSerial);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //如果是Android9.0  Build.getSerial();
            handleDeviceIdentifiersPolicyService();
        }
    }


    //序列号模拟
    private static void handleDeviceIdentifiersPolicyService() {
        if (deviceIdentifiersPolicyServiceBinderProxyStubClass == null) {
            Log.w(Constants.TAG, "can not find ipc class: " + deviceIdentifiersPolicyServiceBinderProxyStubClassName);
            return;
        }
        RatelHookFlagMethodCallback fakeSerialHook = new RatelHookFlagMethodCallback() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                PropertiesMockItem propertiesMockItem = RatelNative.nativeQueryMockSystemProperty(FingerPrintModel.serialKey);
                if (propertiesMockItem != null) {
                    param.setResult(propertiesMockItem.getProperties_value());
                    return;
                }

                //如果 serial有定义，那么以serial为准
                String serial = RatelToolKit.fingerPrintModel.serial.replace(null, (String) param.getResult());
                if (StringUtils.isNotBlank(serial)) {
                    PropertiesStoreHolder propertiesHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.systemPropertiesFakeFile());
                    propertiesHolder.setProperty(FingerPrintModel.serialKey, serial);
                    RatelNative.nativeAddMockSystemProperty(FingerPrintModel.serialKey, serial);
                    RatelToolKit.fingerPrintModel.serial.set(serial);
                    param.setResult(serial);
                }
            }
        };

        //public static final String SERIAL = getString("ro.serialno");
        RposedBridge.hookAllMethodsSafe(deviceIdentifiersPolicyServiceBinderProxyStubClass, "getSerial", fakeSerialHook);
        RposedHelpers.findAndHookMethodSafe(Build.class, "getSerial", fakeSerialHook);
    }
}
