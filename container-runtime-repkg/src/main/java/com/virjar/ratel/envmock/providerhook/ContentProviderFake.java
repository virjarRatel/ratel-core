package com.virjar.ratel.envmock.providerhook;

import android.content.pm.ProviderInfo;
import android.os.Build;
import android.os.IInterface;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.runtime.RatelRuntime;
import com.virjar.ratel.utils.BuildCompat;

import java.util.Map;

import mirror.android.app.ActivityThread;
import mirror.android.app.IActivityManager;
import mirror.android.content.ContentProviderHolderOreo;

public class ContentProviderFake {

    private static void fakeInstalledContentProvider() {
        //noinspection unchecked
        Map<Object, Object> clientMap = ActivityThread.mProviderMap.get(RatelRuntime.mainThread);
        for (Map.Entry<Object, Object> e : clientMap.entrySet()) {
            Object clientRecord = e.getValue();
            if (BuildCompat.isOreo()) {
                IInterface provider = ActivityThread.ProviderClientRecordJB.mProvider.get(clientRecord);
                Object holder = ActivityThread.ProviderClientRecordJB.mHolder.get(clientRecord);
                if (holder == null) {
                    continue;
                }
                ProviderInfo info = ContentProviderHolderOreo.info.get(holder);
                if (!info.authority.startsWith(Constants.ratelManagerPackage)) {
                    provider = ProviderHook.createProxy(true, info.authority, provider);
                    ActivityThread.ProviderClientRecordJB.mProvider.set(clientRecord, provider);
                    ContentProviderHolderOreo.provider.set(holder, provider);
                }
                if (RatelRuntime.isRatelDebugBuild) {
                    Log.i(Constants.TAG, "getContentProvider for  authority: " + info.authority);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                IInterface provider = ActivityThread.ProviderClientRecordJB.mProvider.get(clientRecord);
                Object holder = ActivityThread.ProviderClientRecordJB.mHolder.get(clientRecord);
                if (holder == null) {
                    continue;
                }
                ProviderInfo info = IActivityManager.ContentProviderHolder.info.get(holder);
                if (!info.authority.startsWith(Constants.ratelManagerPackage)) {
                    provider = ProviderHook.createProxy(true, info.authority, provider);
                    ActivityThread.ProviderClientRecordJB.mProvider.set(clientRecord, provider);
                    IActivityManager.ContentProviderHolder.provider.set(holder, provider);
                }
            } else {
                String authority = ActivityThread.ProviderClientRecord.mName.get(clientRecord);
                IInterface provider = ActivityThread.ProviderClientRecord.mProvider.get(clientRecord);
                if (provider != null && !authority.startsWith(Constants.ratelManagerPackage)) {
                    provider = ProviderHook.createProxy(true, authority, provider);
                    ActivityThread.ProviderClientRecord.mProvider.set(clientRecord, provider);
                }
            }
        }
    }

    private static void IPCHook() {
        //android.app.IActivityManager.Stub.Proxy#getContentProvider
        Class<?> IActivityManagerIPCClass = RposedHelpers.findClassIfExists("android.app.IActivityManager$Stub$Proxy", ClassLoader.getSystemClassLoader());
        if (IActivityManagerIPCClass == null) {
            Class<?> activityManagerNativeClass = RposedHelpers.findClass("android.app.ActivityManagerNative", ClassLoader.getSystemClassLoader());
            IActivityManagerIPCClass = RposedHelpers.callStaticMethod(activityManagerNativeClass, "getDefault").getClass();
        }


        RposedBridge.hookAllMethods(IActivityManagerIPCClass, "getContentProvider", new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object holder = param.getResult();
                if (holder == null) {
                    return;
                }
                if (BuildCompat.isOreo()) {
                    //ContentProviderHolder
                    IInterface provider = ContentProviderHolderOreo.provider.get(holder);
                    ProviderInfo info = ContentProviderHolderOreo.info.get(holder);
                    if (provider != null) {
                        provider = ProviderHook.createProxy(false, info.authority, provider);
                    }
                    ContentProviderHolderOreo.provider.set(holder, provider);
                    if (RatelRuntime.isRatelDebugBuild) {
                        Log.i(Constants.TAG, "getContentProvider for  authority: " + info.authority);
                    }
                } else {
                    IInterface provider = IActivityManager.ContentProviderHolder.provider.get(holder);
                    ProviderInfo info = IActivityManager.ContentProviderHolder.info.get(holder);
                    if (RatelRuntime.isRatelDebugBuild) {
                        Log.i(Constants.TAG, "getContentProvider for  authority: " + info.authority);
                    }
                    if (provider != null) {
                        provider = ProviderHook.createProxy(false, info.authority, provider);
                    }
                    IActivityManager.ContentProviderHolder.provider.set(holder, provider);
                }
            }
        });

        RposedBridge.hookAllMethods(IActivityManagerIPCClass, "getContentProviderExternal", new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object holder = param.getResult();
                if (holder == null) {
                    return;
                }
                if (BuildCompat.isOreo()) {
                    //ContentProviderHolder
                    IInterface provider = ContentProviderHolderOreo.provider.get(holder);
                    ProviderInfo info = ContentProviderHolderOreo.info.get(holder);
                    if (provider != null) {
                        provider = ProviderHook.createProxy(true, info.authority, provider);
                    }
                    ContentProviderHolderOreo.provider.set(holder, provider);
                    if (RatelRuntime.isRatelDebugBuild) {
                        Log.i(Constants.TAG, "getContentProvider for  authority: " + info.authority);
                    }
                } else {
                    IInterface provider = IActivityManager.ContentProviderHolder.provider.get(holder);
                    ProviderInfo info = IActivityManager.ContentProviderHolder.info.get(holder);
                    if (provider != null) {
                        provider = ProviderHook.createProxy(true, info.authority, provider);
                    }
                    IActivityManager.ContentProviderHolder.provider.set(holder, provider);
                }
            }
        });

    }

    /**
     * see http://www.msa-alliance.cn/
     * <br>
     * 移动安全联盟，工信部牵头rom厂商共同设计的id服务
     */
    private static void registerMSAFake() {
        ProviderHook.registerFetcher("com.miui.idprovider", (external, provider) -> new IDFake_MUI(provider));
        //TODO vivo通过ContentObserver实现的
        ProviderHook.registerFetcher("com.vivo.vms.IdProvider", (external, provider) -> new IDFake_VIVO(provider));

    }


    public static void fakeContentProvider() {
        fakeInstalledContentProvider();
        IPCHook();

        registerMSAFake();

        // ks通过底层provider之后获取了id
        ProviderHook.registerFetcher("settings", (external, provider) -> new SettingsProviderHook(provider));
    }
}
