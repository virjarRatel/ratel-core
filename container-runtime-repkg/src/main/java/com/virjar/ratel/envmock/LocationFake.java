package com.virjar.ratel.envmock;

import android.location.Location;
import android.os.Bundle;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.SuffixTrimUtils;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.hook.RatelHookFlagMethodCallback;
import com.virjar.ratel.runtime.RatelConfig;
import com.virjar.ratel.runtime.RatelEnvironment;
import com.virjar.ratel.runtime.RatelRuntime;

import java.util.List;

import external.org.apache.commons.lang3.StringUtils;

public class LocationFake {


    public static boolean enable = true;

    public static void setEnable(boolean enable) {
        LocationFake.enable = enable;
    }

    /**
     * 赤道上经度差1度对应距离111千米，1秒对应距离111/360＝0.31千米。
     * 经线上纬度差1度对应距离111千米，1秒对应距离111/360＝0.31千米。
     * <p>
     * 天安门：(116.403838,39.914382) 东北1公里偏移坐标：(116.416055,39.92401) ,北京1公里差异:(0.012217000000006806,0.009627999999999304)
     * <p>
     * 25公里经度差0.31 ,城市直径约50公里
     */
    private static double latitudeDiff;
    private static double longitudeDiff;

    private static double smallLatitudeDiff = SuffixTrimUtils.getRandom().nextGaussian() * 0.01;
    private static double smallLongitudeDiff = SuffixTrimUtils.getRandom().nextGaussian() * 0.008;

    private static final String latitudeDiffKey = "latitudeDiffKey";
    private static final String longitudeDiffKey = "longitudeDiffKey";

    private static boolean useAutoLocation = false;

    public static void fakeLocation() {
        String autoLocationMode = RatelConfig.getConfig(Constants.venvLocationAuto);
        if (StringUtils.isBlank(autoLocationMode)) {
            Double baseLongitude = RatelToolKit.fingerPrintModel.longitude.get();
            Double baseLatitude = RatelToolKit.fingerPrintModel.latitude.get();
            if (baseLongitude == null || baseLatitude == null) {
                autoLocationMode = "true";
            } else {
                autoLocationMode = "false";
            }
            RatelConfig.setConfig(Constants.venvLocationAuto, autoLocationMode);
        }
        useAutoLocation = "true".equalsIgnoreCase(autoLocationMode);

        if (useAutoLocation) {
            parseDiff();
        }

        fakeCellInfo();
        fakeILocationManager();
    }

    private static final String ILocationManagerIPCClassName = "android.location.ILocationManager$Stub$Proxy";
    private static final Class ILocationManagerIPCClass = RposedHelpers.findClassIfExists(ILocationManagerIPCClassName, ClassLoader.getSystemClassLoader());


