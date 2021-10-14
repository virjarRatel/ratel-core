package com.virjar.ratel.envmock.binder.handlers;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.virjar.ratel.envmock.binder.BinderHookManager;
import com.virjar.ratel.envmock.binder.BinderHookParcelHandler;

public class PackageManagerHandler {
    public static final String service = "package";
    //private static final java.lang.String DESCRIPTOR = "android.content.pm.IPackageManager";

    public static class GetInstalledPackages extends Base {
        @Override
        public Object[] parseInvokeParam(String descriptor, Parcel parcel) {
            BinderHookManager.consumeEnforceInterface(parcel);
            Object[] objects = new Object[2];
            objects[0] = parcel.readInt();//flags
            objects[1] = parcel.readInt();//userId
            return objects;
        }

        @Override
        public void writeParamsToParcel(String descriptor, Parcel parcel, Object[] args) {
            parcel.writeInterfaceToken(descriptor);
            parcel.writeInt((Integer) args[0]);
            parcel.writeInt((Integer) args[1]);
        }

        @Override
        public Object parseInvokeResult(Parcel parcel) {
            if (parcel.readInt() != 0) {
                return BinderHookManager.getCreator("android.content.pm.ParceledListSlice")
                        .createFromParcel(parcel);
            }
            return null;
        }


        @Override
        public void writeNewResultToParcel(Parcel parcel, Object object) {
            parcel.writeNoException();
            if (object != null) {
                parcel.writeInt(1);
                ((Parcelable) object).writeToParcel(parcel, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
            } else {
                parcel.writeInt(0);
            }
        }


        @Override
        public String method() {
            return "TRANSACTION_getInstalledPackages";
        }

        @Override
        public int transactCode() {
            return -1;
        }
    }

    public static class GetInstalledApplications extends Base {
        @Override
        public Object[] parseInvokeParam(String descriptor, Parcel parcel) {
            BinderHookManager.consumeEnforceInterface(parcel);
            Object[] objects = new Object[2];
            objects[0] = parcel.readInt();//flags
            objects[1] = parcel.readInt();//userId
            return objects;
        }

        @Override
        public void writeParamsToParcel(String descriptor, Parcel parcel, Object[] args) {
            parcel.writeInterfaceToken(descriptor);
            parcel.writeInt((Integer) args[0]);
            parcel.writeInt((Integer) args[1]);
        }

        @Override
        public Object parseInvokeResult(Parcel parcel) {
            if (parcel.readInt() != 0) {
                return BinderHookManager.getCreator("android.content.pm.ParceledListSlice")
                        .createFromParcel(parcel);
            }
            return null;
        }


        @Override
        public void writeNewResultToParcel(Parcel parcel, Object object) {
            parcel.writeNoException();
            if (object != null) {
                parcel.writeInt(1);
                ((Parcelable) object).writeToParcel(parcel, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
            } else {
                parcel.writeInt(0);
            }
        }


        @Override
        public String method() {
            return "TRANSACTION_getInstalledApplications";
        }

        @Override
        public int transactCode() {
            return -1;
        }
    }

    public static class GetPackageInfo extends Base {
        @Override
        public Object[] parseInvokeParam(String descriptor, Parcel parcel) {
            BinderHookManager.consumeEnforceInterface(parcel);
            Object[] objects = new Object[3];
            objects[0] = parcel.readString();//packageName
            objects[1] = parcel.readInt();//flags
            objects[2] = parcel.readInt();//userId
            return objects;
        }

        @Override
        public void writeParamsToParcel(String descriptor, Parcel parcel, Object[] args) {
            parcel.writeInterfaceToken(descriptor);
            parcel.writeString((String) args[0]);
            parcel.writeInt((Integer) args[1]);
            parcel.writeInt((Integer) args[2]);
        }

        @Override
        public Object parseInvokeResult(Parcel parcel) {
            //Log.i(Constants.TAG,"parseInvokeResult android.content.pm.PackageInfo:" + parcel);
            if (parcel.readInt() != 0) {
                return BinderHookManager.getCreator("android.content.pm.PackageInfo")
                        .createFromParcel(parcel);
            }
            return null;
        }


        @Override
        public void writeNewResultToParcel(Parcel parcel, Object object) {
            parcel.writeNoException();
            if (object != null) {
                parcel.writeInt(1);
                ((Parcelable) object).writeToParcel(parcel, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
            } else {
                parcel.writeInt(0);
            }
        }


        @Override
        public String method() {
            return "TRANSACTION_getPackageInfo";
        }

        @Override
        public int transactCode() {
            return -1;
        }
    }


    public static class GetPackagesForUid extends Base {

        @Override
        public Object[] parseInvokeParam(String descriptor, Parcel parcel) {
            BinderHookManager.consumeEnforceInterface(parcel);
            Object[] objects = new Object[1];
            objects[0] = parcel.readInt();
            return objects;
        }

        @Override
        public void writeParamsToParcel(String descriptor, Parcel parcel, Object[] args) {
            parcel.writeInterfaceToken(descriptor);
            parcel.writeInt((Integer) args[0]);
        }

        @Override
        public Object parseInvokeResult(Parcel parcel) {
            return parcel.createStringArray();
        }

        @Override
        public void writeNewResultToParcel(Parcel parcel, Object object) {
            parcel.writeNoException();
            String[] _result = (String[]) object;
            parcel.writeStringArray(_result);
        }

        @Override
        public String method() {
            return "TRANSACTION_getPackagesForUid";
        }

        @Override
        public int transactCode() {
            return -1;
        }
    }

    public static abstract class Base implements BinderHookParcelHandler {
        @Override
        public String name() {
            return service;
        }
    }

    public static void addHandlers() {
        BinderHookManager.addParcelHandler(new GetInstalledPackages());
        BinderHookManager.addParcelHandler(new GetInstalledApplications());
        BinderHookManager.addParcelHandler(new GetPackageInfo());
        BinderHookManager.addParcelHandler(new GetPackagesForUid());
    }
}
