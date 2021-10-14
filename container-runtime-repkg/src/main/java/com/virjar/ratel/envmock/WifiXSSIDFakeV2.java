package com.virjar.ratel.envmock;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.envmock.binder.AfterHook;
import com.virjar.ratel.envmock.binder.BindMethodHook;
import com.virjar.ratel.envmock.binder.BinderHookManager;
import com.virjar.ratel.envmock.binder.ParcelUtils;
import com.virjar.ratel.envmock.binder.handlers.WifiManagerHandler;
import com.virjar.ratel.runtime.RatelEnvironment;
import com.virjar.ratel.runtime.RatelRuntime;

import java.util.List;

public class WifiXSSIDFakeV2 {
    private static final java.lang.String[] sInvalidMac = {"02:00:00:00:00:00", "01:80:C2:00:00:03", "12:34:56:78:9A:BC", "FF:FF:FF:FF:FF:FF", "00:00:00:00:00:00", "00:02:00:00:00:00"};

    public static void fakeWifi() {
        BinderHookManager.addBinderHook(WifiManagerHandler.service, "getScanResults", new AfterHook() {

            @SuppressWarnings("unchecked")
            @Override
            public Object afterIpcCall(Object[] args, Object result) {
                List<ScanResult> scanResultList = (List<ScanResult>) result;
                if (scanResultList == null || scanResultList.isEmpty()) {
                    return result;
                }
                for (ScanResult scanResult : scanResultList) {
                    modifyScanResult(scanResult);
                }
                return scanResultList;
            }
        });

        BinderHookManager.addBinderHook(WifiManagerHandler.service, "getConnectionInfo", new AfterHook() {
            @Override
            public Object afterIpcCall(Object[] args, Object result) {
                WifiInfo wifiInfo = (WifiInfo) result;
                if (wifiInfo == null) {
                    return result;
                }
                String bssid = wifiInfo.getBSSID();
                if (isInvalidMac(bssid)) {
                    return result;
                }

                String fakeBSSID = xorBSSID(bssid);
                RposedHelpers.setObjectField(wifiInfo, "mBSSID", fakeBSSID);
                return result;
            }
        });

        BinderHookManager.addBinderHook(WifiManagerHandler.service, "getMatchingOsuProviders", new BindMethodHook() {
            @Override
            @SuppressWarnings("unchecked")
            public void beforeIpcCall(Object[] args) {
                List<ScanResult> scanResultList = (List<ScanResult>) args[0];
                if (scanResultList.isEmpty()) {
                    return;
                }
                List<ScanResult> copyScanResult = ParcelUtils.copyListParcel(scanResultList);
                args[0] = copyScanResult;

                for (ScanResult scanResult : copyScanResult) {
                    modifyScanResult(scanResult);
                }
            }

            @Override
            public Object afterIpcCall(Object[] args, Object result) {
                return result;
            }
        });

        BinderHookManager.addBinderHook(WifiManagerHandler.service, "addOrUpdateNetwork", new BindMethodHook() {
            @Override
            public void beforeIpcCall(Object[] args) {
                WifiConfiguration wifiConfiguration = ParcelUtils.copyParcel((WifiConfiguration) args[0]);
                wifiConfiguration.BSSID = xorBSSID(wifiConfiguration.BSSID);
                args[0] = wifiConfiguration;
            }

            @Override
            public Object afterIpcCall(Object[] args, Object result) {
                return result;
            }
        });
    }

    private static void modifyScanResult(ScanResult scanResult) {
        if (isInvalidMac(scanResult.BSSID)) {
            return;
        }
        if (RatelRuntime.isRatelDebugBuild) {
            Log.i(Constants.VENV_TAG, "wifi BSSID mock new  before: " + scanResult.BSSID);
        }
        scanResult.BSSID = xorBSSID(scanResult.BSSID);
        if (RatelRuntime.isRatelDebugBuild) {
            Log.i(Constants.VENV_TAG, "wifi BSSID mock new  after: " + scanResult.BSSID);
        }
    }

    private static String xorBSSID(String bssid) {
        if (bssid.length() < "d4:ee:07:11:".length()) {
            return bssid;
        }

        String prefix = bssid.substring(0, "d4:ee:07:11:".length());
        String suffix = bssid.substring("d4:ee:07:11:".length());
        String[] segments = suffix.split(":");
        if (segments.length != 2) {
            return bssid;
        }
        return prefix + hexByteXor(segments[0]) + ":" + hexByteXor(segments[1]);
    }

    private static String hexByteXor(String hexByte) {
        int data = Integer.parseInt(hexByte, 16) ^ RatelEnvironment.userIdentifierSeed();
        String s = Integer.toHexString(data);
        if (s.length() == 1) {
            return "0" + s;
        }
        if (s.length() == 2) {
            return s;
        }
        return s.substring(s.length() - 2);
    }

    private static boolean isInvalidMac(String mac) {
        if (TextUtils.isEmpty(mac)) {
            return true;
        }
        for (String s : sInvalidMac) {
            if (s.equalsIgnoreCase(mac)) {
                return true;
            }
        }
        return false;
    }
}
