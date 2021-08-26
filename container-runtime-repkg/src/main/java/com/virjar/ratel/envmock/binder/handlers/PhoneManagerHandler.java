package com.virjar.ratel.envmock.binder.handlers;

import android.os.Parcel;

import com.virjar.ratel.envmock.binder.BinderHookManager;
import com.virjar.ratel.envmock.binder.BinderHookParcelHandler;

public class PhoneManagerHandler {
    public static final String service = "phone";
    //private static final java.lang.String DESCRIPTOR = "com.android.internal.telephony.ITelephony";


    public static class HasIccCard extends Base {

        @Override
        public Object[] parseInvokeParam(String descriptor, Parcel parcel) {
            BinderHookManager.consumeEnforceInterface(parcel);
            return new Object[0];
        }

        @Override
        public void writeParamsToParcel(String descriptor, Parcel parcel, Object[] args) {
            parcel.writeInterfaceToken(descriptor);
        }

        @Override
        public Object parseInvokeResult(Parcel parcel) {
            return (0 != parcel.readInt());
        }

        @Override
        public void writeNewResultToParcel(Parcel parcel, Object object) {
            parcel.writeNoException();
            Boolean bool = (Boolean) object;
            parcel.writeInt(bool ? 1 : 0);
        }

        @Override
        public String method() {
            return "TRANSACTION_hasIccCard";
        }
    }


    public static abstract class Base implements BinderHookParcelHandler {
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
        BinderHookManager.addParcelHandler(new Basic.S_ISHandler(service, "TRANSACTION_getImeiForSlot"));
        BinderHookManager.addParcelHandler(new HasIccCard());
        BinderHookManager.addParcelHandler(new Basic.S_SHandler(service, "TRANSACTION_getDeviceId"));
        BinderHookManager.addParcelHandler(new Basic.S_ISHandler(service, "TRANSACTION_getMeidForSlot"));
        BinderHookManager.addParcelHandler(new Basic.I_Handler(service, "TRANSACTION_getSimState"));

    }
}
