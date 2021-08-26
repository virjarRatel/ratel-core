package com.virjar.ratel.envmock.binder.handlers;

import android.os.Build;

import com.virjar.ratel.api.SDK_VERSION_CODES;
import com.virjar.ratel.envmock.binder.BinderHookManager;
import com.virjar.ratel.envmock.binder.BinderHookParcelHandler;

public class IPhoneSubInfoManagerHandler {
    public static final String service = "iphonesubinfo";
    //private static final java.lang.String DESCRIPTOR = "com.android.internal.telephony.IPhoneSubInfo";


    private static abstract class Base implements BinderHookParcelHandler {
        @Override
        public String name() {
            return service;
        }
    }

    public static void addHandlers() {
        BinderHookManager.addParcelHandler(new Basic.S_SHandler(service, "TRANSACTION_getIccSerialNumber"));
        BinderHookManager.addParcelHandler(new Basic.S_ISHandler(service, "TRANSACTION_getSubscriberIdForSubscriber"));
        if (Build.VERSION.SDK_INT >= SDK_VERSION_CODES.M) {
            BinderHookManager.addParcelHandler(new Basic.S_SHandler(service, "TRANSACTION_getSubscriberId"));
        } else {
            BinderHookManager.addParcelHandler(new Basic.S_VHandler(service, "TRANSACTION_getSubscriberId"));
        }
        BinderHookManager.addParcelHandler(new Basic.S_SHandler(service, "TRANSACTION_getLine1Number"));
        BinderHookManager.addParcelHandler(new Basic.S_ISHandler(service, "TRANSACTION_getDeviceIdForPhone"));
        BinderHookManager.addParcelHandler(new Basic.S_SHandler(service, "TRANSACTION_getDeviceId"));
    }
}
