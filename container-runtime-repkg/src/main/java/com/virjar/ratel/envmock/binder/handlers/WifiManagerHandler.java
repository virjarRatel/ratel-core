package com.virjar.ratel.envmock.binder.handlers;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Parcel;
import android.os.Parcelable;

import com.virjar.ratel.envmock.binder.BinderHookManager;
import com.virjar.ratel.envmock.binder.BinderHookParcelHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WifiManagerHandler {
    public static final String service = "wifi";
    //private static final java.lang.String DESCRIPTOR = "android.net.wifi.IWifiManager";

    public static class GetScanResults extends Base {
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
            return parcel.createTypedArrayList(BinderHookManager.getCreator("android.net.wifi.ScanResult"));
        }

        @Override
        public void writeNewResultToParcel(Parcel parcel, Object object) {
            @SuppressWarnings("unchecked")
            List<ScanResult> scanResultList = (List<ScanResult>) object;
            parcel.writeNoException();
            parcel.writeTypedList(scanResultList);
        }

        @Override
        public String method() {
            return "TRANSACTION_getScanResults";
        }
    }

    public static class GetConnectionInfo extends Base {

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
            if (parcel.readInt() == 0) {
                return null;
            }
            return BinderHookManager.getCreator("android.net.wifi.WifiInfo").createFromParcel(parcel);
        }

        @Override
        public void writeNewResultToParcel(Parcel parcel, Object object) {
            parcel.writeNoException();
            if (object == null) {
                parcel.writeInt(0);
            } else {
                parcel.writeInt(1);
                ((Parcelable) object).writeToParcel(parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
            }
        }

        @Override
        public String method() {
            return "TRANSACTION_getConnectionInfo";
        }
    }


    public static class GetMatchingOsuProviders extends Base {

        @Override
        public Object[] parseInvokeParam(String descriptor, Parcel parcel) {
            BinderHookManager.consumeEnforceInterface(parcel);
            Object[] args = new Object[1];
            List<ScanResult> results = new ArrayList<>();
            args[0] = results;
            parcel.readTypedList(results, BinderHookManager.getCreator("android.net.wifi.ScanResult"));
            return args;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void writeParamsToParcel(String descriptor, Parcel parcel, Object[] args) {
            parcel.writeInterfaceToken(descriptor);
            parcel.writeTypedList((List<ScanResult>) args[0]);
        }

        @Override
        public Object parseInvokeResult(Parcel parcel) {
            return parcel.readHashMap(ClassLoader.getSystemClassLoader());
        }


        @Override
        @SuppressWarnings("unchecked")
        public void writeNewResultToParcel(Parcel parcel, Object object) {
            parcel.writeNoException();
            parcel.writeMap((Map<Object, Object>) object);
        }

        @Override
        public String method() {
            return "TRANSACTION_getMatchingOsuProviders";
        }
    }


    public static class AddOrUpdateNetwork extends Base {

        @Override
        public Object[] parseInvokeParam(String descriptor, Parcel parcel) {
            BinderHookManager.consumeEnforceInterface(parcel);
            Object[] ret = new Object[2];
            int i = parcel.readInt();
            if (i != 0) {
                ret[0] = BinderHookManager.getCreator("android.net.wifi.WifiConfiguration")
                        .createFromParcel(parcel);
            }
            ret[1] = parcel.readString();
            return ret;
        }

        @Override
        public void writeParamsToParcel(String descriptor, Parcel parcel, Object[] args) {
            parcel.writeInterfaceToken(descriptor);
            WifiConfiguration wifiConfiguration = (WifiConfiguration) args[0];
            String packageName = (String) args[1];
            if (wifiConfiguration != null) {
                parcel.writeInt(1);
                wifiConfiguration.writeToParcel(parcel, 0);
            } else {
                parcel.writeInt(0);
            }
            parcel.writeString(packageName);
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
        public String method() {
            return "TRANSACTION_addOrUpdateNetwork";
        }
    }

    public static class StartSoftAp extends Base {
        @Override
        public Object[] parseInvokeParam(String descriptor, Parcel parcel) {
            BinderHookManager.consumeEnforceInterface(parcel);
            Object[] ret = new Object[1];
            int i = parcel.readInt();
            if (i != 0) {
                ret[0] = BinderHookManager.getCreator("android.net.wifi.WifiConfiguration")
                        .createFromParcel(parcel);
            }
            return ret;
        }

        @Override
        public void writeParamsToParcel(String descriptor, Parcel parcel, Object[] args) {
            parcel.writeInterfaceToken(descriptor);
            WifiConfiguration wifiConfiguration = (WifiConfiguration) args[0];
            String packageName = (String) args[1];
            if (wifiConfiguration != null) {
                parcel.writeInt(1);
                wifiConfiguration.writeToParcel(parcel, 0);
            } else {
                parcel.writeInt(0);
            }
        }

        @Override
        public Object parseInvokeResult(Parcel parcel) {
            return parcel.readInt() != 0;
        }

        @Override
        public void writeNewResultToParcel(Parcel parcel, Object object) {
            parcel.writeNoException();
            parcel.writeInt((Boolean) object ? 1 : 0);
        }

        @Override
        public String method() {
            return "TRANSACTION_startSoftAp";
        }
    }


    public static class SetWifiApConfiguration extends Base {

        @Override
        public Object[] parseInvokeParam(String descriptor, Parcel parcel) {
            BinderHookManager.consumeEnforceInterface(parcel);
            Object[] ret = new Object[2];
            int i = parcel.readInt();
            if (i != 0) {
                ret[0] = BinderHookManager.getCreator("android.net.wifi.WifiConfiguration")
                        .createFromParcel(parcel);
            }
            ret[1] = parcel.readString();
            return ret;
        }

        @Override
        public void writeParamsToParcel(String descriptor, Parcel parcel, Object[] args) {
            parcel.writeInterfaceToken(descriptor);
            WifiConfiguration wifiConfiguration = (WifiConfiguration) args[0];
            String packageName = (String) args[1];
            if (wifiConfiguration != null) {
                parcel.writeInt(1);
                wifiConfiguration.writeToParcel(parcel, 0);
            } else {
                parcel.writeInt(0);
            }
            parcel.writeString(packageName);
        }

        @Override
        public Object parseInvokeResult(Parcel parcel) {
            return parcel.readInt() != 0;
        }

        @Override
        public void writeNewResultToParcel(Parcel parcel, Object object) {
            parcel.writeNoException();
            parcel.writeInt((Boolean) object ? 1 : 0);
        }

        @Override
        public String method() {
            return "TRANSACTION_setWifiApConfiguration";
        }
    }

    public static class GetWifiApConfiguration extends Base {

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
            if (parcel.readInt() == 0) {
                return null;
            }
            return BinderHookManager.getCreator("android.net.wifi.WifiConfiguration").createFromParcel(parcel);
        }

        @Override
        public void writeNewResultToParcel(Parcel parcel, Object object) {
            parcel.writeNoException();
            if (object == null) {
                parcel.writeInt(0);
            } else {
                parcel.writeInt(1);
                ((WifiConfiguration) object).writeToParcel(parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
            }
        }

        @Override
        public String method() {
            return "TRANSACTION_getWifiApConfiguration";
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
        BinderHookManager.addParcelHandler(new GetConnectionInfo());
        BinderHookManager.addParcelHandler(new GetScanResults());
        BinderHookManager.addParcelHandler(new GetMatchingOsuProviders());
        BinderHookManager.addParcelHandler(new AddOrUpdateNetwork());
        BinderHookManager.addParcelHandler(new StartSoftAp());
        BinderHookManager.addParcelHandler(new SetWifiApConfiguration());
        BinderHookManager.addParcelHandler(new GetWifiApConfiguration());
    }
}
