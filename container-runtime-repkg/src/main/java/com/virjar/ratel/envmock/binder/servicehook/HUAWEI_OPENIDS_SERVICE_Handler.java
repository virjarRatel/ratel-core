package com.virjar.ratel.envmock.binder.servicehook;

import android.os.Parcel;

import com.virjar.ratel.envmock.binder.BinderHookParcelHandler;

public class HUAWEI_OPENIDS_SERVICE_Handler {

    public static final String targetPackage = "com.huawei.hwid";
    public static final String action = "com.uodis.opendevice.OPENIDS_SERVICE";

    public static class GetOaid implements BinderHookParcelHandler {

        @Override
        public Object[] parseInvokeParam(String descriptor, Parcel parcel) {
            return new Object[0];
        }

        @Override
        public void writeParamsToParcel(String descriptor, Parcel parcel, Object[] args) {
            parcel.writeInterfaceToken(descriptor);
        }

        @Override
        public Object parseInvokeResult(Parcel parcel) {
            return parcel.readString();
        }

        @Override
        public void writeNewResultToParcel(Parcel parcel, Object object) {
            parcel.writeNoException();
            parcel.writeString((String) object);
        }

        @Override
        public String name() {
            return ServiceIPCHookManager.genInterfaceKey(targetPackage, action);
        }

        @Override
        public String method() {
            //只有code，无法找到布局文件，通过transactCode来定位方法
            return "TRANSACTION_getOaid";
        }

        @Override
        public int transactCode() {
            return 1;
        }

    }

    public static void addHandlers() {
        // BinderHookManager.addParcelHandler(new WifiManagerHandler.GetConnectionInfo());
        ServiceIPCHookManager.addParcelHandler(new GetOaid());
    }
}