    private static void fakeILocationManager() {
        if (ILocationManagerIPCClass == null) {
            Log.w(Constants.TAG, "can not find ipc class: " + ILocationManagerIPCClassName);
            return;
        }

        RposedBridge.hookAllMethodsSafe(ILocationManagerIPCClass, "getLastKnownLocation", new RatelHookFlagMethodCallback() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!enable) {
                    return;
                }
                Location location = (Location) param.getResult();
                if (location == null) {
                    return;
                }

                double baseLatitude;
                double baseLongitude;

                if (useAutoLocation) {
                    baseLatitude = location.getLatitude() + latitudeDiff;
                    baseLongitude = location.getLongitude() + longitudeDiff;
                    RatelToolKit.fingerPrintModel.latitude.setOrigin(location.getLatitude());
                    RatelToolKit.fingerPrintModel.longitude.setOrigin(location.getLongitude());
                } else {
                    baseLatitude = RatelToolKit.fingerPrintModel.latitude.replace(null, location.getLatitude());
                    baseLongitude = RatelToolKit.fingerPrintModel.longitude.replace(null, location.getLongitude());
                }

                RposedHelpers.setObjectField(location, "mLatitude", baseLatitude + smallLatitudeDiff);
                RposedHelpers.setObjectField(location, "mLongitude", baseLongitude + smallLongitudeDiff);

                if (RatelRuntime.isRatelDebugBuild) {
                    Log.i(Constants.VENV_TAG, "after Location Mock: " + location);
                }
            }
        });
    }

    private static void fakeCellInfo() {
        //com.android.internal.telephony.ITelephony.Stub.Proxy#getAllCellInfo
        Class<?> telephonyStubBinderProxyClass = RposedHelpers.findClassIfExists("com.android.internal.telephony.ITelephony$Stub$Proxy", ClassLoader.getSystemClassLoader());


        RposedBridge.hookAllMethods(telephonyStubBinderProxyClass, "getAllCellInfo", new RatelHookFlagMethodCallback() {
            @Override
            @SuppressWarnings("unchecked")
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                java.util.List<android.telephony.CellInfo> cellInfoList = (List<CellInfo>) param.getResult();
                if (cellInfoList == null) {
                    return;
                }
                for (int i = 0; i < cellInfoList.size(); i++) {
                    mockCellInfo(cellInfoList.get(i), i);
                }
            }
        });

        //com.android.internal.telephony.ITelephony.Stub.Proxy#getCellLocation
        RposedBridge.hookAllMethods(telephonyStubBinderProxyClass, "getCellLocation", new RatelHookFlagMethodCallback() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!enable) {
                    return;
                }
                Object result = param.getResult();
                if (result instanceof Bundle) {
                    handleCallLocationWithBundle((Bundle) result);
                } else if (result instanceof CellIdentityGsm) {
                    CellIdentityGsm cellIdentityGsm = (CellIdentityGsm) result;
                    handleCallLocationWithCellIdentityGsm(cellIdentityGsm);
                } else {
                    Log.e(Constants.TAG, "can not handle cellLocation result: " + result);
                }


            }
        });

        //TODO com.android.internal.telephony.ITelephony#getNeighboringCellInfo
    }

    private static void handleCallLocationWithCellIdentityGsm(CellIdentityGsm cellIdentityGsm) {

        int lac = cellIdentityGsm.getLac();
        int cid = cellIdentityGsm.getCid();

        //int psc = cellLocationBundle.getInt("psc", defaultInt);
        if (Integer.valueOf(cid).equals(RatelToolKit.fingerPrintModel.cid.getOrigin())) {
            RposedHelpers.setObjectField(cellIdentityGsm, "mCid", RatelToolKit.fingerPrintModel.cid.replace(null, cid));
            RposedHelpers.setObjectField(cellIdentityGsm, "mLac", RatelToolKit.fingerPrintModel.lac.replace(null, lac));
        } else if (Integer.valueOf(cid).equals(RatelToolKit.fingerPrintModel.cid2.getOrigin())) {
            RposedHelpers.setObjectField(cellIdentityGsm, "mCid", RatelToolKit.fingerPrintModel.cid2.replace(null, cid));
            RposedHelpers.setObjectField(cellIdentityGsm, "mLac", RatelToolKit.fingerPrintModel.lac2.replace(null, lac));
        } else {
            RposedHelpers.setObjectField(cellIdentityGsm, "mCid", RatelToolKit.fingerPrintModel.cid.replace(null, cid));
            RposedHelpers.setObjectField(cellIdentityGsm, "mLac", RatelToolKit.fingerPrintModel.lac.replace(null, lac));
        }


    }

    private static void handleCallLocationWithBundle(Bundle cellLocationBundle) {
        //for gms network
        int defaultInt = -1;
        if (cellLocationBundle.containsKey("lac")) {
            int lac = cellLocationBundle.getInt("lac", defaultInt);
            int cid = cellLocationBundle.getInt("cid", defaultInt);
            if (cid != defaultInt) {
                //int psc = cellLocationBundle.getInt("psc", defaultInt);
                if (Integer.valueOf(cid).equals(RatelToolKit.fingerPrintModel.cid.getOrigin())) {
                    cellLocationBundle.putInt("cid", RatelToolKit.fingerPrintModel.cid.replace(null, cid));
                    cellLocationBundle.putInt("lac", RatelToolKit.fingerPrintModel.lac.replace(null, lac));
                } else if (Integer.valueOf(cid).equals(RatelToolKit.fingerPrintModel.cid2.getOrigin())) {
                    cellLocationBundle.putInt("cid", RatelToolKit.fingerPrintModel.cid2.replace(null, cid));
                    cellLocationBundle.putInt("lac", RatelToolKit.fingerPrintModel.lac2.replace(null, lac));
                } else {
                    cellLocationBundle.putInt("cid", RatelToolKit.fingerPrintModel.cid.replace(null, cid));
                    cellLocationBundle.putInt("lac", RatelToolKit.fingerPrintModel.lac.replace(null, lac));
                }

            }
        }

        //for CDMA network
        if (cellLocationBundle.containsKey("baseStationLatitude")) {
            int baseStationLatitude = cellLocationBundle.getInt("baseStationLatitude", defaultInt);
            int baseStationLongitude = cellLocationBundle.getInt("baseStationLongitude", defaultInt);
            if (baseStationLatitude != defaultInt) {
                cellLocationBundle.putInt("baseStationLatitude", baseStationLatitude + (int) ((latitudeDiff + smallLatitudeDiff) * 2592000 / 180));
            }
            if (baseStationLongitude != defaultInt) {
                cellLocationBundle.putInt("baseStationLongitude", baseStationLongitude + (int) ((longitudeDiff + smallLongitudeDiff) * 2592000 / 180));
            }
        }
    }

    private static void parseDiff() {
        PropertiesStoreHolder propertiesHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.telephonyFakeFile());
        String latitudeDiffStr = propertiesHolder.getProperty(latitudeDiffKey);
        if (!TextUtils.isEmpty(latitudeDiffStr)) {
            latitudeDiff = Double.parseDouble(latitudeDiffStr);
        } else {
            Double latitudeDiffObj = RatelToolKit.fingerPrintModel.latitudeDiff.get();
            if (latitudeDiffObj != null) {
                latitudeDiff = latitudeDiffObj;
            } else {
                latitudeDiff = (SuffixTrimUtils.getRandom().nextDouble() - 0.5) * 0.6;
            }
            propertiesHolder.setProperty(latitudeDiffKey, String.valueOf(latitudeDiff));
            RatelToolKit.fingerPrintModel.latitudeDiff.set(latitudeDiff);
        }


        String longitudeDiffStr = propertiesHolder.getProperty(longitudeDiffKey);
        if (!TextUtils.isEmpty(longitudeDiffStr)) {
            longitudeDiff = Double.parseDouble(longitudeDiffStr);
        } else {
            Double longitudeDiffObj = RatelToolKit.fingerPrintModel.longitudeDiff.get();
            if (longitudeDiffObj != null) {
                longitudeDiff = longitudeDiffObj;
            } else {
                longitudeDiff = (SuffixTrimUtils.getRandom().nextDouble() - 0.5) * 0.6;
            }
            propertiesHolder.setProperty(longitudeDiffKey, String.valueOf(longitudeDiff));
            RatelToolKit.fingerPrintModel.longitudeDiff.set(longitudeDiff);
        }
    }

    private static void mockCellInfo(CellInfo cellInfo, int index) {
        if (cellInfo instanceof CellInfoCdma) {
            //电信
            mockCdmaCellInfo((CellInfoCdma) cellInfo, index);
        } else if (cellInfo instanceof CellInfoGsm) {
            // gsm 就是各种gsm
            mockGmsCellInfo((CellInfoGsm) cellInfo, index);
        } else if (cellInfo instanceof CellInfoLte) {
            // 13-移动、联通、电信 —— 4G 各个运营商都可能使用。
            mockLteCellInfo((CellInfoLte) cellInfo, index);
        } else if (cellInfo instanceof CellInfoWcdma) {
            //联通 WCDMA 46006 UMTS定义是一种3G移动电话技术，使用WCDMA作为底层标准，WCDMA向下兼容GSM网络。
            mockWcdmaCellInfo((CellInfoWcdma) cellInfo, index);
        } else {
            Log.w(Constants.TAG, "unknown CellInfo type: " + cellInfo.getClass());
        }
    }

    private static void mockCdmaCellInfo(CellInfoCdma cellInfoCdma, int index) {
        CellIdentityCdma cellIdentity = cellInfoCdma.getCellIdentity();
        //mobile Network Code,移动网络号。两位数（中国移动0，中国联通1，中国电信2）
        //int mnc = cellIdentity.getSystemId();
        //Location Area Code，定位区域编码，2个字节长的十六进制BCD码（不包括0000和FFFE）
        int lac = cellIdentity.getNetworkId();
        //Cell Identity，信元标识，2个字节
        int cid = cellIdentity.getBasestationId();
        //int rssi = cellInfoCdma.getCellSignalStrength().getCdmaDbm();

        /*
         * Longitude is a decimal number as specified in 3GPP2 C.S0005-A v6.0.
         * It is represented in units of 0.25 seconds and ranges from -2592000
         * to 2592000, both values inclusive (corresponding to a range of -180
         * to +180 degrees).
         * 请注意，我们使用double类型表示的经纬度，所以diff也是double表述的。但是cellInfo这里使用int类型，所以需要把double转换为int
         */
        if (enable) {
            if (useAutoLocation) {
                RposedHelpers.setObjectField(cellIdentity, "mLatitude", cellIdentity.getLatitude() + (int) ((latitudeDiff + smallLatitudeDiff) * 2592000 / 180));
                RposedHelpers.setObjectField(cellIdentity, "mLongitude", cellIdentity.getLongitude() + (int) ((longitudeDiff + smallLongitudeDiff) * 2592000 / 180));
                RatelToolKit.fingerPrintModel.latitude.setOrigin(cellIdentity.getLatitude() * 180D / 2592000);
                RatelToolKit.fingerPrintModel.longitude.setOrigin(cellIdentity.getLongitude() * 180D / 2592000);
            } else {
                double baseLatitude = RatelToolKit.fingerPrintModel.latitude.replace(null, cellIdentity.getLatitude() * 180D / 2592000);
                double baseLongitude = RatelToolKit.fingerPrintModel.longitude.replace(null, cellIdentity.getLongitude() * 180D / 2592000);
                RposedHelpers.setObjectField(cellIdentity, "mLatitude", (int) ((baseLatitude + smallLatitudeDiff) * 2592000 / 180));
                RposedHelpers.setObjectField(cellIdentity, "mLongitude", (int) ((baseLongitude + smallLongitudeDiff) * 2592000 / 180));
            }
        }

        if (index == 0) {
            RposedHelpers.setObjectField(cellIdentity, "mNetworkId", RatelToolKit.fingerPrintModel.lac.replace(null, lac));
            RposedHelpers.setObjectField(cellIdentity, "mBasestationId", RatelToolKit.fingerPrintModel.cid.replace(null, cid));
        } else if (index == 1) {
            RposedHelpers.setObjectField(cellIdentity, "mNetworkId", RatelToolKit.fingerPrintModel.lac2.replace(null, lac));
            RposedHelpers.setObjectField(cellIdentity, "mBasestationId", RatelToolKit.fingerPrintModel.cid2.replace(null, cid));
        } else {
            Log.w(Constants.TAG, "find cell more than two !! mockCdmaCellInfo,index: " + index);
        }
    }

    private static void mockGmsCellInfo(CellInfoGsm cellInfoGsm, int index) {
        CellIdentityGsm cellIdentity = cellInfoGsm.getCellIdentity();
        //mock
        int lac = cellIdentity.getLac();
        //mock
        int cid = cellIdentity.getCid();

        //int psc = cellIdentity.getPsc();


        //do mock
        if (index == 0) {
            Integer newGMSCid;
            PropertiesStoreHolder propertiesHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.telephonyFakeFile());
            if (propertiesHolder.hasProperty("newGMSCidDiff")) {
                newGMSCid = cid + Integer.parseInt(propertiesHolder.getProperty("newGMSCidDiff"));
            } else {
                newGMSCid = RatelToolKit.fingerPrintModel.cid.replace(null, cid);
                propertiesHolder.setProperty("newGMSCidDiff", String.valueOf(newGMSCid - cid));
            }

            RposedHelpers.setObjectField(cellIdentity, "mCid", newGMSCid);
        }
    }

    private static void mockLteCellInfo(CellInfoLte cellInfoLte, int index) {
        CellIdentityLte cellIdentity = cellInfoLte.getCellIdentity();
        int tac = cellIdentity.getTac();
        int cid = cellIdentity.getCi();

        if (index == 0) {
            //实践证明，可能有n个cellInfo，但是实际只有第一个有作用，其他都是默认值？？应该和实际有插了多少SIM卡有关系吧
            //而且这个修改抖动范围很小，只能修改cid,不跟修改tac
            Integer newLteCid;
            PropertiesStoreHolder propertiesHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.telephonyFakeFile());
            if (propertiesHolder.hasProperty("newLteCidDiff")) {
                newLteCid = Integer.parseInt(propertiesHolder.getProperty("newLteCidDiff"));
            } else {
                newLteCid = RatelToolKit.fingerPrintModel.cid.replace(null, cid);
                propertiesHolder.setProperty("newLteCidDiff", String.valueOf(newLteCid - cid));
            }
            RposedHelpers.setObjectField(cellIdentity, "mCi", newLteCid);
        }

    }

    private static void mockWcdmaCellInfo(CellInfoWcdma cellInfoWcdma, int index) {
        CellIdentityWcdma cellIdentity = cellInfoWcdma.getCellIdentity();
        int lac = cellIdentity.getLac();
        int cid = cellIdentity.getCid();

        //do mock
        if (index == 0) {
            Integer newWcdmaCid;
            PropertiesStoreHolder propertiesHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.telephonyFakeFile());
            if (propertiesHolder.hasProperty("newWcdmaCidDiff")) {
                newWcdmaCid = Integer.parseInt(propertiesHolder.getProperty("newWcdmaCidDiff"));
            } else {
                newWcdmaCid = RatelToolKit.fingerPrintModel.cid.replace(null, cid);
                propertiesHolder.setProperty("newWcdmaCidDiff", String.valueOf(newWcdmaCid - cid));
            }

            RposedHelpers.setObjectField(cellIdentity, "mCid", newWcdmaCid);
        }
    }
}
