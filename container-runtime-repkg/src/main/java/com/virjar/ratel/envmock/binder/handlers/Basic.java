package com.virjar.ratel.envmock.binder.handlers;

import android.os.Parcel;

import com.virjar.ratel.envmock.binder.BinderHookManager;
import com.virjar.ratel.envmock.binder.BinderHookParcelHandler;

public class Basic {
    public static class S_SHandler implements BinderHookParcelHandler {
        private String name;
        private String method;

        public S_SHandler(String name, String method) {
            this.name = name;
            this.method = method;
        }

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
            return parcel.readString();
        }

        @Override
        public void writeNewResultToParcel(Parcel parcel, Object object) {
            parcel.writeNoException();
            parcel.writeString((String) object);
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String method() {
            return method;
        }

        @Override
        public int transactCode() {
            return -1;
        }
    }

    public static class S_VHandler implements BinderHookParcelHandler {
        private String name;
        private String method;

        public S_VHandler(String name, String method) {
            this.name = name;
            this.method = method;
        }

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
            return parcel.readString();
        }

        @Override
        public void writeNewResultToParcel(Parcel parcel, Object object) {
            parcel.writeNoException();
            parcel.writeString((String) object);
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String method() {
            return method;
        }

        @Override
        public int transactCode() {
            return -1;
        }
    }

    public static class S_ISHandler implements BinderHookParcelHandler {
        private String name;
        private String method;

        public S_ISHandler(String name, String method) {
            this.name = name;
            this.method = method;
        }

        @Override
        public Object[] parseInvokeParam(String descriptor, Parcel parcel) {
            Object[] objects = new Object[2];
            BinderHookManager.consumeEnforceInterface(parcel);
            objects[0] = parcel.readInt();//slotIndex
            objects[1] = parcel.readString();//callingPackage
            return objects;
        }

        @Override
        public void writeParamsToParcel(String descriptor, Parcel parcel, Object[] args) {
            parcel.writeInterfaceToken(descriptor);
            parcel.writeInt((Integer) args[0]);
            parcel.writeString((String) args[1]);
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
            return name;
        }

        @Override
        public String method() {
            return method;
        }

        @Override
        public int transactCode() {
            return -1;
        }
    }


    public static class I_Handler implements BinderHookParcelHandler {
        private final String name;
        private final String method;

        public I_Handler(String name, String method) {
            this.name = name;
            this.method = method;
        }

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
            return parcel.readInt();
        }

        @Override
        public void writeNewResultToParcel(Parcel parcel, Object object) {
            parcel.writeNoException();
            parcel.writeInt((Integer) object);
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String method() {
            return method;
        }

        @Override
        public int transactCode() {
            return -1;
        }
    }
}
