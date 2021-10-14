package com.virjar.ratel.envmock.binder;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ParceledListSlice;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.inspect.Lists;
import com.virjar.ratel.envmock.MSAFake;
import com.virjar.ratel.envmock.TimeFake;
import com.virjar.ratel.envmock.binder.handlers.IPhoneSubInfoManagerHandler;
import com.virjar.ratel.envmock.binder.handlers.ISUBManagerHandler;
import com.virjar.ratel.envmock.binder.handlers.PackageManagerHandler;
import com.virjar.ratel.envmock.binder.handlers.PhoneManagerHandler;
import com.virjar.ratel.envmock.binder.servicehook.HUAWEI_OPENIDS_SERVICE_Handler;
import com.virjar.ratel.envmock.binder.servicehook.ServiceIPCHookManager;
import com.virjar.ratel.envmock.idfake.ActiveSubscriptionInfoListReplaceHandler;
import com.virjar.ratel.envmock.idfake.DeviceIdReplaceHandler;
import com.virjar.ratel.envmock.idfake.ImeiReplaceHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BinderFingerHookManager {
    public static void doMockBinder() {
        //com.android.internal.telephony.ITelephony
        addImeiReplaceHandler(PhoneManagerHandler.service, "getImeiForSlot", new ImeiReplaceHandler());
        addImeiReplaceHandler(PhoneManagerHandler.service, "getDeviceId", new ImeiReplaceHandler());
        addDeviceIdReplaceHandler(PhoneManagerHandler.service, "getMeidForSlot", new DeviceIdReplaceHandler("meid", RatelToolKit.fingerPrintModel.meid));

        // 设置为安装了sim卡，且sim卡是安装的状态
        // TODO ITelephony无getSimState方法
        BinderHookManager.addBinderHook(PhoneManagerHandler.service, "getSimState", new AfterHook() {

            @Override
            public Object afterIpcCall(Object[] args, Object result) {
                return TelephonyManager.SIM_STATE_READY;
            }
        });


        //com.android.internal.telephony.IPhoneSubInfo
        addDeviceIdReplaceHandler(IPhoneSubInfoManagerHandler.service, "getIccSerialNumber",
                new DeviceIdReplaceHandler("iccSerialNumber", RatelToolKit.fingerPrintModel.iccSerialNumber));
        addDeviceIdReplaceHandler(IPhoneSubInfoManagerHandler.service, "getSubscriberIdForSubscriber",
                new DeviceIdReplaceHandler("subscriberId", RatelToolKit.fingerPrintModel.iccSerialNumber));
        addDeviceIdReplaceHandler(IPhoneSubInfoManagerHandler.service, "getSubscriberId",
                new DeviceIdReplaceHandler("subscriberId", RatelToolKit.fingerPrintModel.iccSerialNumber));
        addDeviceIdReplaceHandler(IPhoneSubInfoManagerHandler.service, "getLine1Number",
                new DeviceIdReplaceHandler("line1Number", RatelToolKit.fingerPrintModel.line1Number));
        addImeiReplaceHandler(IPhoneSubInfoManagerHandler.service, "getDeviceIdForPhone",
                new ImeiReplaceHandler());
        addImeiReplaceHandler(IPhoneSubInfoManagerHandler.service, "getDeviceId",
                new ImeiReplaceHandler());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            BinderHookManager.addBinderHook(ISUBManagerHandler.service, "getActiveSubscriptionInfoList", new AfterHook() {

                @Override
                public Object afterIpcCall(Object[] args, Object result) {
                    ActiveSubscriptionInfoListReplaceHandler.doReplace((List<SubscriptionInfo>) result);
                    return result;
                }
            });
        }
        hideRatelRelatedApps();

        ServiceIPCHookManager.addBinderHook(HUAWEI_OPENIDS_SERVICE_Handler.targetPackage,
                HUAWEI_OPENIDS_SERVICE_Handler.action, "getOaid", new AfterHook() {
                    @Override
                    public Object afterIpcCall(Object[] args, Object result) {
                        return MSAFake.mockMSA((String) result);
                    }
                });

    }

    private static final Set<String> riskApp = new HashSet<String>() {
        {
            add("com.czb.va");
            add("moe.shizuku.privileged.api");
            add("com.koushikdutta.vysor");
            add("io.virtualapp.addon.arm64");
            add("rikka.shizuku.demo");
            add("com.astrill.astrillvpn");
            add("com.topjohnwu.magisk");
        }
    };

    private static boolean isRatelRelatedApp(String packageName) {
        if (Constants.ratelManagerPackage.equals(packageName)) {
            return true;
        }
        if (riskApp.contains(packageName)) {
            // 一些风险app，我们也把他隐藏掉
            return true;
        }
        if (packageName.contains("virjar")) {
            // 本人写的所有app，都不让看见
            return true;
        }
        return packageName.startsWith("ratel.");
    }

    private static void hideRatelRelatedApps() {
        BinderHookManager.addBinderHook(PackageManagerHandler.service,
                "getInstalledPackages", new AfterHook() {
                    @Override
                    public Object afterIpcCall(Object[] args, Object result) {
                        ParceledListSlice<PackageInfo> parceledListSlice = (ParceledListSlice<PackageInfo>) result;
                        if (parceledListSlice == null) {
                            return null;
                        }
                        List<PackageInfo> list = parceledListSlice.getList();
                        List<PackageInfo> newList = new ArrayList<>();
                        for (PackageInfo packageInfo : list) {
                            if (packageInfo.applicationInfo != null
                                    && packageInfo.applicationInfo.metaData != null
                                    && packageInfo.applicationInfo.metaData.containsKey("xposedmodule")) {
                                //隐藏所有的xp模块
                                continue;
                            }
                            if (isRatelRelatedApp(packageInfo.packageName)) {
                                continue;
                            }
                            TimeFake.fakeInstallTime(packageInfo);
                            newList.add(packageInfo);
                        }

                        return new ParceledListSlice<>(newList);
                    }
                });

        BinderHookManager.addBinderHook(PackageManagerHandler.service,
                "getPackageInfo", new AfterHook() {

                    @Override
                    public Object afterIpcCall(Object[] args, Object result) {
                        PackageInfo packageInfo = (PackageInfo) result;
                        if (packageInfo == null) {
                            return result;
                        }
                        TimeFake.fakeInstallTime(packageInfo);
                        return packageInfo;
                    }
                });

        BinderHookManager.addBinderHook(PackageManagerHandler.service,
                "getInstalledApplications", new AfterHook() {
                    @Override
                    public Object afterIpcCall(Object[] args, Object result) {
                        ParceledListSlice<ApplicationInfo> parceledListSlice = (ParceledListSlice<ApplicationInfo>) result;
                        if (parceledListSlice == null) {
                            return null;
                        }
                        List<ApplicationInfo> list = parceledListSlice.getList();
                        List<ApplicationInfo> newList = new ArrayList<>();
                        for (ApplicationInfo packageInfo : list) {
                            if (packageInfo.metaData != null && packageInfo.metaData.containsKey("xposedmodule")) {
                                //隐藏所有的xp模块
                                continue;
                            }
                            if (isRatelRelatedApp(packageInfo.packageName)) {
                                continue;
                            }
                            newList.add(packageInfo);
                        }
                        return new ParceledListSlice<>(newList);
                    }
                });

        BinderHookManager.addBinderHook(PackageManagerHandler.service, "getPackagesForUid",
                new AfterHook() {
                    @Override
                    public Object afterIpcCall(Object[] args, Object result) {
                        String[] pkgs = (String[]) result;
                        if (pkgs == null) {
                            return null;
                        }
                        List<String> filteredPkg = Lists.newLinkedList();
                        for (String pkg : pkgs) {
                            if (!isRatelRelatedApp(pkg)) {
                                filteredPkg.add(pkg);
                            }
                        }
                        return filteredPkg.toArray(new String[]{});
                    }
                });
    }

    private static void addImeiReplaceHandler(String service, String method, ImeiReplaceHandler imeiReplaceHandler) {
        BinderHookManager.addBinderHook(service, method, new AfterHook() {


            @Override
            public Object afterIpcCall(Object[] args, Object result) {
                return imeiReplaceHandler.doReplace((String) result, args);
            }
        });
    }

    private static void addDeviceIdReplaceHandler(String service, String method, DeviceIdReplaceHandler deviceIdReplaceHandler) {
        BinderHookManager.addBinderHook(service, method, new AfterHook() {

            @Override
            public Object afterIpcCall(Object[] args, Object result) {
                return deviceIdReplaceHandler.doReplace((String) result);
            }
        });
    }
}
