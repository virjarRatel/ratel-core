package com.virjar.ratel.envmock;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.api.FingerPrintModel;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.SuffixTrimUtils;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.buildsrc.Constants;
import com.virjar.ratel.hook.RatelHookFlagMethodCallback;
import com.virjar.ratel.runtime.RatelEnvironment;
import com.virjar.ratel.runtime.RatelRuntime;

import java.util.List;
import java.util.Locale;

import external.org.apache.commons.lang3.StringUtils;

@Deprecated
public class WifiXSSIDFake {

    //android.net.wifi.IWifiManager.Stub.Proxy#getScanResults
    private static final String WifiManagerIPCClassName = "android.net.wifi.IWifiManager$Stub$Proxy";
    private static java.lang.String[] sInvalidMac = {"02:00:00:00:00:00", "01:80:C2:00:00:03", "12:34:56:78:9A:BC", "FF:FF:FF:FF:FF:FF", "00:00:00:00:00:00", "00:02:00:00:00:00"};
    private static PropertiesStoreHolder propertiesStoreHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.wifiMockFile());


    public static void fakeWifi() {
        Class<?> wifiManagerIpClazz = RposedHelpers.findClassIfExists(WifiManagerIPCClassName, ClassLoader.getSystemClassLoader());
        if (wifiManagerIpClazz == null) {
            Log.w(Constants.TAG, "can not find ipc class:" + wifiManagerIpClazz);
            return;
        }

        RatelHookFlagMethodCallback replaceScanResultListHook = new RatelHookFlagMethodCallback() {
            @Override
            @SuppressWarnings("unchecked")
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<ScanResult> scanResultList = (List<ScanResult>) param.getResult();
                if (scanResultList == null || scanResultList.isEmpty()) {
                    return;
                }
                for (ScanResult scanResult : scanResultList) {
                    if (isInvalidMac(scanResult.BSSID)) {
                        continue;
                    }
                    if (RatelRuntime.isRatelDebugBuild) {
                        Log.i(Constants.VENV_TAG, "wifi BSSID mock new  before: " + scanResult.BSSID);
                    }
                    FingerPrintModel.WIFIReplaceItem wifiReplaceItem = new FingerPrintModel.WIFIReplaceItem();
                    wifiReplaceItem.BSSID = scanResult.BSSID;
                    wifiReplaceItem.SSID = scanResult.SSID;
                    getFakeBSSID(wifiReplaceItem);

                    scanResult.BSSID = wifiReplaceItem.BSSID;
                    scanResult.SSID = wifiReplaceItem.SSID;
                    if (RatelRuntime.isRatelDebugBuild) {
                        Log.i(Constants.VENV_TAG, "wifi BSSID mock new  after: " + scanResult.BSSID);
                    }
                }

            }
        };

        //handle scanResult
        RposedBridge.hookAllMethodsSafe(wifiManagerIpClazz, "getScanResults", replaceScanResultListHook);
        RposedBridge.hookAllMethodsSafe(WifiManager.class, "getScanResults", replaceScanResultListHook);


        RatelHookFlagMethodCallback replaceScanResultHook = new RatelHookFlagMethodCallback() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                WifiInfo wifiInfo = (WifiInfo) param.getResult();
                if (wifiInfo == null) {
                    return;
                }
                String bssid = wifiInfo.getBSSID();
                if (isInvalidMac(bssid)) {
                    return;
                }

                FingerPrintModel.WIFIReplaceItem wifiReplaceItem = new FingerPrintModel.WIFIReplaceItem();
                wifiReplaceItem.BSSID = wifiInfo.getBSSID();
                wifiReplaceItem.SSID = wifiInfo.getSSID();
                getFakeBSSID(wifiReplaceItem);

                RposedHelpers.setObjectField(wifiInfo, "mBSSID", wifiReplaceItem.BSSID);
                //TODO replace
                //RposedHelpers.setObjectField(wifiInfo, "mWifiSsid", WifiSsid wifiReplaceItem.SSID);
            }
        };

        //android.net.wifi.IWifiManager.Stub.Proxy#getConnectionInfo
        RposedBridge.hookAllMethodsSafe(wifiManagerIpClazz, "getConnectionInfo", replaceScanResultHook);
        RposedBridge.hookAllMethodsSafe(WifiManager.class, "getConnectionInfo", replaceScanResultHook);

        RatelHookFlagMethodCallback replaceBSSIDForParam = new RatelHookFlagMethodCallback() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                ScanResult scanResult = findScanResultFromParams(param.args, ScanResult.class);
                if (scanResult == null) {
                    return;
                }
                scanResult.BSSID = getOriginBSSID(scanResult.BSSID);
                String originSSID = getOriginSSID(scanResult.BSSID);
                if (!TextUtils.isEmpty(originSSID)) {
                    scanResult.SSID = originSSID;
                }

            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ScanResult scanResult = findScanResultFromParams(param.args, ScanResult.class);
                if (scanResult == null) {
                    return;
                }
                FingerPrintModel.WIFIReplaceItem wifiReplaceItem = new FingerPrintModel.WIFIReplaceItem();
                wifiReplaceItem.BSSID = scanResult.BSSID;
                wifiReplaceItem.SSID = scanResult.SSID;
                getFakeBSSID(wifiReplaceItem);

                scanResult.BSSID = wifiReplaceItem.BSSID;
                scanResult.SSID = wifiReplaceItem.SSID;
            }
        };

        RposedBridge.hookAllMethodsSafe(wifiManagerIpClazz, "getMatchingOsuProviders", replaceBSSIDForParam);
        RposedBridge.hookAllMethodsSafe(wifiManagerIpClazz, "getMatchingWifiConfig", replaceBSSIDForParam);
        RposedBridge.hookAllMethodsSafe(WifiManager.class, "getMatchingOsuProviders", replaceBSSIDForParam);
        RposedBridge.hookAllMethodsSafe(WifiManager.class, "getMatchingWifiConfig", replaceBSSIDForParam);


        //handle configuration
        RatelHookFlagMethodCallback replaceBSSIDForWifiConfigurationParam = new RatelHookFlagMethodCallback() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                WifiConfiguration wifiConfiguration = findScanResultFromParams(param.args, WifiConfiguration.class);
                if (wifiConfiguration == null) {
                    return;
                }
                wifiConfiguration.BSSID = getOriginBSSID(wifiConfiguration.BSSID);
                String originSSID = getOriginSSID(wifiConfiguration.BSSID);
                if (!TextUtils.isEmpty(originSSID)) {
                    wifiConfiguration.SSID = originSSID;
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                WifiConfiguration wifiConfiguration = findScanResultFromParams(param.args, WifiConfiguration.class);
                if (wifiConfiguration == null) {
                    return;
                }
                FingerPrintModel.WIFIReplaceItem wifiReplaceItem = new FingerPrintModel.WIFIReplaceItem();
                wifiReplaceItem.BSSID = wifiConfiguration.BSSID;
                wifiReplaceItem.SSID = wifiConfiguration.SSID;
                getFakeBSSID(wifiReplaceItem);

                wifiConfiguration.BSSID = wifiReplaceItem.BSSID;
                wifiConfiguration.SSID = wifiReplaceItem.SSID;

            }
        };

        RposedBridge.hookAllMethodsSafe(wifiManagerIpClazz, "addOrUpdateNetwork", replaceBSSIDForWifiConfigurationParam);
        RposedBridge.hookAllMethodsSafe(wifiManagerIpClazz, "setWifiApEnabled", replaceBSSIDForWifiConfigurationParam);
        RposedBridge.hookAllMethodsSafe(wifiManagerIpClazz, "startSoftAp", replaceBSSIDForWifiConfigurationParam);
        RposedBridge.hookAllMethodsSafe(wifiManagerIpClazz, "setWifiApConfiguration", replaceBSSIDForWifiConfigurationParam);


        RposedBridge.hookAllMethodsSafe(WifiManager.class, "addOrUpdateNetwork", replaceBSSIDForWifiConfigurationParam);
        RposedBridge.hookAllMethodsSafe(WifiManager.class, "setWifiApEnabled", replaceBSSIDForWifiConfigurationParam);
        RposedBridge.hookAllMethodsSafe(WifiManager.class, "startSoftAp", replaceBSSIDForWifiConfigurationParam);
        RposedBridge.hookAllMethodsSafe(WifiManager.class, "setWifiApConfiguration", replaceBSSIDForWifiConfigurationParam);

        RatelHookFlagMethodCallback replaceBSSIDForWifiConfigurationResult = new RatelHookFlagMethodCallback() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                WifiConfiguration wifiConfiguration = (WifiConfiguration) param.getResult();
                if (wifiConfiguration == null) {
                    return;
                }
                FingerPrintModel.WIFIReplaceItem wifiReplaceItem = new FingerPrintModel.WIFIReplaceItem();
                wifiReplaceItem.BSSID = wifiConfiguration.BSSID;
                wifiReplaceItem.SSID = wifiConfiguration.SSID;
                getFakeBSSID(wifiReplaceItem);

                wifiConfiguration.BSSID = wifiReplaceItem.BSSID;
                wifiConfiguration.SSID = wifiReplaceItem.SSID;
            }
        };

        RposedBridge.hookAllMethodsSafe(wifiManagerIpClazz, "getMatchingWifiConfig", replaceBSSIDForWifiConfigurationResult);
        RposedBridge.hookAllMethodsSafe(wifiManagerIpClazz, "getWifiApConfiguration", replaceBSSIDForWifiConfigurationResult);
        RposedBridge.hookAllMethodsSafe(WifiManager.class, "getMatchingWifiConfig", replaceBSSIDForWifiConfigurationResult);
        RposedBridge.hookAllMethodsSafe(WifiManager.class, "getWifiApConfiguration", replaceBSSIDForWifiConfigurationResult);
    }


    @SuppressWarnings("unchecked")
    private static <T> T findScanResultFromParams(Object[] args, Class<T> clazz) {
        T scanResult = null;
        for (Object obj : args) {
            if (clazz.isAssignableFrom(obj.getClass())) {
                scanResult = (T) obj;
                break;
            }
        }
        if (scanResult == null) {
            //TODO
            Log.w(Constants.VENV_TAG, "can not get match obj for method:getMatchingOsuProviders");
            return null;
        }
        return scanResult;
    }

    private static boolean isFakeBSSID(String testFakeBSSID) {
        String property = propertiesStoreHolder.getProperty("origin_BSSID_" + testFakeBSSID);
        return StringUtils.isNotBlank(property);
    }

    private static String getOriginSSID(String fakeBSSID) {
        String property = propertiesStoreHolder.getProperty("origin_SSID_" + fakeBSSID);
        if (StringUtils.isNotBlank(property)) {
            return property;
        }
        return fakeBSSID;
    }

    private static String getOriginBSSID(String fakeBSSID) {
        String property = propertiesStoreHolder.getProperty("origin_BSSID_" + fakeBSSID);
        if (property != null) {
            return property;
        }
        return fakeBSSID;
    }

    private static void getFakeBSSID(FingerPrintModel.WIFIReplaceItem wifiReplaceItem) {
        if (isFakeBSSID(wifiReplaceItem.BSSID)) {
            Log.i(Constants.VENV_TAG, "is fake BSSID: " + wifiReplaceItem.BSSID);
            return;
        }
        String fakeBSSID = propertiesStoreHolder.getProperty("fake_BSSID_" + wifiReplaceItem.BSSID);
        String fakeSSID = propertiesStoreHolder.getProperty("fake_SSID_" + wifiReplaceItem.BSSID);
        if (!TextUtils.isEmpty(fakeBSSID)) {
            wifiReplaceItem.BSSID = fakeBSSID;
            if (!TextUtils.isEmpty(fakeSSID)) {
                wifiReplaceItem.SSID = fakeSSID;
            }
            return;
        }

        FingerPrintModel.WifiReplacer wifiReplacer = RatelToolKit.fingerPrintModel.wifiReplacer;
        if (wifiReplacer != null) {
            FingerPrintModel.WIFIReplaceItem wifiReplaceItemBackup = new FingerPrintModel.WIFIReplaceItem();
            wifiReplaceItemBackup.BSSID = wifiReplaceItem.BSSID;
            wifiReplaceItemBackup.SSID = wifiReplaceItem.SSID;
            wifiReplacer.doReplace(wifiReplaceItemBackup);

            if (StringUtils.isNotBlank(wifiReplaceItemBackup.BSSID) && !wifiReplaceItemBackup.BSSID.equals(wifiReplaceItem.BSSID)) {
                fakeBSSID = wifiReplaceItemBackup.BSSID;
                fakeSSID = wifiReplaceItemBackup.SSID;
            }

        }


        if (fakeBSSID == null) {
            String prefix = wifiReplaceItem.BSSID.substring(0, "d4:ee:07:11:".length());
            //String prefix = "11:11:11:11:";
            fakeBSSID = prefix + nextRandomHexByte() + ":" + nextRandomHexByte();
            fakeBSSID = fakeBSSID.toLowerCase(Locale.CHINA);
            Log.i(Constants.VENV_TAG, "use default replace plan!!");
        }
        if (StringUtils.isBlank(fakeSSID)) {
            fakeSSID = wifiReplaceItem.SSID;
        }


        propertiesStoreHolder.setProperty("fake_BSSID_" + wifiReplaceItem.BSSID, fakeBSSID);
        propertiesStoreHolder.setProperty("fake_SSID_" + wifiReplaceItem.BSSID, fakeSSID);
        propertiesStoreHolder.setProperty("origin_BSSID_" + fakeBSSID, wifiReplaceItem.BSSID);
        propertiesStoreHolder.setProperty("origin_SSID_" + fakeBSSID, wifiReplaceItem.SSID);

        wifiReplaceItem.BSSID = fakeBSSID;
        wifiReplaceItem.SSID = fakeSSID;
    }


    private static String nextRandomHexByte() {
        String s = Integer.toHexString(SuffixTrimUtils.getRandom().nextInt(255));
        if (s.length() == 1) {
            return "0" + s;
        }
        return s.substring(0, 2);
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
