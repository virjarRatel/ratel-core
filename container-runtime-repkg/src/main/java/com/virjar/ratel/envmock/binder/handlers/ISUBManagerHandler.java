package com.virjar.ratel.envmock.binder.handlers;

import android.os.Parcel;

import com.virjar.ratel.envmock.binder.BinderHookManager;
import com.virjar.ratel.envmock.binder.BinderHookParcelHandler;

import java.util.ArrayList;

public class ISUBManagerHandler {
    public static final String service = "isub";
    //private static final java.lang.String DESCRIPTOR = "com.android.internal.telephony.ISub";

    public static class GetActiveSubscriptionInfoList extends Base {

        @Override
        public Object[] parseInvokeParam(String descriptor, Parcel parcel) {
            BinderHookManager.consumeEnforceInterface(parcel);
            Object[] args = new Object[1];
            args[0] = parcel.readString();
            return args;
        }

        @Override
        public void writeParamsToParcel(String descriptor, Parcel parcel, Object[] args) {
            parcel.writeInterfaceToken(descriptor);
            parcel.writeString((String) args[0]);
        }

        @Override
        public Object parseInvokeResult(Parcel parcel) {
            return parcel.createTypedArrayList(BinderHookManager.getCreator("android.telephony.SubscriptionInfo"));
        }

        @Override
        public void writeNewResultToParcel(Parcel parcel, Object object) {
            parcel.writeNoException();
            parcel.writeTypedList((ArrayList) object);
        }

        @Override
        public String method() {
            return "TRANSACTION_getActiveSubscriptionInfoList";
        }
    }

    private static abstract class Base implements BinderHookParcelHandler {
        @Override
        public String name() {
            return service;
        }

        @Override
        public int transactCode() {
            return -1;
        }
    }

    public static void addHandlers() {
        BinderHookManager.addParcelHandler(new GetActiveSubscriptionInfoList());
    }
}
