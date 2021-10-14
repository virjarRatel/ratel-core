package com.virjar.ratel.envmock;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.FingerPrintModel;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.api.rposed.callbacks.RCallback;
import com.virjar.ratel.envmock.idfake.ActiveSubscriptionInfoListReplaceHandler;
import com.virjar.ratel.envmock.idfake.DeviceIdReplaceHandler;
import com.virjar.ratel.envmock.idfake.ImeiReplaceHandler;
import com.virjar.ratel.hook.RatelHookFlagMethodCallback;

import java.util.List;

public class TelephonyManagerFake {
    /**
     * android.os.IDeviceIdentifiersPolicyService$Stub$Proxy
     * com.android.internal.telephony.IPhoneSubInfo$Stub$Proxy
     * com.android.internal.telephony.ITelephony$Stub$Proxy
     */

    private static final String phoneSubInfoBinderProxyStubClassName = "com.android.internal.telephony.IPhoneSubInfo$Stub$Proxy";
    private static final String telephonyBinderProxyStubClassName = "com.android.internal.telephony.ITelephony$Stub$Proxy";
    private static final String isubProxyStubClassName = "com.android.internal.telephony.ISub$Stub$Proxy";


    //com.android.internal.telephony.IPhoneSubInfo.Stub.Proxy#getIccSerialNumber 这是：IMSI
    //com.android.internal.telephony.IPhoneSubInfo#getDeviceId 唯一设备号(GSM为IMEI，CDMA为MEID或ESN)
    private static Class phoneSubInfoBinderProxyStubClass = RposedHelpers.findClassIfExists(phoneSubInfoBinderProxyStubClassName, ClassLoader.getSystemClassLoader());

    private static Class telephonyBinderProxyStubClass = RposedHelpers.findClassIfExists(telephonyBinderProxyStubClassName, ClassLoader.getSystemClassLoader());

    private static Class isubBinderProxyStubClass = null;

    public static void fakeTelephony() {
        handlePhoneSubInfo();
        handleTelephony();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            isubBinderProxyStubClass = RposedHelpers.findClassIfExists(isubProxyStubClassName, ClassLoader.getSystemClassLoader());
            handleISub();
        }

    }

    private static class DeviceIdReplaceHook extends RatelHookFlagMethodCallback {

        private DeviceIdReplaceHandler deviceIdReplaceHandler;

        DeviceIdReplaceHook(String key, FingerPrintModel.ValueHolder<String> valueHolder) {
            super(RCallback.PRIORITY_HIGHEST * 2);
            deviceIdReplaceHandler = new DeviceIdReplaceHandler(key, valueHolder);
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            String originDeviceInfo = (String) param.getResult();
            param.setResult(deviceIdReplaceHandler.doReplace(originDeviceInfo));
        }
    }

    private static void handlePhoneSubInfo() {
        if (phoneSubInfoBinderProxyStubClass == null) {
            Log.w(Constants.TAG, "can not find ipc class: " + phoneSubInfoBinderProxyStubClassName);
            return;
        }

        //89860318760102789213
        RposedBridge.hookAllMethodsSafe(phoneSubInfoBinderProxyStubClass, "getIccSerialNumber",
                new DeviceIdReplaceHook("iccSerialNumber", RatelToolKit.fingerPrintModel.iccSerialNumber));
        RposedBridge.hookAllMethodsSafe(TelephonyManager.class, "getSimSerialNumber",
                new DeviceIdReplaceHook("iccSerialNumber", RatelToolKit.fingerPrintModel.iccSerialNumber));


        //460110021513051
        RposedBridge.hookAllMethodsSafe(phoneSubInfoBinderProxyStubClass, "getSubscriberIdForSubscriber",
                new DeviceIdReplaceHook("subscriberId", RatelToolKit.fingerPrintModel.subscriberId));
        RposedBridge.hookAllMethodsSafe(TelephonyManager.class, "getSubscriberId",
                new DeviceIdReplaceHook("subscriberId", RatelToolKit.fingerPrintModel.subscriberId));


        //这是手机号码
        RposedBridge.hookAllMethodsSafe(phoneSubInfoBinderProxyStubClass, "getLine1Number",
                new DeviceIdReplaceHook("line1Number", RatelToolKit.fingerPrintModel.line1Number));
        RposedBridge.hookAllMethodsSafe(TelephonyManager.class, "getLine1Number",
                new DeviceIdReplaceHook("line1Number", RatelToolKit.fingerPrintModel.line1Number));

        //这是imei
        RposedBridge.hookAllMethodsSafe(phoneSubInfoBinderProxyStubClass, "getDeviceIdForPhone",
                new ImeiReplaceHook()
        );
        RposedBridge.hookAllMethodsSafe(phoneSubInfoBinderProxyStubClass, "getDeviceId",
                new ImeiReplaceHook()
        );
    }


    private static class ImeiReplaceHook extends RatelHookFlagMethodCallback {
        private ImeiReplaceHandler imeiReplaceHandler = new ImeiReplaceHandler();

        protected void handleAfterHookedMethod(MethodHookParam param) {
            if (!param.hasThrowable()) {
                String replace = imeiReplaceHandler.doReplace((String) param.getResult(), param.args);
                param.setResult(replace);
            }
        }


        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            handleAfterHookedMethod(param);
        }
    }

    private static void handleISub() {
        if (isubBinderProxyStubClass == null) {
            Log.w(Constants.TAG, "can not find ipc class: " + isubProxyStubClassName);
            return;
        }
        RposedBridge.hookAllMethodsSafe(isubBinderProxyStubClass, "getActiveSubscriptionInfoList",
                new RC_MethodHook() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
                    @Override
                    @SuppressWarnings("unchecked")
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        List<SubscriptionInfo> result = (List<SubscriptionInfo>) param.getResult();
                        ActiveSubscriptionInfoListReplaceHandler.doReplace(result);
                    }
                });
    }

    private static void handleTelephony() {
        if (telephonyBinderProxyStubClass == null) {
            Log.w(Constants.TAG, "can not find ipc class: " + telephonyBinderProxyStubClassName);
            return;
        }
        //IMEI
        RposedBridge.hookAllMethodsSafe(telephonyBinderProxyStubClass, "getImeiForSlot",
                new ImeiReplaceHook());
        RposedBridge.hookAllMethodsSafe(TelephonyManager.class, "getImei", new ImeiReplaceHook());


        //IMEI
        RposedBridge.hookAllMethodsSafe(telephonyBinderProxyStubClass, "getDeviceId", new ImeiReplaceHook());
        //android.telephony.TelephonyManager.getDeviceId()
        RposedBridge.hookAllMethodsSafe(TelephonyManager.class, "getDeviceId", new ImeiReplaceHook());


        //电信读这个值
        RposedBridge.hookAllMethodsSafe(telephonyBinderProxyStubClass, "getMeidForSlot",
                new DeviceIdReplaceHook("meid", RatelToolKit.fingerPrintModel.meid));
        RposedBridge.hookAllMethodsSafe(TelephonyManager.class, "getMeid",
                new DeviceIdReplaceHook("meid", RatelToolKit.fingerPrintModel.meid));

    }
}
